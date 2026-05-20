package com.aplikata.service;

import com.aplikata.dto.UserCreateDto;
import com.aplikata.dto.UserDto;
import java.util.List;

public interface UserService {

    /**
     * 查询用户列表（支持按项目或域过滤）
     * @param projectId 项目ID（可选）
     * @param domainId 域ID（可选）
     * @return 用户DTO列表
     */
    List<UserDto> listUsers(Long projectId, Long domainId);

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户DTO
     */
    UserDto getUserById(Long id);

    /**
     * 创建用户
     * @param createDto 创建用户DTO
     * @return 创建后的用户DTO
     */
    UserDto createUser(UserCreateDto createDto);

    /**
     * 更新用户
     * @param id 用户ID
     * @param updateDto 更新用户DTO
     * @return 更新后的用户DTO
     */
    UserDto updateUser(Long id, UserCreateDto updateDto);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 获取用户已分配的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 分配角色给用户
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);
}