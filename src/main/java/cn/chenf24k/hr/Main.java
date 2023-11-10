package cn.chenf24k.hr;

import cn.chenf24k.hr.service.YamlApi;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.setProperty("org.apache.hcatalog5.level", "INFO");
        YamlApi yamlApi = YamlApi.getInstance();
        // yamlApi.add("facesign/facesign-impl-test.yml");
//        yamlApi.add("http_request.yml");
//        yamlApi.add("reqres.in.yml");
        yamlApi.add("indicator/upload.yml");
        yamlApi.play();
    }
}