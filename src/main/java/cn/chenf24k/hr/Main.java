package cn.chenf24k.hr;

import cn.chenf24k.hr.model.entity.HttpRequest;
import cn.chenf24k.hr.model.entity.Step;
import cn.chenf24k.hr.model.entity.YamlObject;
import cn.chenf24k.hr.tool.JsonUtil;
import com.jayway.jsonpath.JsonPath;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.setProperty("com.jayway.jsonpath", "warn");
        YamlObject yamlObject = new YamlObject("http_request.yml");
        for (Step step : yamlObject.getSteps()) {
            HttpRequest httpRequest = step.getRequest();
            System.out.println("------  " + step.getTitle() + "    ------");
            String response = httpRequest.request();
//            System.out.println(JsonUtil.pretty(response));
            step.handleVars(response);
        }
    }
}