package com.aplikata.controller;

import java.io.IOException;
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
import com.aplikata.dto.MenuDto;
import com.aplikata.entity.MenuEntity;
import com.aplikata.service.MenuService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

	@Autowired
	private MenuService menuService;

	// 获取当前登录用户的菜单树（用于前端侧边栏）
	@GetMapping("/user")
	public ApiResponse<List<MenuEntity>> getUserMenus() {
		List<MenuEntity> menus = menuService.getCurrentUserMenus();
		return ApiResponse.success(menus);
	}

	/**
	 * 获取菜单树（支持按项目、域筛选）
	 * 
	 * @param projectId 项目ID（可为 null，表示全局菜单）
	 * @param domainId  域ID（可为 null）
	 * @return 菜单树
	 */
	@GetMapping("/tree")
	public ApiResponse<List<MenuEntity>> getMenuTree(@RequestParam(required = false) Long projectId,
			@RequestParam(required = false) Long domainId) {
		List<MenuEntity> menuTree = menuService.getMenuTree(projectId, domainId);
		return ApiResponse.success(menuTree);
	}

	/**
	 * 创建菜单
	 * 
	 * @param menuDto 菜单信息
	 * @return 创建的菜单
	 */
	@PostMapping
	public ApiResponse<MenuEntity> createMenu(@Valid @RequestBody MenuDto menuDto) {
		MenuEntity menu = menuService.createMenu(menuDto);
		return ApiResponse.success(menu);
	}

	/**
	 * 更新菜单
	 * 
	 * @param id      菜单ID
	 * @param menuDto 更新信息
	 * @return 更新后的菜单
	 */
	@PutMapping("/{id}")
	public ApiResponse<MenuEntity> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuDto menuDto) {
		MenuEntity menu = menuService.updateMenu(id, menuDto);
		return ApiResponse.success(menu);
	}

	/**
	 * 删除菜单
	 * 
	 * @param id 菜单ID
	 * @return 无数据响应
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
		menuService.deleteMenu(id);
		return ApiResponse.success("Menu deleted successfully", null);
	}
	
	@PostMapping("/import")
	public ApiResponse<Void> importMenus(
	        @RequestParam Long projectId,
	        @RequestParam(required = false) Long domainId,
	        @RequestParam("file") MultipartFile file) throws IOException {
	    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
	    menuService.importMenus(projectId, domainId, content);
	    return ApiResponse.success("Menus imported successfully");
	}
}