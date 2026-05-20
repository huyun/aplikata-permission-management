package com.aplikata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplikata.dto.MenuDto;
import com.aplikata.entity.DomainEntity;
import com.aplikata.entity.MenuEntity;
import com.aplikata.entity.ProjectEntity;
import com.aplikata.entity.RoleEntity;
import com.aplikata.entity.UserEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.DomainRepository;
import com.aplikata.repository.MenuRepository;
import com.aplikata.repository.ProjectRepository;
import com.aplikata.repository.UserRepository;
import com.aplikata.utils.JsonUtils;

@Service
public class MenuServiceImpl implements MenuService {

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DomainRepository domainRepository;
	@Autowired
	private UserRepository userRepository;

	@Override
	public List<MenuEntity> getCurrentUserMenus() {
		// 1. 获取当前登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new BusinessException(404, "User not found"));

		// 超级管理员：domain 为 null
		if (user.getDomain() == null) {
			// 返回全局菜单（project IS NULL AND domain IS NULL）
			List<MenuEntity> globalMenus = menuRepository.findGlobalMenusOrderBySortOrderAsc();
			return buildMenuTree(globalMenus);
		}

		// 2. 获取用户所属的项目和域（用户属于某个域，域属于某个项目）
		Long domainId = user.getDomain().getId();
		Long projectId = user.getDomain().getProject().getId();

		// 3. 查询用户角色所关联的菜单（需要考虑角色范围）
		List<Long> roleIds = user.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toList());
		if (roleIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 4. 查询这些角色可访问的菜单，并根据用户的项目/域过滤
		// 规则：角色可访问的菜单包括：
		// - 全局菜单（project_id IS NULL）
		// - 项目级菜单（project_id = 用户项目ID 且 domain_id IS NULL）
		// - 域级菜单（domain_id = 用户域ID）
		List<MenuEntity> allMenus = menuRepository.findMenusByRoleIdsAndUserScope(roleIds, projectId, domainId);

		// 5. 构建树形结构
		return buildMenuTree(allMenus);
	}

	// ==================== 公共查询方法 ====================

	@Override
	public List<MenuEntity> getMenuTree(Long projectId, Long domainId) {
		List<MenuEntity> allMenus;
		if (domainId != null) {
			// 指定域：返回该域下菜单 + 项目级菜单 + 全局菜单
			allMenus = menuRepository.findByDomainIdOrProjectIdOrGlobal(domainId, projectId);
		} else if (projectId != null) {
			// 仅指定项目：返回项目级菜单 + 全局菜单
			allMenus = menuRepository.findByProjectIdOrGlobal(projectId);
		} else {
			// 未指定项目：只返回全局菜单
			allMenus = menuRepository.findGlobalMenusOrderBySortOrderAsc();
		}
		return buildMenuTree(allMenus);
	}

	// ==================== 创建菜单 ====================

	@Override
	@Transactional
	public MenuEntity createMenu(MenuDto menuDto) {
		// 1. 处理项目
		ProjectEntity project = null;
		if (menuDto.getProjectId() != null) {
			project = projectRepository.findById(menuDto.getProjectId())
					.orElseThrow(() -> new BusinessException(404, "Project not found"));
		}

		// 2. 处理域
		DomainEntity domain = null;
		if (menuDto.getDomainId() != null) {
			if (project == null) {
				throw new BusinessException(400, "Cannot assign domain without a project");
			}
			domain = domainRepository.findById(menuDto.getDomainId())
					.orElseThrow(() -> new BusinessException(404, "Domain not found"));
			if (!domain.getProject().getId().equals(project.getId())) {
				throw new BusinessException(400, "Domain does not belong to the project");
			}
		}

		// 3. 校验父菜单合法性（如果指定了父菜单）
		if (menuDto.getParentId() != null && menuDto.getParentId() != 0) {
			MenuEntity parent = menuRepository.findById(menuDto.getParentId())
					.orElseThrow(() -> new BusinessException(404, "Parent menu not found"));
			// 检查父子是否属于同一项目/域范围（可选）
			if (!isSameScope(parent, project, domain)) {
				throw new BusinessException(400, "Parent menu does not belong to the same project/domain scope");
			}
		}

		// 4. 唯一性校验
		validateMenuNameUniqueness(menuDto.getName(), project, domain, null);

		// 5. 创建实体
		MenuEntity menu = new MenuEntity();
		BeanUtils.copyProperties(menuDto, menu);
		menu.setProject(project);
		menu.setDomain(domain);
		menu.setParentId(menuDto.getParentId() == null ? 0L : menuDto.getParentId());

		return menuRepository.save(menu);
	}

