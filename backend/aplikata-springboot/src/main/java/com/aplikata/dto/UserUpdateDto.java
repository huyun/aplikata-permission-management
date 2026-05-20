package com.aplikata.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // 可以添加其他可修改字段，如 phone, avatar 等
}
