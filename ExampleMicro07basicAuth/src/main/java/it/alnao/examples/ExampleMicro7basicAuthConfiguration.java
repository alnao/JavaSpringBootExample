package it.alnao.examples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;

@Configuration
@EnableWebSecurity
public class ExampleMicro7basicAuthConfiguration extends WebSecurityConfigurerAdapter {
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {//creatore di password
		return new BCryptPasswordEncoder();
	}
	@Bean
	public UserDetailsService userDetailsService() {
		UserBuilder user = User.builder();
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(user
				.username("root")
				.password(new BCryptPasswordEncoder().encode("difficile"))
				.roles("ADMIN")//ruoli
				.build()
			);
		manager.createUser(user
				.username("alnao")
				.password(new BCryptPasswordEncoder().encode("bello"))
				.roles("USER")//ruoli
				.build()
			);
		return manager;
	}
	
	public static final String api = "/api/demoms/";
	public static final String all = "/**";
	//public static final String[] USER_MATCHER = {api+"lista"+all};
	public static final String[] USER_MATCHER = {api+"/**"};
	public static final String[] ADMIN_MATCHER = {"/api/admin/**"};
	public static final String REALM="REAME";
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeRequests()
			.antMatchers("/api/demoms/lista").hasAnyRole("USER","ADMIN")
			.antMatchers("/api/demoms/amministrativa").hasRole("ADMIN")
			.antMatchers(HttpMethod.POST).hasAnyRole("ADMIN")
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic().realmName(REALM).authenticationEntryPoint(getBasicAuthEntryPoint())
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)/**/
		;
	}
	@Bean
	public AuthEntryPoint getBasicAuthEntryPoint() {
		return new AuthEntryPoint();
	}
	@Override
	public void configure(WebSecurity web) throws Exception{
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}
}