	// ==================== 更新菜单 ====================

	@Override
	@Transactional
	public MenuEntity updateMenu(Long id, MenuDto menuDto) {
		MenuEntity menu = menuRepository.findById(id).orElseThrow(() -> new BusinessException(404, "Menu not found"));

		// 记录原范围（用于唯一性校验排除自身）
//        ProjectEntity oldProject = menu.getProject();
//        DomainEntity oldDomain = menu.getDomain();

		// 1. 处理项目变更
		ProjectEntity newProject = null;
		if (menuDto.getProjectId() != null) {
			newProject = projectRepository.findById(menuDto.getProjectId())
					.orElseThrow(() -> new BusinessException(404, "Project not found"));
		}

		// 2. 处理域变更
		DomainEntity newDomain = null;
		if (menuDto.getDomainId() != null) {
			if (newProject == null) {
				throw new BusinessException(400, "Cannot assign domain without a project");
			}
			newDomain = domainRepository.findById(menuDto.getDomainId())
					.orElseThrow(() -> new BusinessException(404, "Domain not found"));
			if (!newDomain.getProject().getId().equals(newProject.getId())) {
				throw new BusinessException(400, "Domain does not belong to the project");
			}
		}

		// 3. 校验父菜单合法性（不能将父菜单设为自己的子菜单，防止循环引用）
		if (menuDto.getParentId() != null && menuDto.getParentId() != 0) {
			if (menuDto.getParentId().equals(id)) {
				throw new BusinessException(400, "Cannot set a menu as its own parent");
			}
			MenuEntity newParent = menuRepository.findById(menuDto.getParentId())
					.orElseThrow(() -> new BusinessException(404, "Parent menu not found"));
			// 检查父子范围一致性
			if (!isSameScope(newParent, newProject, newDomain)) {
				throw new BusinessException(400, "Parent menu does not belong to the same project/domain scope");
			}
			// 检查循环引用：newParent 不能是当前菜单的后代
			if (isDescendant(id, menuDto.getParentId())) {
				throw new BusinessException(400, "Cannot set a descendant as parent");
			}
		}

		// 4. 唯一性校验（排除自身）
		validateMenuNameUniqueness(menuDto.getName(), newProject, newDomain, id);

		// 5. 更新字段
		menu.setProject(newProject);
		menu.setDomain(newDomain);
		menu.setParentId(menuDto.getParentId() == null ? 0L : menuDto.getParentId());
		menu.setName(menuDto.getName());
		menu.setPath(menuDto.getPath());
		menu.setComponent(menuDto.getComponent());
		menu.setIcon(menuDto.getIcon());
		menu.setSortOrder(menuDto.getSortOrder());

		return menuRepository.save(menu);
	}

	// ==================== 删除菜单 ====================

	@Override
	@Transactional
	public void deleteMenu(Long id) {
		MenuEntity menu = menuRepository.findById(id).orElseThrow(() -> new BusinessException(404, "Menu not found"));

		// 检查是否有子菜单
		if (menuRepository.existsByParentId(id)) {
			throw new BusinessException(400, "Cannot delete menu with children");
		}

		// 检查是否被角色关联（可选）
		// if (menuRepository.isAssignedToAnyRole(id)) { ... }

		menuRepository.delete(menu);
	}

	// ==================== 辅助方法 ====================

	/**
	 * 构建菜单树（从平铺列表）
	 */
	private List<MenuEntity> buildMenuTree(List<MenuEntity> flatList) {
		Map<Long, MenuEntity> menuMap = new HashMap<>();
		List<MenuEntity> rootMenus = new ArrayList<>();

		for (MenuEntity menu : flatList) {
			menuMap.put(menu.getId(), menu);
			menu.setChildren(new ArrayList<>());
		}

		for (MenuEntity menu : flatList) {
			if (menu.getParentId() == 0) {
				rootMenus.add(menu);
			} else {
				MenuEntity parent = menuMap.get(menu.getParentId());
				if (parent != null) {
					parent.getChildren().add(menu);
				}
			}
		}
		return rootMenus;
	}

	/**
	 * 检查两个菜单是否属于相同的项目/域范围（用于父子校验）
	 */
	private boolean isSameScope(MenuEntity menu, ProjectEntity targetProject, DomainEntity targetDomain) {
		Long menuProjectId = menu.getProject() == null ? null : menu.getProject().getId();
		Long menuDomainId = menu.getDomain() == null ? null : menu.getDomain().getId();
		Long targetProjectId = targetProject == null ? null : targetProject.getId();
		Long targetDomainId = targetDomain == null ? null : targetDomain.getId();
		return Objects.equals(menuProjectId, targetProjectId) && Objects.equals(menuDomainId, targetDomainId);
	}

