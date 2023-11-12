package cn.chenf24k.hr.tool;

import cn.chenf24k.hr.model.entity.Step;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表达式处理
 *
 * @author chenfeng
 * @version 1.0.0
 * @date 2023-11-12 10:10 AM
 */
public class ExpressionProcess {

    /**
     * 递归 将期望keys，返回一个平面的map集合
     *
     * @param root   期望值的key表达式
     * @param expect 期望值map集合
     * @return Map<String, String>
     * @auther chenfeng
     */
    public static Map<String, Object> handleToUnidimensional(String root, Map<String, Object> expect) {
        Map<String, Object> expectMaps = new LinkedHashMap<>();
        if (expect != null && !expect.isEmpty()) expect.forEach((key, template) -> {
            String rootString = Objects.equals(root, "") ? key : root + "." + key;
            if (template instanceof Map)
                expectMaps.putAll(handleToUnidimensional(rootString, (Map<String, Object>) template));
            else {
                expectMaps.put(rootString, template);
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
    public static Map<String, Object> handleExpectExpressions(Map<String, Object> handled, Step.StepContext stepContext) {
        Map<String, Object> actualWithExpect = new LinkedHashMap<>();
        if (handled != null && !handled.isEmpty()) {
            handled.forEach((key, expectEl) -> {
                boolean isTemplate = TemplateProcess.isTemplate(expectEl);
                Object expectValue = null;
                if (isTemplate) {
                    try {
                        String extracted = TemplateProcess.extractTemplate((String) expectEl);
                        expectValue = Ognl.getValue(extracted, stepContext);
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

}
