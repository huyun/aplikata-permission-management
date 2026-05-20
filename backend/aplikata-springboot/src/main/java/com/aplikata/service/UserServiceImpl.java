package com.aplikata.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplikata.dto.UserCreateDto;
import com.aplikata.dto.UserDto;
import com.aplikata.entity.DomainEntity;
import com.aplikata.entity.ProjectEntity;
import com.aplikata.entity.RoleEntity;
import com.aplikata.entity.UserEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.DomainRepository;
import com.aplikata.repository.ProjectRepository;
import com.aplikata.repository.RoleRepository;
import com.aplikata.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto convertToDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProjectId(user.getProjectId());
        dto.setDomainId(user.getDomainId());
        if (user.getRoles() != null) {
            dto.setRoleNames(user.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    public List<UserDto> listUsers(Long projectId, Long domainId) {
        List<UserEntity> users;
        if (domainId != null) {
            users = userRepository.findByDomainId(domainId);
        } else if (projectId != null) {
            users = userRepository.findByProjectId(projectId);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
        return convertToDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        // 1. 校验项目与域的组合
        if (createDto.getProjectId() == null && createDto.getDomainId() == null) {
            // 超级管理员
            if (userRepository.existsSuperAdminByUsername(createDto.getUsername())) {
                throw new BusinessException(409, "Super admin username already exists");
            }
            UserEntity user = new UserEntity();
            user.setUsername(createDto.getUsername());
            user.setEmail(createDto.getEmail());
            user.setPassword(passwordEncoder.encode(createDto.getPassword()));
            user.setProject(null);
            user.setDomain(null);
            user = userRepository.save(user);
            return convertToDto(user);
        } else if (createDto.getProjectId() != null && createDto.getDomainId() != null) {
            // 普通用户
            ProjectEntity project = projectRepository.findById(createDto.getProjectId())
                    .orElseThrow(() -> new BusinessException(404, "Project not found"));
            DomainEntity domain = domainRepository.findById(createDto.getDomainId())
                    .orElseThrow(() -> new BusinessException(404, "Domain not found"));
            if (!domain.getProject().getId().equals(project.getId())) {
                throw new BusinessException(400, "Domain does not belong to the project");
            }
            if (userRepository.existsByDomainIdAndUsername(domain.getId(), createDto.getUsername())) {
                throw new BusinessException(409, "Username already exists in this domain");
            }
            UserEntity user = new UserEntity();
            user.setUsername(createDto.getUsername());
            user.setEmail(createDto.getEmail());
            user.setPassword(passwordEncoder.encode(createDto.getPassword()));
            user.setProject(project);
            user.setDomain(domain);
            user = userRepository.save(user);
            return convertToDto(user);
        } else {
            throw new BusinessException(400, "Invalid user type: either both project and domain are null, or both are non-null");
        }
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserCreateDto updateDto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "User not found"));

        // 记录原类型
        boolean wasSuperAdmin = (user.getProject() == null && user.getDomain() == null);

        // 新类型
        boolean isSuperAdmin = (updateDto.getProjectId() == null && updateDto.getDomainId() == null);
        boolean isNormalUser = (updateDto.getProjectId() != null && updateDto.getDomainId() != null);

        if (isSuperAdmin) {
            // 变为超级管理员
            if (!wasSuperAdmin && userRepository.existsSuperAdminByUsername(updateDto.getUsername())) {
                throw new BusinessException(409, "Super admin username already exists");
            }
            user.setProject(null);
            user.setDomain(null);
        } else if (isNormalUser) {
            // 变为普通用户
            ProjectEntity project = projectRepository.findById(updateDto.getProjectId())
                    .orElseThrow(() -> new BusinessException(404, "Project not found"));
            DomainEntity domain = domainRepository.findById(updateDto.getDomainId())
                    .orElseThrow(() -> new BusinessException(404, "Domain not found"));
            if (!domain.getProject().getId().equals(project.getId())) {
                throw new BusinessException(400, "Domain does not belong to the project");
            }
            // 唯一性校验（排除自身）
            if (!domain.getId().equals(user.getDomainId()) &&
                    userRepository.existsByDomainIdAndUsername(domain.getId(), updateDto.getUsername())) {
                throw new BusinessException(409, "Username already exists in this domain");
            }
            user.setProject(project);
            user.setDomain(domain);
        } else {
            throw new BusinessException(400, "Invalid user type: either both project and domain are null, or both are non-null");
        }

        // 更新公共字段
        if (!user.getUsername().equals(updateDto.getUsername())) {
            // 如果用户名改变，需要在新类型下校验唯一性（上面已经部分校验，此处补充分情况）
            if (isSuperAdmin && userRepository.existsSuperAdminByUsername(updateDto.getUsername())) {
                throw new BusinessException(409, "Super admin username already exists");
            }
            if (isNormalUser && userRepository.existsByDomainIdAndUsername(user.getDomainId(), updateDto.getUsername())) {
                throw new BusinessException(409, "Username already exists in this domain");
            }
        }
        user.setUsername(updateDto.getUsername());
        user.setEmail(updateDto.getEmail());
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
        return user.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
        // 清除原有角色
        user.getRoles().clear();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<RoleEntity> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new BusinessException(400, "Some roles do not exist");
            }
            user.getRoles().addAll(roles);
        }
        userRepository.save(user);
    }
}