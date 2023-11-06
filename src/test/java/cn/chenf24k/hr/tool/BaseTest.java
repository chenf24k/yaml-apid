package cn.chenf24k.hr.tool;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.RandomUtil;
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
