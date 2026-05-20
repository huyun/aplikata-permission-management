package com.aplikata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aplikata.entity.DomainEntity;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {
	// 根据项目 ID 获取域名列表
    @Query("SELECT d FROM DomainEntity d WHERE d.project.id = :projectId ORDER BY d.id")
    List<DomainEntity> findByProjectId(@Param("projectId") Long projectId);

    // 检查同一项目下域名是否存在
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DomainEntity d WHERE d.project.id = :projectId AND d.name = :name")
    boolean existsByProjectIdAndName(@Param("projectId") Long projectId, @Param("name") String name);

    // 根据项目 ID 和域名获取实体
    @Query("SELECT d FROM DomainEntity d WHERE d.project.id = :projectId AND d.name = :name")
    Optional<DomainEntity> findByProjectIdAndName(@Param("projectId") Long projectId, @Param("name") String name);
}
