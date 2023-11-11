package cn.chenf24k.hr.context;

import cn.chenf24k.hr.remake.CustomMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 全局上下文
 */
@Data
@Slf4j
public final class GlobalContext {

    private static CustomMap vars;

    static {
        vars = new CustomMap();
    }

    private GlobalContext() {
    }

    public CustomMap getVars() {
        return vars;
    }

    private static class GlobalContextInstance {
        private static final GlobalContext singleton = new GlobalContext();
    }

    public static GlobalContext getInstance() {
        return GlobalContextInstance.singleton;
    }

    public void put(String key, Object value) {
        log.info("GlobalContext add key: {}, value: {}", key, value);
        vars.put(key, value);
    }

    public void putAll(Map<? extends String, ?> m) {
        log.info("GlobalContext add all: {}", m.toString());
        vars.putAll(m);
    }

}
