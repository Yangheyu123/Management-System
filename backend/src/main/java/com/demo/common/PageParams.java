package com.demo.common;

import java.util.HashMap;
import java.util.Map;

/** 分页参数构造器：把 page/size 转成 offset/limit，附加业务过滤字段。 */
public class PageParams {

    public static Map<String, Object> of(int page, int size) {
        int s = size < 1 ? 10 : Math.min(size, 100);
        int p = Math.max(page, 1);
        Map<String, Object> m = new HashMap<>();
        m.put("offset", (p - 1) * s);
        m.put("size", s);
        m.put("page", p);
        return m;
    }
}
