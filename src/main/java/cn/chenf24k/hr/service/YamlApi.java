package cn.chenf24k.hr.service;

import cn.chenf24k.hr.model.entity.YamlObject;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

public class YamlApi {

    private final List<YamlObject> yamlObjects;

    private YamlApi() {
        yamlObjects = new LinkedList<>();
    }

    private static class YamlApiInstance {
        private static final YamlApi singleton = new YamlApi();
    }

    public static YamlApi getInstance() {
        return YamlApiInstance.singleton;
    }

    public void add(String document) {
        YamlObject yamlObject = new YamlObject(document);
        yamlObjects.add(yamlObject);
    }

    public void play() {
        this.yamlObjects.forEach(YamlObject::play);
    }

}
