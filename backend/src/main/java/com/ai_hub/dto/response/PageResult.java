package com.ai_hub.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 分页响应结果类
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size; // 计算总页数
    }

    /**
     * 构建分页响应
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param size    每页大小
     * @param current 当前页码
     * @param <T>     数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }
}