	/**
	 * 检查菜单 A 是否是菜单 B 的后代（循环引用检测）
	 */
	private boolean isDescendant(Long ancestorId, Long descendantId) {
		MenuEntity descendant = menuRepository.findById(descendantId).orElse(null);
		while (descendant != null && descendant.getParentId() != 0) {
			if (descendant.getParentId().equals(ancestorId)) {
				return true;
			}
			descendant = menuRepository.findById(descendant.getParentId()).orElse(null);
		}
		return false;
	}

	/**
	 * 唯一性校验：根据菜单所属范围（全局/项目级/域级）检查名称是否重复
	 * 
	 * @param name      菜单名称
	 * @param project   所属项目（可为 null）
	 * @param domain    所属域（可为 null）
	 * @param excludeId 排除的菜单ID（更新时使用）
	 */
	private void validateMenuNameUniqueness(String name, ProjectEntity project, DomainEntity domain, Long excludeId) {
		boolean exists;
		if (project == null) {
			// 全局菜单：名称全局唯一
			exists = menuRepository.existsByNameAndProjectIsNullAndIdNot(name, excludeId);
		} else if (domain == null) {
			// 项目级菜单：名称在项目内唯一
			exists = menuRepository.existsByNameAndProjectIdAndDomainIsNullAndIdNot(name, project.getId(), excludeId);
		} else {
			// 域级菜单：名称在域内唯一
			exists = menuRepository.existsByNameAndDomainIdAndIdNot(name, domain.getId(), excludeId);
		}
		if (exists) {
			throw new BusinessException(409, "Menu name already exists in this scope");
		}
	}

	@Override
	@Transactional
	public void importMenus(Long projectId, Long domainId, String jsonContent) {
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

		// 3. 解析 JSON 数组
		List<Map<String, Object>> jsonMenus = JsonUtils.parseArray(jsonContent); // 自行实现工具类或使用 Jackson
		if (jsonMenus == null || jsonMenus.isEmpty()) {
			throw new BusinessException(400, "No menus found in file");
		}

		// 可选：清空该项目/域下的现有菜单（根据需求决定是否覆盖）
		// 这里选择不清空，而是增量导入，但需要处理重复名称或 id 冲突。
		// 由于旧菜单的 id 可能与现有 id 冲突，建议忽略旧 id，重新生成。

		// 递归构建菜单树并保存
		for (Map<String, Object> jsonMenu : jsonMenus) {
			saveMenuFromJson(jsonMenu, 0L, 1, project, domain);
		}
	}

	@SuppressWarnings("unchecked")
	private void saveMenuFromJson(Map<String, Object> jsonMenu, Long parentId, int level, ProjectEntity project,
			DomainEntity domain) {
		MenuEntity menu = new MenuEntity();
		// 存储原始 ID 到 jsonId 字段
		Object idObj = jsonMenu.get("id");
		if (idObj instanceof Number) {
			menu.setJsonId(((Number) idObj).longValue());
		} else if (idObj instanceof String) {
			try {
				menu.setJsonId(Long.parseLong((String) idObj));
			} catch (NumberFormatException e) {
				// 忽略非数字字符串
			}
		}

		menu.setParentId(parentId);
		// 处理 name（旧 JSON 中可能是 i18n key，直接使用）
		menu.setName((String) jsonMenu.get("name"));
		// 处理 url -> path
		String url = (String) jsonMenu.get("url");
		menu.setPath(url != null ? url : "");
		// 处理 icon
		menu.setIcon((String) jsonMenu.get("icon"));
		// 处理 sort -> sortOrder
		Integer sort = (Integer) jsonMenu.get("sort");
		menu.setSortOrder(sort != null ? sort : 0);
		// 可选：level 字段不映射，因为可以从树形结构推导
		menu.setProject(project);
		menu.setDomain(domain);
		menu.setLevel(level);
		// 保存菜单
		menu = menuRepository.save(menu);

		// 处理 children
		List<Map<String, Object>> children = (List<Map<String, Object>>) jsonMenu.get("children");
		if (children != null && !children.isEmpty()) {
			for (Map<String, Object> child : children) {
				saveMenuFromJson(child, menu.getId(), level + 1, project, domain);
			}
		}
	}
}