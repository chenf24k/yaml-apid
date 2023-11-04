package cn.chenf24k.hr.tool;

import cn.chenf24k.hr.model.entity.Result;

import java.util.List;

public class PrintUtil {

    public static void printError(String text) {
        System.out.println("\033[31m" + text + "\033[0m");
    }

    public static void printSuccess(String text) {
        System.out.println("\033[32m" + text + "\033[0m");
    }

    public static void printWarn(String text) {
        System.out.println("\033[33m" + text + "\033[0m");
    }

    public static void printResult(List<Result> results) {
        results.forEach(result -> {
            boolean isSuccess = result.isSuccess();
            if (isSuccess) {
                System.out.println(result.getTitle()
                        + " => "
                        + "\033[32mpassed\033[0m"
                        + " actual: " + result.getActualValue()
                        + " expect: " + result.getExpectValue()
                );
            } else {
                System.out.println(result.getTitle()
                        + " => "
                        + "\033[31mfailed\033[0m"
                        + " actual: \033[31m" + result.getActualValue() + "\033[0m"
                        + " expect: \033[31m" + result.getExpectValue() + "\033[0m"
                );
            }

        });
    }

}
