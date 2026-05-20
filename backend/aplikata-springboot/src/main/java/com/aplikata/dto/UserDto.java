package com.aplikata.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserDto {
	private Long id;
	private String username;
	private String email;
	private Boolean blocked;
	private Long projectId;
	private Long domainId;
    private List<Long> roleIds;        // 用于接收/返回角色ID列表
    private List<String> roleNames;    // 可选，用于显示
}
