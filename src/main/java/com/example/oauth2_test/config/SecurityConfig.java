package com.example.oauth2_test.config;


import com.example.oauth2_test.config.oauth.PrincipleOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/*
				1. 코드를 받기(인증)
				2. 엑세스 토큰(사용자 정보에 접근가능한 권한)
				3. 사용자 프로필 정보를 가져옴
				4. 그 정보를 토대로 회원가입을 진행 시킴
				-> 그 외의 정보 (집주소, 회원등급 등등)가 필요하게 된다면 추가적으로 정보를 더 받는다
				만약 없다면 구글이 주는 기본정보로 로그인을 시키면 된다.
 */



@Configuration
@EnableWebSecurity //시큐리티 활성화 -> 기본 스프링 필터 체인에 등록
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true) //secured 어노테이션 활성화, preAuthorize 어노테이션 활성화
//prePostEnabled = true 의 경우 preAuthorize, postAuthorize 두가지의 어노테이션을 활성화 시켜준다.
public class SecurityConfig  {

    @Autowired
    private PrincipleOauth2UserService principleOauth2UserService;

    //해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
//    @Bean
//    public BCryptPasswordEncoder encodePwd() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 인가(접근권한) 설정
        http.authorizeHttpRequests().requestMatchers("/user/**").authenticated()
                .requestMatchers("/manager/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN")
                .anyRequest().permitAll();

        http.formLogin().loginPage("/loginForm")
                .loginProcessingUrl("/login") //login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행 -> Controller에 /login 안만들어도됨
                .defaultSuccessUrl("/")
                .and()
                .oauth2Login()
                .loginPage("/loginForm") //구글 로그인이 완료 된 뒤의 후처리가 필요함, 구글 로그인이 되면 엑세스 토큰 + 사용자 프로필 정보를 함께 받음음
                .userInfoEndpoint()
                .userService(principleOauth2UserService); //Service내부에 들어가는 것은 Oauth2UserService가 되어야한다.


        //                .usernameParameter("ㅇㅇ") //PrincipalDetailsService 에서와 String 일치 하지 않으면 여기서 설정

        // 사이트 위변조 요청 방지
        http.csrf().disable();

//        // 로그인 설정
//        http.formLogin()
//                .loginPage("/user2/login")
//                .defaultSuccessUrl("/user2/loginSuccess")
//                .failureUrl("/user2/login?success=100)")
//                .usernameParameter("uid")
//                .passwordParameter("pass");

//        // 로그아웃 설정
//        http.logout()
//                .invalidateHttpSession(true)
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                .logoutSuccessUrl("/login?success=200");

        // 사용자 인증 처리 컴포넌트 서비스 등록
//        http.userDetailsService(service);

        return http.build();
    }
}
