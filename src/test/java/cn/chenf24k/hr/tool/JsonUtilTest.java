package cn.chenf24k.hr.tool;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * @author
 * @version 1.0.0
 * @date 2023-11-12
 */
public class JsonUtilTest {

    @Test
    public void toObject() {
        String json = "{\"code\":200,\"errCode\":null,\"message\":\"success\",\"data\":{\"id\":1,\"name\":\"xiaotiantian\",\"guoxian\":{\"name\":\"hhhhhh\",\"age\":18}}}";
        Object object = JsonUtil.toObject(json, Object.class);

        System.out.println(object);
    }
}