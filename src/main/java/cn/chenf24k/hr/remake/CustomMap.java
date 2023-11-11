package cn.chenf24k.hr.remake;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomMap extends LinkedHashMap<String, Object> {

}
