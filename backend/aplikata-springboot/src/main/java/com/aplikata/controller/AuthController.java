package com.aplikata.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aplikata.dto.ApiResponse;
import com.aplikata.dto.LoginDto;
import com.aplikata.dto.LoginResponse;
import com.aplikata.dto.RegisterRequest;
import com.aplikata.dto.UserDto;
import com.aplikata.entity.UserEntity;
import com.aplikata.exception.BusinessException;
import com.aplikata.repository.UserRepository;
import com.aplikata.security.CustomUserDetails;
import com.aplikata.utils.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@RequestBody LoginDto loginDto) {
		// 1️⃣ 让 Spring Security 校验用户名密码
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

		// 2. 认证成功后，生成 JWT
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		// 3. 从数据库查询完整的用户信息（避免返回密码）
		UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new BusinessException(500, "User not found after authentication"));

		String token = jwtUtil.generateToken(userDetails);
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userEntity, userDto);
		LoginResponse response = new LoginResponse(token, userDto);
		return ApiResponse.success("Login successful", response);
	}

//	// 注册接口
//	@PostMapping("/register")
//	public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
//		// 检查用户名是否已存在
//		if (userRepository.existsByUsername(request.getUsername())) {
//			throw new BusinessException(409, "Username already exists");
//		}
//
//		// 检查邮箱是否已存在
//		if (userRepository.existsByEmail(request.getEmail())) {
//			throw new BusinessException(409, "Email already exists");
//		}
//
//		// 创建新用户
//		UserEntity userEntity = new UserEntity();
//		userEntity.setUsername(request.getUsername());
//		userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
//		userEntity.setEmail(request.getEmail());
//
//		// 分配默认角色（例如 "ROLE_USER"），根据你的实体设计调整
//		// 如果你的 roles 是 List<String>，直接赋值
//		// user.setRoles(Collections.singletonList("ROLE_USER"));
//
//		// 保存到数据库
//		userRepository.save(userEntity);
//
//		return ApiResponse.success("User registered successfully");
//	}
}
