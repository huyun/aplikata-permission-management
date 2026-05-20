package com.aplikata.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aplikata.dto.ApiResponse;
import com.aplikata.dto.UserCreateDto;
import com.aplikata.dto.UserDto;
import com.aplikata.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（支持按项目或域过滤）
     */
    @GetMapping
    public ApiResponse<List<UserDto>> listUsers(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long domainId) {
        return ApiResponse.success(userService.listUsers(projectId, domainId));
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserDto> createUser(@Valid @RequestBody UserCreateDto createDto) {
        return ApiResponse.success(userService.createUser(createDto));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateDto updateDto) {
        return ApiResponse.success(userService.updateUser(id, updateDto));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully");
    }

    /**
     * 获取用户已分配的角色ID列表
     */
    @GetMapping("/{id}/roles")
    public ApiResponse<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserRoleIds(id));
    }

    /**
     * 分配角色给用户
     */
    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.success("Roles assigned successfully");
    }
}