package com.kocatepe.kafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.kocatepe.kafe.entity.Kullanici;
import com.kocatepe.kafe.repository.KullaniciRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(KullaniciRepository kullaniciRepository) {
        return kullaniciAdi -> {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(kullaniciAdi)
                    .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + kullaniciAdi));
            return User.withUsername(kullanici.getKullaniciAdi())
                    .password(kullanici.getSifreHash())
                    .roles(kullanici.getRol().name())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/giris", "/hata").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/personel/**", "/yogunluk/**", "/tahmin/**", "/csv/**")
                        .hasRole("YONETICI")
                        .requestMatchers(HttpMethod.POST, "/vardiya/kaydet", "/vardiya/yayinla", "/vardiya/otomatik")
                        .hasRole("YONETICI")
                        .requestMatchers(HttpMethod.POST, "/takas/*/onayla", "/takas/*/yonetici-reddet")
                        .hasRole("YONETICI")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/giris")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/giris?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/cikis")
                        .logoutSuccessUrl("/giris?logout")
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
