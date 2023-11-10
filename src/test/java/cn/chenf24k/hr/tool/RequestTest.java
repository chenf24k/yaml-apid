package cn.chenf24k.hr.tool;

import org.junit.Test;

import java.io.File;

/**
 * @author chenfeng
 * @version 1.0.0
 * @since 2023/11/10 10:47
 */
public class RequestTest {

    @Test
    public void testGet() {
        File file = new File("C:/Users/99493/Downloads/Screenshot_20230616_110331.jpg");
        System.out.println(file.getName());
    }
}
