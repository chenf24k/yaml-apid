package cn.chenf24k.hr.context;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

/**
 * 上下文对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Context extends HashMap<String, Object> {

}
