package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private static final String[] PUBLIC_ENDPOINTS = {
    "/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"
  };

  private final CustomJwtDecoder customJwtDecoder;

  public SecurityConfig(CustomJwtDecoder customJwtDecoder) {
    this.customJwtDecoder = customJwtDecoder;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.authorizeHttpRequests(
        request ->
            request
                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .authenticated());

    httpSecurity.oauth2ResourceServer(
        oauth2 ->
            oauth2
                .jwt(
                    jwtConfigurer ->
                        jwtConfigurer
                            .decoder(customJwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
    httpSecurity.csrf(AbstractHttpConfigurer::disable);

    return httpSecurity.build();
  }

  @Bean // Đăng ký cấu hình CORS vào Spring Để nó tự động hoạt động toàn hệ thống
  public CorsFilter
      corsFilter() { // Tạo một bộ lọc (filter) để chặn và xử lý request trước khi vào Controller
    CorsConfiguration corsConfiguration =
        new CorsConfiguration(); // Tạo nơi chứa luật CORS  (“Cho phép ai, làm gì, gửi gì”
    corsConfiguration.addAllowedOrigin("*"); // Cho phép mọi frontend (mọi domain) gọi API
    corsConfiguration.addAllowedMethod(
        "*"); // Cho phép tất cả HTTP method (GET, POST, PUT, DELETE...)
    corsConfiguration.addAllowedHeader(
        "*"); // Cho phép frontend gửi header (đặc biệt là Authorization)
    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource =
        new UrlBasedCorsConfigurationSource(); // Tạo nơi để gắn rule CORS với URL
    urlBasedCorsConfigurationSource.registerCorsConfiguration(
        "/**", corsConfiguration); // Áp dụng rule CORS cho toàn bộ API
    return new CorsFilter(urlBasedCorsConfigurationSource); // intercept request,
  }

  @Bean //
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtAuthenticationConverter;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }
}
