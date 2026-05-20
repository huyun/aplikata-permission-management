package com.aplikata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuDto {
    private Long id;
    private Long projectId;   // 可为 null
    private Long domainId;     // 可为 null
    
    private Long parentId = 0L;          // 父菜单ID，默认0表示根
    @NotBlank
    private String name;
    private String path;                  // 路由路径
    private String component;              // 组件名
    private String icon;                   // 图标名
    private Integer sortOrder = 0;          // 排序
}