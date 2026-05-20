package com.aplikata.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aplikata.entity.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    boolean existsByName(String name);
}