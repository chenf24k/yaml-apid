package cn.chenf24k.hr.tool;


import cn.chenf24k.hr.model.Context;

public class TemplateProcessTest {


    public static void testProcess() {
        Context.vars.put("a", "v");
        String value = "{{a}}";
        String processed = TemplateProcess.processTemplate(value, Context.vars);
        System.out.println(processed);
    }

    public static void textExtract() {
        String value = "{{a}}";
        String processed = TemplateProcess.extractTemplate(value);
        System.out.println(processed);
    }

    public static void main(String[] args) {
        testProcess();
        textExtract();
    }

}