package cn.chenf24k.hr.tool;


import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TemplateProcessTest {

    @Test
    public void testProcess() {
        final Map<String, String> varsMap = new HashMap<>();
        varsMap.put("weight", "100");
        varsMap.put("varieties", "fuji");
        String templateSql = "select id, weight, varieties from apple where weight > {{weight}} and varieties = '{{varieties}}'";
        String processedSql = TemplateProcess.processTemplate(templateSql, varsMap);
        String expectSql = "select id, weight, varieties from apple where weight > 100 and varieties = 'fuji'";
        Assert.assertEquals(expectSql, processedSql);
    }

    @Test
    public void textExtract() {
        String value = "{{a}}-{{b}}-{{c}}";
        List<String> extracted = TemplateProcess.extractAllTemplate(value);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, extracted.toArray());
    }

    @Test
    public void testExtractTemplate() {
        String value = "{{user.age > 1}}";
        String extracted = TemplateProcess.extractTemplate(value);
        Assert.assertEquals("user.age > 1", extracted);
    }

    @Test
    public void testIsTemplate() {
        String template = "{{user.age > 1}}";
        String notTemplate = "good days";
        Assert.assertTrue(TemplateProcess.isTemplate(template));
        Assert.assertFalse(TemplateProcess.isTemplate(notTemplate));

    }
}