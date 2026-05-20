package com.aplikata.controller;import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.multipart.MultipartFile;

import com.aplikata.dto.ApiResponse;
import com.aplikata.dto.RoleDto;
import com.aplikata.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表（支持按项目和域过滤）
     */
    @GetMapping
    public ApiResponse<List<RoleDto>> listRoles(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long domainId) {
        List<RoleDto> roles = roleService.listRoles(projectId, domainId);
        return ApiResponse.success(roles);
    }

    /**
     * 获取单个角色详情
     */
    @GetMapping("/{id}")
    public ApiResponse<RoleDto> getRoleById(@PathVariable Long id) {
        RoleDto role = roleService.getRoleById(id);
        return ApiResponse.success(role);
    }

    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleDto created = roleService.createRole(roleDto);
        return ApiResponse.success(created);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ApiResponse<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        RoleDto updated = roleService.updateRole(id, roleDto);
        return ApiResponse.success(updated);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success("Role deleted successfully");
    }

    /**
     * 获取角色已分配的菜单ID列表
     */
    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        List<Long> menuIds = roleService.getAssignedMenuIds(id);
        return ApiResponse.success(menuIds);
    }

    /**
     * 分配菜单给角色
     */
    @PutMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResponse.success("Menus assigned successfully");
    }
    
    @PostMapping("/import")
    public ApiResponse<Void> importRoles(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long domainId,
            @RequestParam("file") MultipartFile file) throws IOException {

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        roleService.importRoles(projectId, domainId, content);
        return ApiResponse.success("Roles imported successfully");
    }
}