package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.tool.ExpressionProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;


public class ExpressionProcessTest {

    private final Map<String, Object> target1 = new LinkedHashMap<>();
    private final Map<String, Object> target2 = new LinkedHashMap<>();

    {
        target1.put("code", 200);
        target1.put("message", true);
        target1.put("data.status", 1);
        target1.put("data.reason", null);
        target1.put("data.operation", "{\"success\": 200}");
        target1.put("data.name", "{{vars.name}}");
        target1.put("data.price", "{{10*10}}");
        target1.put("data.id", "{{response.data.id}}");
        target1.put("data.age", "{{response.data.age}}");

        target2.put("code", 200);
        target2.put("message", true);
        target2.put("data.status", 1);
        target2.put("data.reason", null);
        target2.put("data.operation", "{\"success\": 200}");
        target2.put("data.name", "chenfeng");
        target2.put("data.price", 100);
        target2.put("data.id", 1);
        target2.put("data.age", 18);
    }

    @Test
    public void testHandleToUnidimensional() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", 1);
        map.put("reason", null);
        map.put("operation", "{\"success\": 200}");
        map.put("name", "{{vars.name}}");
        map.put("price", "{{10*10}}");
        map.put("id", "{{response.data.id}}");
        map.put("age", "{{response.data.age}}");

        Map<String, Object> expect = new LinkedHashMap<>();
        expect.put("code", 200);
        expect.put("message", true);
        expect.put("data", map);

        Map<String, Object> handled = ExpressionProcess.handleToUnidimensional("", expect);

        Assert.assertEquals(target1, handled);
    }

    @Test
    public void testHandleExpectExpressions() {
        Step.StepContext stepContext = new Step.StepContext();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("name", "chenfeng");
        stepContext.setVars(vars);

        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 1);
        data.put("age", 18);
        response.put("data", data);
        stepContext.setResponse(response);

        Map<String, Object> handled = ExpressionProcess.handleExpectExpressions(target1, stepContext);
        Assert.assertEquals(target2, handled);
    }
}