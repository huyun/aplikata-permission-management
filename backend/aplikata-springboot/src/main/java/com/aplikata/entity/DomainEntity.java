package com.aplikata.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "_domain")
@Data
public class DomainEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false , length = 45)
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;
	
	@Column(name="blocked")
	private boolean blocked;
	
	// One domain can have many users
    @OneToMany(mappedBy = "domain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEntity> users;
    
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }
}
