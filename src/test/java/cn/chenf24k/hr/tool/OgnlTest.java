package cn.chenf24k.hr.tool;

import ognl.Ognl;
import ognl.OgnlException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OgnlTest {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        map.put("response", "ababab");
        map.put("request", "aaa");
        Object value = null;
        try {
            value = Ognl.getValue("response", map);
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }
        System.out.println(value);
    }


    static class P {
        
    }

}
