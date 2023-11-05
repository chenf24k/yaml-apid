package cn.chenf24k.hr.context;

import lombok.Data;

@Data
public final class GlobalContext {
    private Context vars;

    private GlobalContext() {
        vars = new Context();
    }

    private static class GlobalContextInstance {
        private static final GlobalContext singleton = new GlobalContext();
    }

    public static GlobalContext getInstance() {
        return GlobalContextInstance.singleton;
    }

}
