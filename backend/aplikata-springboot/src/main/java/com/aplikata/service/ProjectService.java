package com.aplikata.service;

import com.aplikata.dto.ProjectDto;
import java.util.List;

public interface ProjectService {
    List<ProjectDto> getAllProjects();
    ProjectDto getProjectById(Long id);
    ProjectDto createProject(ProjectDto dto);
    ProjectDto updateProject(Long id, ProjectDto dto);
    void deleteProject(Long id);
}