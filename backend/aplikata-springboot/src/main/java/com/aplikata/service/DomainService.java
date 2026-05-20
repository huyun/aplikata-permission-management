package com.aplikata.service;

import java.util.List;

import com.aplikata.dto.DomainDto;

public interface DomainService {

	List<DomainDto> getDomainsByProject(Long projectId);

	DomainDto getDomainById(Long id);

	DomainDto createDomain(DomainDto dto);

	DomainDto updateDomain(Long id, DomainDto dto);

	void deleteDomain(Long id);
}
