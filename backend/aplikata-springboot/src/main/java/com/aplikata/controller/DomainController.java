package com.aplikata.controller;

import java.util.Collections;
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

import com.aplikata.dto.ApiResponse;
import com.aplikata.dto.DomainDto;
import com.aplikata.service.DomainService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/domains")
public class DomainController {
	@Autowired
    private DomainService domainService;

    @GetMapping
    public ApiResponse<List<DomainDto>> getAllDomains(@RequestParam(required = false) Long projectId) {
    	if (projectId == null) {
            return ApiResponse.success(Collections.emptyList());
        }
    	List<DomainDto> domains = domainService.getDomainsByProject(projectId);
        return ApiResponse.success(domains);
    }

    @GetMapping("/by-project/{projectId}")
    public ApiResponse<List<DomainDto>> getDomainsByProject(@PathVariable Long projectId) {
        return ApiResponse.success(domainService.getDomainsByProject(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DomainDto> getDomainById(@PathVariable Long id) {
        return ApiResponse.success(domainService.getDomainById(id));
    }

    @PostMapping
    public ApiResponse<DomainDto> createDomain(@Valid @RequestBody DomainDto dto) {
        return ApiResponse.success(domainService.createDomain(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<DomainDto> updateDomain(@PathVariable Long id, @Valid @RequestBody DomainDto dto) {
        return ApiResponse.success(domainService.updateDomain(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDomain(@PathVariable Long id) {
        domainService.deleteDomain(id);
        return ApiResponse.success("Domain deleted successfully");
    }
}
