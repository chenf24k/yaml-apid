package cn.chenf24k.hr.tool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther chenfeng
 * @date 2023/11/2
 */
public final class TemplateProcess {

    /**
     * 替换模板字符串
     *
     * @param targetContent 操作的字符串
     * @param params        变量集合
     * @return 替换后的字符串
     */
    public static String processTemplate(String targetContent, Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        Matcher m = matcher(targetContent);
        while (m.find()) {
            String param = m.group();
            String value = params.get(param.substring(2, param.length() - 2));
            m.appendReplacement(sb, value == null ? "" : value);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 提取所有模板字符串
     *
     * @param targetContent 操作的字符串
     * @return 提取出的字符串 List<String>
     */
    public static List<String> extractAllTemplate(String targetContent) {
        List<String> strings = new LinkedList<>();
        Matcher m = matcher(targetContent);
        while (m.find()) {
            String param = m.group();
            String value = param.substring(2, param.length() - 2);
            strings.add(value);
        }
        return strings;
    }

    /**
     * 提取模板字符串
     *
     * @param targetContent 操作的字符串
     * @return 提取出的字符串 String
     */
    public static String extractTemplate(String targetContent) {
        String string = null;
        Matcher m = matcher(targetContent);
        while (m.find()) {
            String param = m.group();
            string = param.substring(2, param.length() - 2);
        }
        return string;
    }

    /**
     * 判断字符串是否为模板字符串
     *
     * @param targetContent 操作的字符串
     * @return boolean
     */
    public static boolean isTemplate(String targetContent) {
        return matcher(targetContent).find();
    }

    /**
     * 判断字符串是否为模板字符串
     *
     * @param targetObject 操作的对象
     * @return boolean
     */
    public static boolean isTemplate(Object targetObject) {
        String targetContent = String.valueOf(targetObject);
        return isTemplate(targetContent);
    }

    private static Matcher matcher(String targetContent) {
        return Pattern.compile("\\{\\{(.+?)\\}\\}").matcher(targetContent);
    }
}