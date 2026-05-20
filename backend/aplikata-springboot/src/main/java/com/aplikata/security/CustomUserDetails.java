package com.aplikata.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

	private final Long id;
	private final String username;
	private final String password;
	private final Long projectId;
	private final Long domainId;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(Long id, String username, String password, Long projectId, Long domainId,
			List<String> roles) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.projectId = projectId;
		this.domainId = domainId;
		this.authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	// Getters for custom fields
	public Long getId() {
		return id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getDomainId() {
		return domainId;
	}

	// UserDetails methods
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
