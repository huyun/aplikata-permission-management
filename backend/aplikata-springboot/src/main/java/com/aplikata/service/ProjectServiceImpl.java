package com.aplikata.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplikata.dto.ProjectDto;
import com.aplikata.entity.ProjectEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.ProjectRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    private ProjectDto convertToDto(ProjectEntity project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        return dto;
    }

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDto getProjectById(Long id) {
    	ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        return convertToDto(project);
    }

    @Override
    @Transactional
    public ProjectDto createProject(ProjectDto dto) {
        // 检查 name 是否已存在
        if (projectRepository.existsByName(dto.getName())) {
            throw new BusinessException(409, "Project name already exists");
        }
        ProjectEntity project = new ProjectEntity();
        project.setName(dto.getName());
        // 无需设置时间戳
        project = projectRepository.save(project);
        return convertToDto(project);
    }

    @Override
    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto dto) {
    	ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        // 如果修改了 name，检查新 name 是否唯一（且不是自己）
        if (!project.getName().equals(dto.getName()) && projectRepository.existsByName(dto.getName())) {
            throw new BusinessException(409, "Project name already exists");
        }
        project.setName(dto.getName());
        // 无需更新时间戳
        project = projectRepository.save(project);
        return convertToDto(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
    	ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        // 可选：检查是否有关联的 domain
        if (project.getDomains() != null && !project.getDomains().isEmpty()) {
            throw new BusinessException(400, "Cannot delete project with existing domains");
        }
        projectRepository.delete(project);
    }
}