package com.aplikata.repository;

import com.aplikata.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // 基础查询
    Optional<RoleEntity> findByName(String name);
    
    // 按项目查询
    @Query(value = "SELECT * FROM _role WHERE project_id = :projectId", nativeQuery = true)
    List<RoleEntity> findByProjectId(@Param("projectId") Long projectId);
    
    // 按域查询
    @Query(value = "SELECT * FROM _role WHERE domain_id = :domainId", nativeQuery = true)
    List<RoleEntity> findByDomainId(@Param("domainId") Long domainId);
    
    // 全局角色名称唯一
    @Query(value = "SELECT EXISTS(SELECT 1 FROM _role WHERE name = :name AND project_id IS NULL AND domain_id IS NULL)", nativeQuery = true)
    boolean existsByName(@Param("name") String name);
    
    // 项目级角色名称唯一（project_id 非空，domain_id 为空）
    @Query(value = "SELECT EXISTS(SELECT 1 FROM _role WHERE project_id = :projectId AND name = :name AND domain_id IS NULL)", nativeQuery = true)
    boolean existsByProjectIdAndName(@Param("projectId") Long projectId, @Param("name") String name);
    
    // 域级角色名称唯一（domain_id 非空）
    @Query(value = "SELECT EXISTS(SELECT 1 FROM _role WHERE domain_id = :domainId AND name = :name)", nativeQuery = true)
    boolean existsByDomainIdAndName(@Param("domainId") Long domainId, @Param("name") String name);
    
    // 组合查询（用于列表过滤）
    @Query(value = "SELECT * FROM _role WHERE (:projectId IS NULL OR project_id = :projectId) AND (:domainId IS NULL OR domain_id = :domainId)", nativeQuery = true)
    List<RoleEntity> findByProjectIdAndDomainId(@Param("projectId") Long projectId,
                                                @Param("domainId") Long domainId);
    
    // 查询指定项目下的所有角色（包括项目级和域级）
    @Query(value = "SELECT * FROM _role WHERE project_id = :projectId", nativeQuery = true)
    List<RoleEntity> findAllByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT r FROM RoleEntity r WHERE r.project IS NULL AND r.domain IS NULL")
    List<RoleEntity> findGlobalRoles();
}