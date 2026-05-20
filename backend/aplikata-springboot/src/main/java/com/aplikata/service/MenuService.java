package com.aplikata.service;

import com.aplikata.dto.MenuDto;
import com.aplikata.entity.MenuEntity;
import java.util.List;

public interface MenuService {

    /**
     * 获取菜单树（根据项目ID和域ID筛选）
     * @param projectId 项目ID（可为 null，表示全局菜单）
     * @param domainId  域ID（可为 null）
     * @return 菜单树（根节点列表）
     */
    List<MenuEntity> getMenuTree(Long projectId, Long domainId);

    /**
     * 创建菜单
     */
    MenuEntity createMenu(MenuDto menuDto);

    /**
     * 更新菜单
     */
    MenuEntity updateMenu(Long id, MenuDto menuDto);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

	List<MenuEntity> getCurrentUserMenus();
	
	void importMenus(Long projectId, Long domainId, String content);
}