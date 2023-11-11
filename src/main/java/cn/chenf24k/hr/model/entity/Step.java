package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import cn.chenf24k.hr.model.enums.PROTOCOL;
import cn.chenf24k.hr.tool.JsonUtil;
import cn.chenf24k.hr.tool.PrintUtil;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Slf4j
public class Step {
    private String title;
    private PROTOCOL protocol;
    private HttpRequest request;
    private Bind bind;
    private Map<String, Object> expect;

    // 当前步骤内的上下文
    private StepContext stepContext = new StepContext();
    private final List<Result> results = new LinkedList<>();

    public void run() {
        log.info("Step: {}", this.getTitle());
        CustomResponse requested = this.getRequest().request();
        Object object = JsonUtil.toObject(requested.getResponse(), LinkedHashMap.class);
        // 将响应塞入 stepContext 用于断言比较
        this.stepContext.setResponse(object);
        this.bindVars();

        Map<String, String> handled = this.handleToUnidimensional("", this.getExpect());
        // 将全局变量塞入 stepContext 用于断言比较
        this.stepContext.setVars(GlobalContext.getInstance().getVars());

        Map<String, Object> stringObjectMap = this.handleExpectExpressions(handled);
        this.assertFunc(stringObjectMap);
        outputResult();
    }

    /**
     * 处理像响应后的参数绑定、注入全局上下文
     */
    private void bindVars() {
        if (this.bind != null && !this.bind.getVars().isEmpty()) {
            Map<String, Object> bindVars = this.bind.getVars();
            bindVars.forEach((varKey, varValue) -> {
                Object object = bindVars.get(varKey);
                if (object instanceof String) {
                    String template = String.valueOf(object);
                    boolean isTemplate = TemplateProcess.isTemplate(template);
                    if (isTemplate) {
                        String expression = TemplateProcess.extractTemplate(template);
                        try {
                            Object value = Ognl.getValue(expression, this.stepContext);
                            bindVars.put(varKey, value);
                        } catch (OgnlException e) {
                            // e.printStackTrace();
                        }
                    } else {
                        bindVars.put(varKey, varValue);
                    }
                } else {
                    bindVars.put(varKey, varValue);
                }

            });
            GlobalContext.getInstance().putAll(bindVars);
        }
    }

    /**
     * 递归 将期望keys，返回一个平面的map集合
     *
     * @param root   期望值的key表达式
     * @param expect 期望值map集合
     * @return Map<String, String>
     */
    private Map<String, String> handleToUnidimensional(String root, Map<String, Object> expect) {
        Map<String, String> expectMaps = new LinkedHashMap<>();
        if (expect != null && !expect.isEmpty())
            expect.forEach((key, template) -> {
                String rootString = Objects.equals(root, "") ? key : root + "." + key;
                if (template instanceof Map)
                    expectMaps.putAll(this.handleToUnidimensional(rootString, (Map<String, Object>) template));
                else {
                    expectMaps.put(
                            rootString
                            ,
                            Objects.toString(template)
                    );
                }
            });
        return expectMaps;
    }

    /**
     * 期望的集合变量求值
     *
     * @param handled 已经处理为平面的集合
     * @return Map<String, Object>
     */
    private Map<String, Object> handleExpectExpressions(Map<String, String> handled) {
        Map<String, Object> actualWithExpect = new LinkedHashMap<>();
        if (handled != null && !handled.isEmpty()) {
            handled.forEach((key, expectEl) -> {
                boolean isTemplate = TemplateProcess.isTemplate(expectEl);
                Object expectValue = null;
                if (isTemplate) {
                    try {
                        String extracted = TemplateProcess.extractTemplate(expectEl);
                        expectValue = Ognl.getValue(extracted, this.stepContext);
                    } catch (OgnlException ignored) {

                    }
                } else {
                    expectValue = expectEl;
                }
                actualWithExpect.put(key, expectValue);
            });
        }
        return actualWithExpect;
    }

    /**
     * 断言处理
     *
     * @param handled 已经求值的集合
     */
    private void assertFunc(Map<String, Object> handled) {
        if (handled != null && !handled.isEmpty()) {
            handled.forEach((key, expectEl) -> {
                // 模板字符：否：直接进行字符串比较
                Object actualValue = null;
                try {
                    Object response = this.stepContext.getResponse();
                    actualValue = Ognl.getValue(key, response);
                } catch (OgnlException e) {
                    // e.printStackTrace();
                }

                if (actualValue != null) {
                    String type = actualValue.getClass().getName();
                    if (type.contains("Double")) {
                        actualValue = String.valueOf(((Double) actualValue).intValue());
                    }
                }

                Result result;
                if (expectEl instanceof Boolean) {
                    result = new Result((Boolean) expectEl, key, expectEl, actualValue);
                    results.add(result);
                } else {
                    if (expectEl == null) {
                        expectEl = "null";
                    }
                    boolean b = expectEl.equals(actualValue);
                    result = new Result(b, key, expectEl, actualValue);
                    results.add(result);
                }
            });

        }
    }

    /**
     * 打印结果
     */
    private void outputResult() {
        List<Result> collect = results.stream().filter(Result::isSuccess)
                .collect(Collectors.toList());
        if (results.size() == collect.size())
            PrintUtil.printSuccess(this.title + ": all tests success");
        else
            PrintUtil.printError(this.title + ": test failed");
        PrintUtil.printResult(results);
    }

    /**
     * 上下文，仅在当前步骤中生效
     */
    @Data
    @NoArgsConstructor
    private static final class StepContext {
        private Object response;
        private Object vars;
    }

}
