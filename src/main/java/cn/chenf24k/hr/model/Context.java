package cn.chenf24k.hr.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public final class Context {
    public static final Map<String, String> vars = new HashMap<>();
}
