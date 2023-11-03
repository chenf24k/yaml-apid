package cn.chenf24k.hr.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonUtil<T> {

    /**
     * 对象转json字符串
     *
     * @param source 源对象
     * @return String
     */
    public static String toJsonString(Object source) {
        Gson gson = new Gson();
        return gson.toJson(source);
    }

    /**
     * json字符串转对象
     *
     * @param jsonStr json字符串
     * @param clazz   映射对象类型
     * @param <T>     泛型参数
     * @return 泛型
     */
    public static <T> T toObject(String jsonStr, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, clazz);
    }

    /**
     * json格式化输出
     *
     * @param jsonStr json字符串
     * @return String
     */
    public static String pretty(String jsonStr) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(jsonStr).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }

    /**
     * 读取json文件
     *
     * @param pathname 默认从resources目录下读取
     * @return json string
     */
    public static String read(String pathname) {
        String jsonStr = null;
        StringBuffer buf = null;
        try {
            InputStream inputStream = JsonUtil.class.getResourceAsStream(pathname);
            Reader reader = new InputStreamReader(inputStream);
            int ch = 0;
            buf = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                buf.append((char) ch);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jsonStr = buf.toString();
        return jsonStr;
    }
}
