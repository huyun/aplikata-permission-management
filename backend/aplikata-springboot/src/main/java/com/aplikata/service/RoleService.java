package com.aplikata.service;

import com.aplikata.dto.RoleDto;
import java.util.List;

public interface RoleService {
	List<RoleDto> listRoles(Long projectId, Long domainId);

	RoleDto getRoleById(Long id);

	RoleDto createRole(RoleDto roleDto);

	RoleDto updateRole(Long id, RoleDto roleDto);

	void deleteRole(Long id);

	List<Long> getAssignedMenuIds(Long roleId);

	void assignMenus(Long roleId, List<Long> menuIds);
	
	void importRoles(Long projectId, Long domainId, String jsonContent);
}