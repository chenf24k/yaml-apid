package cn.chenf24k.hr.tool;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BaseTest {

    @Test
    public void forEach() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.forEach((k, v) -> System.out.println(v));
    }
}
