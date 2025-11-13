package com.football.kick_board.common.config;

import com.football.kick_board.common.security.JwtAccessDeniedHandler;
import com.football.kick_board.common.security.JwtAuthenticationEntryPoint;
import com.football.kick_board.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 시큐리티 인증/인가 설정
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//세션 미사용
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/members/signup", "/members/login", "/api/matches/**").permitAll()// 회원가입 경로 허용
            .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/likes/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()
            //Swagger 접속을 위한 URL들 추가
            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            .anyRequest().authenticated())// 그 외 모든 요청은 인증 필요
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패 처리
            .accessDeniedHandler(jwtAccessDeniedHandler)     // ✨ 인가(권한) 실패 처리 ✨
        )
        // 기본 폼 로그인 비활성화 (자동 /login 리디렉션 방지)
        .formLogin(form -> form.disable())
        // 기본 HTTP Basic 인증 비활성화 (포스트맨 401 에러 방지)
        .httpBasic(basic -> basic.disable())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
