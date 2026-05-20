package com.aplikata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplikata.dto.RoleDto;
import com.aplikata.entity.DomainEntity;
import com.aplikata.entity.MenuEntity;
import com.aplikata.entity.ProjectEntity;
import com.aplikata.entity.RoleEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.DomainRepository;
import com.aplikata.repository.MenuRepository;
import com.aplikata.repository.ProjectRepository;
import com.aplikata.repository.RoleRepository;
import com.aplikata.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DomainRepository domainRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MenuRepository menuRepository;

	private RoleDto convertToDto(RoleEntity role) {
		RoleDto dto = new RoleDto();
		BeanUtils.copyProperties(role, dto);
		// 如果需要返回关联菜单ID列表，可以在这里设置
		if (role.getMenus() != null) {
			dto.setMenuIds(role.getMenus().stream().map(MenuEntity::getId).collect(Collectors.toList()));
		}
		return dto;
	}

	@Override
	public List<RoleDto> listRoles(Long projectId, Long domainId) {
		List<RoleEntity> roles;
		if (domainId != null) {
			// 按域查询
			roles = roleRepository.findByDomainId(domainId);
		} else if (projectId != null) {
			// 按项目查询：返回该项目下所有角色（项目级 + 域级）
			roles = roleRepository.findAllByProjectId(projectId);
		} else {
			// 返回所有角色（包括全局、项目级、域级）
			roles = roleRepository.findGlobalRoles();
		}
		return roles.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Override
	public RoleDto getRoleById(Long id) {
		RoleEntity roleEntity = roleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "Role not found"));
		return convertToDto(roleEntity);
	}

	@Override
	@Transactional
	public RoleDto createRole(RoleDto dto) {
		// 1. 处理项目和域
		ProjectEntity project = null;
		if (dto.getProjectId() != null) {
			project = projectRepository.findById(dto.getProjectId())
					.orElseThrow(() -> new BusinessException(404, "Project not found"));
		}

		DomainEntity domain = null;
		if (dto.getDomainId() != null) {
			if (project == null) {
				throw new BusinessException(400, "Cannot assign domain without a project");
			}
			domain = domainRepository.findById(dto.getDomainId())
					.orElseThrow(() -> new BusinessException(404, "Domain not found"));
			if (!domain.getProject().getId().equals(project.getId())) {
				throw new BusinessException(400, "Domain does not belong to the project");
			}
		}

		// 2. 唯一性校验
		if (project == null) {
			// 全局角色：名称全局唯一
			if (roleRepository.existsByName(dto.getName())) {
				throw new BusinessException(409, "Global role name already exists");
			}
		} else if (domain == null) {
			// 项目级角色：名称在项目内唯一
			if (roleRepository.existsByProjectIdAndName(project.getId(), dto.getName())) {
				throw new BusinessException(409, "Role name already exists in this project");
			}
		} else {
			// 域级角色：名称在域内唯一
			if (roleRepository.existsByDomainIdAndName(domain.getId(), dto.getName())) {
				throw new BusinessException(409, "Role name already exists in this domain");
			}
		}

		// 3. 创建角色
		RoleEntity role = new RoleEntity();
		role.setProject(project);
		role.setDomain(domain);
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		role = roleRepository.save(role);
		return convertToDto(role);
	}

	// ==================== 更新角色 ====================
	@Override
	@Transactional
	public RoleDto updateRole(Long id, RoleDto dto) {
		RoleEntity role = roleRepository.findById(id).orElseThrow(() -> new BusinessException(404, "Role not found"));

		// 1. 处理项目和域变更
		ProjectEntity newProject = null;
		if (dto.getProjectId() != null) {
			newProject = projectRepository.findById(dto.getProjectId())
					.orElseThrow(() -> new BusinessException(404, "Project not found"));
		}

		DomainEntity newDomain = null;
		if (dto.getDomainId() != null) {
			if (newProject == null) {
				throw new BusinessException(400, "Cannot assign domain without a project");
			}
			newDomain = domainRepository.findById(dto.getDomainId())
					.orElseThrow(() -> new BusinessException(404, "Domain not found"));
			if (!newDomain.getProject().getId().equals(newProject.getId())) {
				throw new BusinessException(400, "Domain does not belong to the project");
			}
		}

		// 2. 唯一性校验（排除自身）
		boolean nameChanged = !role.getName().equals(dto.getName());
		boolean projectChanged = (role.getProject() == null && newProject != null)
				|| (role.getProject() != null && newProject == null) || (role.getProject() != null && newProject != null
						&& !role.getProject().getId().equals(newProject.getId()));
		boolean domainChanged = (role.getDomain() == null && newDomain != null)
				|| (role.getDomain() != null && newDomain == null) || (role.getDomain() != null && newDomain != null
						&& !role.getDomain().getId().equals(newDomain.getId()));

		if (nameChanged || projectChanged || domainChanged) {
			if (newProject == null) {
				// 变更为全局角色
				if (roleRepository.existsByName(dto.getName())) {
					throw new BusinessException(409, "Global role name already exists");
				}
			} else if (newDomain == null) {
				// 变更为项目级角色
				if (roleRepository.existsByProjectIdAndName(newProject.getId(), dto.getName())) {
					throw new BusinessException(409, "Role name already exists in this project");
				}
			} else {
				// 变更为域级角色
				if (roleRepository.existsByDomainIdAndName(newDomain.getId(), dto.getName())) {
					throw new BusinessException(409, "Role name already exists in this domain");
				}
			}
		}

		// 3. 更新字段
		role.setProject(newProject);
		role.setDomain(newDomain);
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		role = roleRepository.save(role);
		return convertToDto(role);
	}

	// ==================== 删除角色 ====================
	@Override
	@Transactional
	public void deleteRole(Long id) {
		RoleEntity role = roleRepository.findById(id).orElseThrow(() -> new BusinessException(404, "Role not found"));

		// 检查是否有用户关联该角色
		if (userRepository.countUsersByRoleId(id) > 0) {
			throw new BusinessException(400, "Cannot delete role with assigned users");
		}

		// 删除角色-菜单关联（中间表）
		entityManager.createNativeQuery("DELETE FROM _role_menu WHERE role_id = :roleId").setParameter("roleId", id)
				.executeUpdate();

		// 删除角色
		roleRepository.delete(role);
	}

	@Override
	public List<Long> getAssignedMenuIds(Long roleId) {
		RoleEntity roleEntity = roleRepository.findById(roleId)
				.orElseThrow(() -> new BusinessException(404, "Role not found"));
		return roleEntity.getMenus().stream().map(MenuEntity::getId).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void assignMenus(Long roleId, List<Long> menuIds) {
		// 1. 校验角色存在
		if (!roleRepository.existsById(roleId)) {
			throw new BusinessException(404, "Role not found");
		}

		// 2. 删除旧关联
		entityManager.createNativeQuery("DELETE FROM _role_menu WHERE role_id = :roleId").setParameter("roleId", roleId)
				.executeUpdate();

		// 3. 批量插入新关联
		if (menuIds != null && !menuIds.isEmpty()) {
			// 构建批量插入 SQL
			StringBuilder sql = new StringBuilder("INSERT INTO _role_menu (role_id, menu_id) VALUES ");
			for (int i = 0; i < menuIds.size(); i++) {
				if (i > 0)
					sql.append(",");
				sql.append("(:roleId, :menuId").append(i).append(")");
			}
			Query query = entityManager.createNativeQuery(sql.toString());
			query.setParameter("roleId", roleId);
			for (int i = 0; i < menuIds.size(); i++) {
				query.setParameter("menuId" + i, menuIds.get(i));
			}
			query.executeUpdate();
		}
	}

	@Override
	@Transactional
	public void importRoles(Long projectId, Long domainId, String jsonContent) {
		// 1. 验证 project 是否存在
		ProjectEntity project = projectRepository.findById(projectId)
				.orElseThrow(() -> new BusinessException(404, "Project not found"));

		// 2. 如果 domainId 不为空，验证 domain 属于该项目
		DomainEntity domain = null;
		if (domainId != null) {
			domain = domainRepository.findById(domainId)
					.orElseThrow(() -> new BusinessException(404, "Domain not found"));
			if (!domain.getProject().getId().equals(projectId)) {
				throw new BusinessException(400, "Domain does not belong to the project");
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root;
		try {
			root = mapper.readTree(jsonContent);
		} catch (Exception e) {
			throw new BusinessException(400, "Invalid JSON format");
		}
		if (!root.isArray()) {
			throw new BusinessException(400, "JSON root must be an array");
		}

		for (JsonNode node : root) {
			String name = node.get("name").asText();
			String protectUrl = node.has("protectUrl") ? node.get("protectUrl").asText() : "";
			int grade = node.has("grade") ? node.get("grade").asInt() : 0;

			// 解析菜单旧 ID 列表
			List<Long> oldMenuIds = new ArrayList<>();
			JsonNode menuIdsNode = node.get("menuIds");
			if (menuIdsNode != null && menuIdsNode.isArray()) {
				for (JsonNode idNode : menuIdsNode) {
					oldMenuIds.add(idNode.asLong());
				}
			}

			// 根据旧菜单 ID 查找实际菜单（必须属于同一个 project/domain）
			List<MenuEntity> menus = new ArrayList<>();
			if (!oldMenuIds.isEmpty()) {
				List<MenuEntity> existingMenus = menuRepository.findByJsonIdInAndProjectIdAndDomainId(oldMenuIds, projectId, domainId);
				menus = existingMenus;
//				// 注意：需要限定查询条件：json_id IN (:oldMenuIds) AND project_id = :projectId AND
//				// domain_id = :domainId（domain 可能为 null）
//				menus = menuRepository.findByJsonIdInAndProjectIdAndDomainId(oldMenuIds, projectId, domainId);
//				if (menus.size() != oldMenuIds.size()) {
//					// 可选：打印缺失的菜单 ID
//					System.err.println("Some menu IDs not found: " + oldMenuIds);
//					// 根据业务决定是抛异常还是继续（这里抛异常）
//					throw new BusinessException(400,
//							"Some menu IDs in role JSON do not exist in current project/domain");
				//}
			}
			
			if (menus.isEmpty()) {
		        continue;
		    }
			// 创建角色（根据需求决定是否先删除已有）
			RoleEntity role = new RoleEntity();
			role.setName(name);
			role.setProject(project);
			role.setDomain(domain);
			role.setMenus(menus);
			role.setGrade(grade);
			role.setProtectUrl(protectUrl);
			role = roleRepository.save(role);
		}
	}
}