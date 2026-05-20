package com.aplikata.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aplikata.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

    	String authHeader = request.getHeader("Authorization");
    	 if (authHeader != null && authHeader.startsWith("Bearer ")) {
    		 String token = authHeader.substring(7);
    		 try {
                 String username = jwtUtil.extractUsername(token);

                 if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                     UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                     if (jwtUtil.validateToken(token, userDetails)) {
                         UsernamePasswordAuthenticationToken authToken =
                                 new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                         authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                         SecurityContextHolder.getContext().setAuthentication(authToken);
                     }
                 }
             } catch (Exception e) {
                 // 记录日志，但不抛出异常，继续过滤器链
                 logger.error("JWT authentication failed: " + e.getMessage());
             }
    	 }

        // 4️⃣ 放行请求链 ⚡ 必须调用
        filterChain.doFilter(request, response);
    }
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "aaaaaa";
        String encoded = encoder.encode(rawPassword);
        System.out.println(encoded); // 复制到数据库
    }
}