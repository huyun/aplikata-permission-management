package com.aplikata.dto;

import lombok.Data;

@Data
public class MenuImportDto {
    private Long projectId;   // 必填
    private Long domainId;    // 可选，为空表示项目级菜单
    private String jsonContent; // 文件内容字符串
}
