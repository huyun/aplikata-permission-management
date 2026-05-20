package com.aplikata.handler;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.aplikata.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message;
        if (authException instanceof BadCredentialsException) {
            message = "Invalid username or password";
        } else if (authException instanceof UsernameNotFoundException) {
            message = "User not found";
        } else {
            message = "Unauthorized, please login first";
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(401, message);
        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }

}
