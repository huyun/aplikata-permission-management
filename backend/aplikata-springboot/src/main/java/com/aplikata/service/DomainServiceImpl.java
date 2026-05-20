package com.aplikata.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplikata.dto.DomainDto;
import com.aplikata.entity.DomainEntity;
import com.aplikata.entity.ProjectEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.DomainRepository;
import com.aplikata.repository.ProjectRepository;

@Service
public class DomainServiceImpl implements DomainService {

	@Autowired
	private DomainRepository domainRepository;

	@Autowired
	private ProjectRepository projectRepository;

	private DomainDto convertToDto(DomainEntity domain) {
		DomainDto dto = new DomainDto();
		dto.setId(domain.getId());
		dto.setProjectId(domain.getProject().getId());
		dto.setName(domain.getName());
		return dto;
	}

//	@Override
//	public List<DomainDto> getAllDomains() {
//		return domainRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
//	}

	@Override
	public List<DomainDto> getDomainsByProject(Long projectId) {
		return domainRepository.findByProjectId(projectId).stream().map(this::convertToDto)
				.collect(Collectors.toList());
	}

	@Override
	public DomainDto getDomainById(Long id) {
		DomainEntity domain = domainRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "Domain not found"));
		return convertToDto(domain);
	}

	@Override
	@Transactional
	public DomainDto createDomain(DomainDto dto) {
		ProjectEntity project = projectRepository.findById(dto.getProjectId())
				.orElseThrow(() -> new BusinessException(404, "Project not found"));

		if (domainRepository.existsByProjectIdAndName(dto.getProjectId(), dto.getName())) {
			throw new BusinessException(409, "Domain name already exists in this project");
		}

		DomainEntity domain = new DomainEntity();
		domain.setProject(project);
		domain.setName(dto.getName());
		domain = domainRepository.save(domain);
		return convertToDto(domain);
	}

	@Override
	@Transactional
	public DomainDto updateDomain(Long id, DomainDto dto) {
		DomainEntity domain = domainRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "Domain not found"));

		// 如果项目发生变化
		if (!domain.getProject().getId().equals(dto.getProjectId())) {
			ProjectEntity newProject = projectRepository.findById(dto.getProjectId())
					.orElseThrow(() -> new BusinessException(404, "New project not found"));
			domain.setProject(newProject);
		}

		// 名称唯一性校验
		if (!domain.getName().equals(dto.getName())
				&& domainRepository.existsByProjectIdAndName(domain.getProject().getId(), dto.getName())) {
			throw new BusinessException(409, "Domain name already exists in this project");
		}

		domain.setName(dto.getName());
		domain = domainRepository.save(domain);
		return convertToDto(domain);
	}

	@Override
	@Transactional
	public void deleteDomain(Long id) {
		DomainEntity domain = domainRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "Domain not found"));
		// 可选：检查是否有用户或菜单关联
		// if (!domain.getUsers().isEmpty()) { throw new BusinessException(400, "Cannot
		// delete domain with existing users"); }
		domainRepository.delete(domain);
	}

}
