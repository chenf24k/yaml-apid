package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.model.Context;
import cn.chenf24k.hr.model.enums.PROTOCOL;
import cn.chenf24k.hr.tool.JsonUtil;
import cn.chenf24k.hr.tool.PrintUtil;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Step {
    private String title;
    private PROTOCOL protocol;
    private HttpRequest request;
    private Bind bind;
    private Map<String, Object> expect;

    // 当前步骤内的上下文
    private StepContext stepContext = new StepContext();
    private final List<Result> results = new ArrayList<>();

    private final String[] relationalOperator = new String[]{"<", ">", "==", ">=", "<=", "!="};

    public void run() {
        System.out.println("====>  " + this.getTitle());
        String response = this.getRequest().request();
        Object object = JsonUtil.toObject(response, Object.class);
        this.stepContext.setResponse(object);
        this.bindVars();

        Map<String, String> handled = this.handleExpect("", this.getExpect());
        this.assertFunc(handled);
        outputResult();
    }

    /**
     * 处理像响应后的参数提取、注入上下文
     */
    private void bindVars() {
        if (this.bind != null) {
            Map<String, String> bindVars = this.bind.getVars();
            if (!bindVars.isEmpty()) {
                Set<String> keySet = bindVars.keySet();
                for (String key : keySet) {
                    String template = bindVars.get(key);
                    String expression = TemplateProcess.extractTemplate(template);
                    try {
                        String result = (String) Ognl.getValue(expression, this.stepContext);
                        bindVars.put(key, result);
                    } catch (OgnlException e) {
                        e.printStackTrace();
                    }
                }
                Context.vars.putAll(bindVars);
            }
        }
    }

    /**
     * 递归处理期望值
     *
     * @param root
     * @param expect
     * @return
     */
    private Map<String, String> handleExpect(String root, Map<String, Object> expect) {
        Map<String, String> expectMaps = new LinkedHashMap<>();
        if (expect != null && expect.size() > 0) {
            expect.forEach((key, template) -> {
                if (template instanceof Map) {
                    expectMaps.putAll(this.handleExpect(key, (Map<String, Object>) template));
                } else {
                    expectMaps.put(
                            Objects.equals(root, "") ? key : root + "." + key
                            ,
                            (String) template
                    );
                }
            });
        }
        return expectMaps;
    }

    private void assertFunc(Map<String, String> handled) {
        GlobalContext globalContext = new GlobalContext(Context.vars);

        if (handled != null && handled.size() > 0) {
            handled.forEach((key, expectEl) -> {
                // 判断是否为模板字符
                boolean isTemplate = TemplateProcess.isTemplate(expectEl);

                if (isTemplate) {
                    // 模板字符：是：表达式处理
                    String expression = TemplateProcess.extractTemplate(expectEl);

                    // 是否进行 关系运算比较
                    boolean isContainsMark = Arrays.stream(relationalOperator).anyMatch(expression::contains);
                    Object expectValue = null;
                    try {
                        expectValue = Ognl.getValue(expression, globalContext);
                    } catch (OgnlException ignored) {

                    }
                    if (isContainsMark) {
                        if (expectValue == null) {
                            try {
                                expectValue = Ognl.getValue(expression, this.stepContext);
                            } catch (OgnlException e) {
                                e.printStackTrace();
                            }
                        }

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

                        Result result = new Result((Boolean) expectValue, key, expression, actualValue);
                        results.add(result);
                    } else {
                        if (expectValue == null) {
                            try {
                                expectValue = Ognl.getValue(expression, this.stepContext);
                            } catch (OgnlException e) {
                                e.printStackTrace();
                            }
                        }

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

                        boolean b = expectValue.equals(actualValue);
                        Result result = new Result(b, key, expectValue, actualValue);
                        results.add(result);
                    }
                } else {
                    // 模板字符：否：直接进行字符串比较
                    Object actualValue = null;
                    try {
                        Object response = this.stepContext.getResponse();
                        actualValue = Ognl.getValue(key, response);
                    } catch (OgnlException e) {
                        e.printStackTrace();
                    }

                    if (actualValue != null) {
                        String type = actualValue.getClass().getName();
                        if (type.contains("Double")) {
                            actualValue = String.valueOf(((Double) actualValue).intValue());
                        }
                    }

                    boolean b = ((Object) expectEl).equals(actualValue);
                    Result result = new Result(b, key, expectEl, actualValue);
                    results.add(result);
                }
            });

        }
    }

    private void outputResult() {
        List<Result> collect = results.stream().filter(Result::isSuccess)
                .collect(Collectors.toList());
        if (results.size() == collect.size())
            PrintUtil.printSuccess(this.title + ": all tests success");
        else
            PrintUtil.printError(this.title + ": test failed");
        PrintUtil.printResult(results);
    }

    @Data
    @NoArgsConstructor
    private static class StepContext {
        private Object response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class GlobalContext {
        private Object vars;
    }

}
