package cn.chenf24k.hr.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomResponse {
    private int status;
    private String response;
    private String reason;
    private long duration;
}
