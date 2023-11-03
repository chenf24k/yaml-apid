package cn.chenf24k.hr.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Bind {
    private Map<String, String> vars;
}
