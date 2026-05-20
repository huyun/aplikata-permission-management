package com.aplikata.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDto {
	@NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private String password;   // 更新时可选

    private Long projectId;    // 可为 null（超级管理员）
    private Long domainId;     // 若 projectId 非空，则 domainId 必填
}
