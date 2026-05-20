package com.aplikata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DomainDto {
	private Long id;

	@NotNull(message = "Project ID is required")
	private Long projectId;

	@NotBlank
	private String name;
}
