package com.cdutetc.nanpo.pojo;

import java.util.List;

import lombok.Data;

/**
 * 点单请求：前端提交的 JSON 会被自动装配成这个对象。
 * 对应 JSON 形如：{"dishList":[{"dishName":"回锅肉","count":2}]}
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
