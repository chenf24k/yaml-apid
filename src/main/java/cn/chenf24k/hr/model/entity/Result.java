package cn.chenf24k.hr.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private boolean success;
    private String title;
    private Object expectValue;
    private Object actualValue;
}