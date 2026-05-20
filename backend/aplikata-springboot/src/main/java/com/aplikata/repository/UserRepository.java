package com.aplikata.repository;

import com.aplikata.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByDomainId(Long domainId); // 注意：这里方法名 findByDomainId 是合法的，JPA 会解析为 u.domain.id

    List<UserEntity> findByDomainIdIn(List<Long> domainIds);

    @Query("SELECT u FROM UserEntity u WHERE u.project IS NULL AND u.domain IS NULL")
    List<UserEntity> findSuperAdmins();

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.project IS NULL AND u.domain IS NULL AND u.username = :username")
    boolean existsSuperAdminByUsername(@Param("username") String username);

    // 使用 JPQL 明确关联
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.domain.id = :domainId AND u.username = :username")
    boolean existsByDomainIdAndUsername(@Param("domainId") Long domainId, @Param("username") String username);

    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT u FROM UserEntity u WHERE u.domain.project.id = :projectId")
    List<UserEntity> findByProjectId(@Param("projectId") Long projectId);
}