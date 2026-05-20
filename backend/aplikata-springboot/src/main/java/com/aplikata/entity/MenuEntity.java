package com.aplikata.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "_menu")
@Data
public class MenuEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 45)
	private String name;

	private Long parentId; // 父菜单ID，0 表示根菜单

	@Column(columnDefinition = "TINYINT(2)")
	private int level;

	@Column(length = 127)
	private String path;

	@Column(length = 45)
	private String component;

	@Column(length = 45)
	private String icon;

	@Column
	private Integer sortOrder;

	@Transient
	private List<MenuEntity> children = new ArrayList<>();

	@ManyToMany(mappedBy = "menus")
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<RoleEntity> roles = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	@JsonIgnore
	private ProjectEntity project; // 允许为 null，表示全局菜单

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_id")
	@JsonIgnore
	private DomainEntity domain; // 可为 null，表示全局菜单
	
	@Column(name = "json_id")
	private Long jsonId;

	public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    public Long getDomainId() {
        return domain != null ? domain.getId() : null;
    }
}
