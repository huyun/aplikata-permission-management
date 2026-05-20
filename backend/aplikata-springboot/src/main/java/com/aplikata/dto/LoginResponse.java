package com.aplikata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDto user;  // 你可以使用已有的 UserDto 或单独定义一个

}