package com.jongwon.FunBit.jwt;

import com.jongwon.FunBit.Entity.User;
import com.jongwon.FunBit.dto.JWTUserDetails;
import com.jongwon.FunBit.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.matches("^\\/login(?:\\/.*)?$")) {

            filterChain.doFilter(request, response);
            return;
        }
        if (requestUri.matches("^\\/oauth2(?:\\/.*)?$")) {

            filterChain.doFilter(request, response);
            return;
        }

        // request에서 Authorization 헤더를 얻음
        String authorization = request.getHeader("Authorization");
        
        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer")) {

            logger.info("token null");
            filterChain.doFilter(request, response);

            return;
        }

        logger.info("authorization now");
        // Bearer 제거(순수 토큰)
        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            logger.info("token expired");
            filterChain.doFilter(request, response);

            return;
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // User 생성
        User user = new User();
        user.setUsername(username);
        user.setRole(role);

        // UserDetails에 User 객체 추가
        JWTUserDetails jwtUserDetails = new JWTUserDetails(user);

        // Spring Security 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request,response);
    }
}
