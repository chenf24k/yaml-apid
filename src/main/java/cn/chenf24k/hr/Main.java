package cn.chenf24k.hr;

import cn.chenf24k.hr.model.entity.HttpRequest;
import cn.chenf24k.hr.model.entity.Step;
import cn.chenf24k.hr.model.entity.YamlObject;
import cn.chenf24k.hr.service.YamlApi;
import cn.chenf24k.hr.tool.JsonUtil;
import com.jayway.jsonpath.JsonPath;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        YamlApi yamlApi = YamlApi.getInstance();
        // yamlApi.add("facesign/facesign-impl-test.yml");
        yamlApi.add("reqres.in.yml");
        yamlApi.play();
    }
}