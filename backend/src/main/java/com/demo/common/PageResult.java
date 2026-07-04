package com.demo.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private int page;
    private int size;
    private long total;
    private int pages;
    private List<T> list;

    public PageResult() {}

    public PageResult(int page, int size, long total, List<T> list) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.pages = size == 0 ? 0 : (int) ((total + size - 1) / size);
        this.list = list;
    }
}
