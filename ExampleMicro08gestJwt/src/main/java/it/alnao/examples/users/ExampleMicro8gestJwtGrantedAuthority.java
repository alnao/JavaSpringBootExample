package it.alnao.examples.users;

import org.springframework.security.core.GrantedAuthority;

public class ExampleMicro8gestJwtGrantedAuthority implements GrantedAuthority{
	private static final long serialVersionUID = 1L;
	private String authority;
	
	public ExampleMicro8gestJwtGrantedAuthority(String authority) {
		super();
		this.authority = authority;
	}
	
	@Override
	public String getAuthority() {
		return this.authority;
	}
	
}
