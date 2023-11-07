package cn.chenf24k.hr.tool;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.RandomUtil;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseTest {

    @Test
    public void forEach() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.forEach((k, v) -> System.out.println(v));

        String a = "222";
        String string = Objects.toString(a);
        Integer integer = new Integer(222);
        String string1 = Objects.toString(integer);
        System.out.println(string.equals(string1));
    }
}
