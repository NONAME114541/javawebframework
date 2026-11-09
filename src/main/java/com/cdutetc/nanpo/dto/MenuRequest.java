package com.cdutetc.nanpo.dto;

import java.math.BigDecimal;
import lombok.Data;

/** 菜品创建/修改请求体。 */
@Data
public class MenuRequest {
    private String dishName;
    private BigDecimal price;
    private String imageUrl;
}
