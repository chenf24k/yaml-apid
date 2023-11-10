package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import ognl.*;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.util.LinkedHashMap;
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
        this.steps = yamlObject.getSteps();
        preHandle();
    }

    /**
     * 处理全部变量
     */
    public void preHandle() {
        Map<String, Object> globalVars = new LinkedHashMap<>();
        if (this.getVars() != null && !this.getVars().isEmpty())
            this.getVars().forEach((varName, varValue) -> {
                Object handleValue = null;
                if (TemplateProcess.isTemplate(varValue)) {
                    String extracted = TemplateProcess.extractTemplate(varValue);
                    try {
                        Object expression = Ognl.parseExpression(extracted);
                        handleValue = Ognl.getValue(expression, this);
                    } catch (OgnlException ignored) {

                    }
                } else {
                    handleValue = varValue;
                }
                globalVars.put(varName, handleValue);
            });

        GlobalContext.getInstance().getVars().putAll(globalVars);
    }

    public void play() {
        System.out.println("[------   " + this.getTitle() + "   ------]");
        for (Step step : this.getSteps())
            step.run();
    }

}
