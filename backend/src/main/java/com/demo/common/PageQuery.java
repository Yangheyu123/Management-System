package com.demo.common;

import lombok.Data;

@Data
public class PageQuery {
    private Integer page = 1;
    private Integer size = 10;

    public int getOffset() {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        return (p - 1) * s;
    }

    public int safeSize() {
        int s = size == null || size < 1 ? 10 : size;
        return Math.min(s, 100);
    }

    public int safePage() {
        return page == null || page < 1 ? 1 : page;
    }
}
