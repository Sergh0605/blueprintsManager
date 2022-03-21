package com.dataart.blueprintsmanager.config.security;

import com.dataart.blueprintsmanager.persistence.entity.Role;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtFilter jwtFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/user/register", "/api/user/auth", "/api/user/refresh_token_auth").permitAll()
                .antMatchers(HttpMethod.GET, "/api/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/user/logout").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/user/*").authenticated()
                .antMatchers(HttpMethod.POST, "/api/project/**").hasAuthority(Role.EDITOR.name())
                .antMatchers(HttpMethod.PUT, "/api/project/**").hasAuthority(Role.EDITOR.name())
                .antMatchers(HttpMethod.POST, "/api/company/**", "/api/user/**").hasAuthority(Role.ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/api/company/**", "/api/user/**").hasAuthority(Role.ADMIN.name())
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
