package it.alnao.examples;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
//classe per gestione dell'errore
public class AuthEntryPoint extends BasicAuthenticationEntryPoint{
	private static final Logger logger=LoggerFactory.getLogger(AuthEntryPoint.class);
	private static String REALM="REAME";//uguale all'altra classe
	
	@Override
	public void commence(final HttpServletRequest request
				,final HttpServletResponse response
				,final AuthenticationException authException
			) throws IOException{
		String msg="Userid e/o password non valide";
		logger.warn("Errore"+authException.getMessage());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
		PrintWriter writer=response.getWriter();
		writer.println(msg);
	}
	@Override
	public void afterPropertiesSet(){
		setRealmName(REALM);
		super.afterPropertiesSet();
	}
}