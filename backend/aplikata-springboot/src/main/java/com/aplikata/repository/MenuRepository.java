package com.aplikata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aplikata.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

	// 查询全局菜单
	@Query("SELECT m FROM MenuEntity m WHERE m.project IS NULL ORDER BY m.sortOrder ASC")
	List<MenuEntity> findGlobalMenusOrderBySortOrderAsc();

	// 查询项目级菜单 + 全局菜单
	@Query("SELECT m FROM MenuEntity m WHERE (m.project.id = :projectId AND m.domain IS NULL) OR m.project IS NULL ORDER BY m.sortOrder ASC")
	List<MenuEntity> findByProjectIdOrGlobal(@Param("projectId") Long projectId);

	// 查询指定域菜单 + 项目级菜单 + 全局菜单
	@Query("SELECT m FROM MenuEntity m WHERE m.domain.id = :domainId OR (m.project.id = :projectId AND m.domain IS NULL) OR m.project IS NULL ORDER BY m.sortOrder ASC")
	List<MenuEntity> findByDomainIdOrProjectIdOrGlobal(@Param("domainId") Long domainId,
			@Param("projectId") Long projectId);

	// 检查是否存在子菜单
	boolean existsByParentId(Long parentId);

	// 全局菜单唯一性（排除自身）
	@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuEntity m WHERE m.name = :name AND m.project IS NULL AND (:excludeId IS NULL OR m.id <> :excludeId)")
	boolean existsByNameAndProjectIsNullAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

	// 项目级菜单唯一性（排除自身）
	@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuEntity m WHERE m.name = :name AND m.project.id = :projectId AND m.domain IS NULL AND (:excludeId IS NULL OR m.id <> :excludeId)")
	boolean existsByNameAndProjectIdAndDomainIsNullAndIdNot(@Param("name") String name,
			@Param("projectId") Long projectId, @Param("excludeId") Long excludeId);

	// 域级菜单唯一性（排除自身）
	@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuEntity m WHERE m.name = :name AND m.domain.id = :domainId AND (:excludeId IS NULL OR m.id <> :excludeId)")
	boolean existsByNameAndDomainIdAndIdNot(@Param("name") String name, @Param("domainId") Long domainId,
			@Param("excludeId") Long excludeId);

	@Query(value = "SELECT DISTINCT m.* FROM menus m JOIN _role_menu rm ON m.id = rm.menu_id "
			+ "WHERE rm.role_id IN (:roleIds) " + "AND (m.project_id IS NULL " + // 全局菜单
			"     OR (m.project_id = :projectId AND m.domain_id IS NULL) " + // 项目级菜单
			"     OR (m.domain_id = :domainId)) " + // 域级菜单
			"ORDER BY m.sort_order ASC", nativeQuery = true)
	List<MenuEntity> findMenusByRoleIdsAndUserScope(@Param("roleIds") List<Long> roleIds,
			@Param("projectId") Long projectId, @Param("domainId") Long domainId);

	// 根据旧菜单 ID 列表以及项目/域查询菜单
    @Query("SELECT m FROM MenuEntity m WHERE m.jsonId IN :jsonIds AND m.project.id = :projectId AND (:domainId IS NULL OR m.domain.id = :domainId)")
    List<MenuEntity> findByJsonIdInAndProjectIdAndDomainId(@Param("jsonIds") List<Long> jsonIds,
                                                     @Param("projectId") Long projectId,
                                                     @Param("domainId") Long domainId);
}
