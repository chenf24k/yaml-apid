package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.model.Context;
import cn.chenf24k.hr.model.enums.PROTOCOL;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class Step {
    private String title;
    private PROTOCOL protocol;
    private HttpRequest request;
    private Bind bind;
    private Map<String, Object> expect;

    public void handleVars(String response) {
        if (this.bind != null) {
            Map<String, String> bindVars = this.bind.getVars();
            if (!bindVars.isEmpty()) {
                Set<String> keySet = bindVars.keySet();
                for (String key : keySet) {
                    String v = bindVars.get(key);
                    String target = JsonPath.read(response, "$." + v);
                    bindVars.put(key, target);
                }
                Context.vars.putAll(bindVars);
            }
        }

    }

}
