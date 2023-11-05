package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
public class YamlObject implements Serializable {
    private String title;
    private Map<String, String> vars;
    private Step[] steps;

    private static final Yaml yaml = new Yaml();

    public YamlObject(String document) {
        YamlObject yamlObject = yaml.loadAs(YamlObject.class.getClassLoader().getResourceAsStream(document), YamlObject.class);
        this.title = yamlObject.getTitle();
        this.vars = yamlObject.getVars();
        GlobalContext.getInstance().getVars().putAll(vars);
        this.steps = yamlObject.getSteps();
    }

    public void play() {
        System.out.println("[------   " + this.getTitle() + "   ------]");
        for (Step step : this.getSteps())
            step.run();
    }
}
