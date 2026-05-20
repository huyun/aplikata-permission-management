package com.aplikata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static ApiResponse<?> unauthorized() {
        return new ApiResponse<>(401, "Unauthorized", null);
    }

    public static ApiResponse<?> forbidden() {
        return new ApiResponse<>(403, "Forbidden", null);
    }
    
    // 成功响应（无数据）
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    // 成功响应（有数据）
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    // 成功响应（自定义消息和数据）
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // 错误响应（可附带数据，但通常用于错误时附带额外信息）
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
