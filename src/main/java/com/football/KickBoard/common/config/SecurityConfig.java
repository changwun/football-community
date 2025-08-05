package com.football.KickBoard.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }
//나중에 시큐리티 인증/인가 설정도 여기에 추가
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/members/signup").permitAll()// 회원가입 경로 허용
            .anyRequest().authenticated())// 그 외 모든 요청은 인증 필요
        // 기본 폼 로그인 비활성화 (자동 /login 리디렉션 방지)
        .formLogin(form ->form.disable())
        // 기본 HTTP Basic 인증 비활성화 (포스트맨 401 에러 방지)
        .httpBasic(basic -> basic.disable());
    return http.build();
  }
}
