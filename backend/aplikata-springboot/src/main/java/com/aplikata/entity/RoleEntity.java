package com.aplikata.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "_role")
@Data
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name; // 例如 "ROLE_ADMIN", "ROLE_USER"
    
    @Column(length = 127)
    private String description;
    
    private Integer grade;
    private String protectUrl;
    
 // 所属项目（必填）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;
    
 // 所属租户（可选，为空表示项目级别）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private DomainEntity domain;

    // 反向多对多（与 User）
    @ManyToMany(mappedBy = "roles")
    private List<UserEntity> users = new ArrayList<>();


    // 与 Menu 的多对多关系
    @ManyToMany
    @JoinTable(
        name = "_role_menu",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MenuEntity> menus = new ArrayList<>();

 // 便捷方法
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    public Long getDomainId() {
        return domain != null ? domain.getId() : null;
    }
}
