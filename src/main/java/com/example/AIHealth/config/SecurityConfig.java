package com.example.AIHealth.config;

import com.example.AIHealth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// ▼▼▼ [추가] WebSecurityCustomizer import ▼▼▼
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 설정
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/inbody/**", "/recommendation/**")
                )
                // 1. 요청별 인가 규칙 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/inbody/analyze").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/workout/**",
                                "/comment/**",
                                "/main/profile",
                                "/member/update",
                                "/board/write",
                                "/board/edit/**",
                                "/consult/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                // 2. 폼 기반 로그인 설정
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("memberEmail")
                        .passwordParameter("memberPassword")
                        .defaultSuccessUrl("/main", true)
                        .permitAll()
                )
                // 3. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/member/login")
                        .defaultSuccessUrl("/main", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                // 4. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/main")
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }

    // ▼▼▼ [추가] 정적 리소스(css, js, images 등)에 대한 시큐리티 적용을 무시하는 설정 ▼▼▼
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
    }
}