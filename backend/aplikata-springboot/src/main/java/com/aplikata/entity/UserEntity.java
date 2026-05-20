package com.aplikata.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "_user", uniqueConstraints = { @UniqueConstraint(columnNames = { "domain_id", "username" }) })
@Data
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_name", nullable = false, length = 45)
	private String username;

	@Column(name = "user_pwd", nullable = false)
	private String password;

	@Column(length = 45)
	private String email;

	@Column(name = "blocked")
	private boolean blocked;

	// 所属项目（可为 null，超级管理员）
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private ProjectEntity project;

	// 所属域（可为 null，超级管理员时 null）
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_id")
	private DomainEntity domain;

	// 多对多关系：一个用户可以有多个角色，一个角色可以有多个用户
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "_user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<RoleEntity> roles = new ArrayList<>();

	// convenience method to get projectId via domain
	public Long getProjectId() {
		return project != null ? project.getId() : null;
	}

	public Long getDomainId() {
		return domain != null ? domain.getId() : null;
	}
}
