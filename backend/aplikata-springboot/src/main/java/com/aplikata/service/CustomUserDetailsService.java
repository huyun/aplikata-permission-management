package com.aplikata.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aplikata.entity.RoleEntity;
import com.aplikata.entity.UserEntity;
import com.aplikata.repository.UserRepository;
import com.aplikata.security.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found!"));
		
		List<String> roles = userEntity.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toList());

		return new CustomUserDetails(
		        userEntity.getId(),
		        userEntity.getUsername(),
		        userEntity.getPassword(),
		        userEntity.getProjectId(),
		        userEntity.getDomainId(),
		        roles
		    );
	}
}
