package com.demo.config;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

/**
 * 让 MyBatis 返回的 Map 结果列名自动从下划线转驼峰
 * （原生 mapUnderscoreToCamelCase 只对 Bean 生效，对 Map 不生效）。
 */
public class CamelCaseMapWrapper extends MapWrapper {

    public CamelCaseMapWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return underscoreToCamelCase(name);
    }

    private String underscoreToCamelCase(String name) {
        if (name == null) return null;
        StringBuilder sb = new StringBuilder(name.length());
        boolean upper = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
