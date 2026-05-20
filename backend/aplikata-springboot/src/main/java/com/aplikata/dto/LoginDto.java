package com.aplikata.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
	@NotBlank(message = "User name is required!")
	private String username;

	@NotBlank(message = "Password is required!")
	@Length(min = 4)
//	@JsonIgnore   // 添加此注解，在返回 JSON 时忽略 password 字段
	private String password;
}

