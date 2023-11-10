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
     * 模板字符串替换
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
     * 模板字符串提取
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
     * 模板字符串提取
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

    private static Matcher matcher(String targetContent) {
        return Pattern.compile("\\{\\{(.+?)\\}\\}").matcher(targetContent);
    }
}