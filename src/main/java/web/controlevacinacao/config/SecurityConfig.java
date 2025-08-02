package web.controlevacinacao.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(configurer -> configurer
                                                //.anyRequest().permitAll()
                                                // Qualquer um pode fazer requisições para essas URLs
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/index.html")
                                                .permitAll()
                                                .requestMatchers("/movimentacoes/**").hasAnyRole("ADMIN") // APENAS ADMIN e OPERADOR
                                                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                                                .requestMatchers("/carros/**").hasAnyRole("ADMIN", "USUARIO")
                                                // Um usuário autenticado e com o papel ADMIN pode fazer requisições
                                                // para essas
                                                // URLs
                                                //.requestMatchers("/carros/**", "/motoristas/**", "/pessoas/**", "/vacinas/**", "/lotes/**", "/aplicacoes/**", "/relatorios/**")
                                               // .hasAnyRole("USUARIO", "ADMIN")
                                               // .requestMatchers("/movimentacoes/**").hasRole("ADMIN") // Somente ADMIN pode ver movimentações
                                               // .requestMatchers("/usuarios/cadastrar").hasRole("ADMIN") // Apenas ADMIN cadastra usuários

                                               // .requestMatchers("/vacinas/cadastrar").hasRole("ADMIN")
                                               // .requestMatchers("/usuarios/cadastrar").hasRole("ADMIN")
                                                .anyRequest().permitAll()

                                )
                                                //.anyRequest().permitAll())
                                // .requestMatchers("URL").hasAnyRole("ADMIN", "USUARIO")
                                                .formLogin(form -> form
                                                // Uma página de login customizada
                                                .loginPage("/login")
                                                // Define a URL para onde o usuário será redirecionado após o login
                                                .defaultSuccessUrl("/index.html")
                                                // Define a URL para o caso de falha no login
                                                //
                                                 .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessHandler(
                                                                (_, response, _) -> response.addHeader("HX-Redirect",
                                                                                "/"))
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(DataSource dataSource) {
                JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
                manager.setUsersByUsernameQuery("select nome_usuario, senha, ativo "
                                + "from usuario "
                                + "where nome_usuario = ?");
                manager.setAuthoritiesByUsernameQuery(
                                "SELECT tab.nome_usuario , papel.nome FROM"
                                                + "(SELECT usuario.nome_usuario , usuario.codigo FROM usuario WHERE nome_usuario = ?) as tab "
                                                + " INNER JOIN usuario_papel ON codigo_usuario = tab.codigo "
                                                + " INNER JOIN papel ON codigo_papel = papel.codigo;");
                return manager;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                // return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

                // Usar um PasswordEncoder que aceite todos os esquemas disponiveis no Spring
                // Security
                // mas escolhendo quais vamos aceitar. As senhas comecam dizendo qual o esquema
                // usado {noop}, {bcrypt}, {argon2}
                String idEnconder = "argon2";
                Map<String, PasswordEncoder> encoders = new HashMap<>();
                encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
                encoders.put("noop", NoOpPasswordEncoder.getInstance());
                encoders.put("bcrypt", new BCryptPasswordEncoder());
                // encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
                // encoders.put("scrypt", new SCryptPasswordEncoder());
                // encoders.put("sha256", new StandardPasswordEncoder());
                PasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(idEnconder, encoders);
                return passwordEncoder;
        }

}