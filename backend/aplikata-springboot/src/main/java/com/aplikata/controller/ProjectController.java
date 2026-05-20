package com.aplikata.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aplikata.dto.ApiResponse;
import com.aplikata.dto.ProjectDto;
import com.aplikata.service.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ApiResponse<List<ProjectDto>> getAllProjects() {
        return ApiResponse.success(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDto> getProjectById(@PathVariable Long id) {
        return ApiResponse.success(projectService.getProjectById(id));
    }

    @PostMapping
    public ApiResponse<ProjectDto> createProject(@Valid @RequestBody ProjectDto dto) {
        return ApiResponse.success(projectService.createProject(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectDto> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDto dto) {
        return ApiResponse.success(projectService.updateProject(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ApiResponse.success("Project deleted successfully");
    }
}