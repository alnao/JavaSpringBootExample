package it.alnao.examples;
import java.io.Serializable;

public class ExampleMicro8gestJwtResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;
	private final String jwttoken;

	public ExampleMicro8gestJwtResponse(String jwttoken) {
		this.jwttoken = jwttoken;
	}

	public String getToken() {
		return this.jwttoken;
	}
}