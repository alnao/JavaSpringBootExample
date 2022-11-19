package it.alnao.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class ExampleMicro10cloudBasicSecurity /* { */ extends WebSecurityConfigurerAdapter{
	@Value("${sicurezza.user}")  
	String user;

	@Value("${sicurezza.pwd}")  
	String pwd; 
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	};
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
		System.out.println(this.user + this.pwd);
		auth.inMemoryAuthentication()
			.withUser(this.user)
			.password(new BCryptPasswordEncoder().encode(this.pwd))
			.roles("USER");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.csrf().disable().httpBasic()
		// .and().authorizeRequests()
		//	.antMatchers("/**").hasAuthority("ROLE_USER")
		;
	} /* */
}