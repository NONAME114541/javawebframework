package com.cdutetc.nanpo.dto;

import java.util.List;

import lombok.Data;

/**
 * 点单请求 DTO（入参：前端 → 后端）。
 * 第10课：从 pojo 包迁入 dto 包并正名——DTO 专职"传输"，不含业务逻辑。
 * 对应 JSON：{"dishList":[{"dishName":"回锅肉","count":2}]}
 */
@Data
public class OrderRequest {

    private List<DishCount> dishList;

    @Data
    public static class DishCount {
        private String dishName;
        private Integer count;
    }
}
