package cn.chenf24k.hr;

import cn.chenf24k.hr.service.YamlApi;

public class Main {

    public static void main(String[] args) {
        YamlApi yamlApi = YamlApi.getInstance();
        // yamlApi.add("facesign/facesign-impl-test.yml");
        // yamlApi.add("http_request.yml");
        // yamlApi.add("reqres.in.yml");
        yamlApi.add("demo.yml");
        // yamlApi.add("indicator/upload.yml");
        yamlApi.play();
    }
}