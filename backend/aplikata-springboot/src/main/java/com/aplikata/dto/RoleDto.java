package com.aplikata.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

@Data
public class RoleDto {
    private Long id;
    
    @NotBlank(message = "Name is required!")
    private String name;
    
    private Long projectId;    // 所属项目
    private Long domainId;     
    // 可选：返回该角色关联的菜单ID列表
    private List<Long> menuIds;
    
    private String description;
}