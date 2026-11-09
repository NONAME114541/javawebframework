package com.cdutetc.nanpo.vo;

import java.util.List;

/** 列表响应保留分页元数据，避免向 Controller 暴露 Spring Data Page。 */
public record MenuPageVO(List<MenuVO> records, int page, int size,
        long totalElements, int totalPages) {
}
