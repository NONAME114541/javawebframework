package com.cdutetc.nanpo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单实体——数据库表 t_menu 在 Java 里的"影子"：一个字段对应一列。
 * 第8课：菜谱从 WaiterController 的静态 Map 搬进数据库，改价不用再改代码发版。
 * 目录规范：实体（表）放 entity 包，操作表的手（Repository）放 dao 包。
 */
@Entity
@Table(name = "t_menu")
@Data
@NoArgsConstructor      // JPA 要求：反射创建对象时需要无参构造
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 主键自增
    private Long id;

    @Column(nullable = false)
    private String dishName;

    @Column(nullable = false)
    private BigDecimal price;   // 钱一律 BigDecimal——第2课用 Integer 是教学起点的妥协，本次一并整改

    /** 菜品照片访问路径（如 /files/xxx.png）。第13课新增——ddl-auto:update 启动自动加列。
     *  第19课踩坑整改：本地路径很短，但 MinIO 预签名 URL 长达数百字符，
     *  默认 VARCHAR(255) 存不下（Data too long for column 'image_url'）——加长到 1024。
     *  注意 ddl-auto:update 不会修改已存在列的长度，存量库需手动 ALTER（真实运维课题） */
    @Column(length = 1024)
    private String imageUrl;

    // 不用 @AllArgsConstructor：它会把 id 也算进去生成三参构造器，初始化用不上；
    // 手写两参构造器，id 留给数据库自增
    public Menu(String dishName, BigDecimal price) {
        this.dishName = dishName;
        this.price = price;
    }
}
