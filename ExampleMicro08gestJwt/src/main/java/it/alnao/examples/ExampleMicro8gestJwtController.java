package it.alnao.examples;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.alnao.examples.security.ExampleMicro8gestJwtTokenUtil;
import it.alnao.examples.users.ExampleMicro8gestJwtService;


@RestController
@CrossOrigin
@RequestMapping("api/demoms")
public class ExampleMicro8gestJwtController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private ExampleMicro8gestJwtTokenUtil jwtTokenUtil;

	@Autowired
	private ExampleMicro8gestJwtService userDetailsService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody ExampleMicro8gestJwtRequest authenticationRequest) throws Exception {

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword() ));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new ExampleMicro8gestJwtResponse(token));
	}

	@GetMapping(value ="/lista")
	  public ResponseEntity<List<String>> lista(){
		ArrayList<String> l=new ArrayList<String>();
		l.add("ok");
	    return new ResponseEntity<List<String>> ( l ,HttpStatus.OK);
	}

	@GetMapping(value ="/magazzino/{codArt}")
	public ResponseEntity<List<String>> magazzino(
		@RequestHeader("Authorization") String authHeader,
	  	@PathVariable("codArt") String codArt)
	{
		System.out.println("magazzinoAPIEsterna codArt=" + codArt);
		ArrayList<String> l=new ArrayList<String>();
		String s="17";
		if (codArt.equals("matite")) s="42";
		if (codArt.equals("penne")) s="16";
		if (codArt.equals("penne")) s="1000";
		if (codArt.equals("barche")) s="0";
		l.add(s);
	    return new ResponseEntity<List<String>> ( l ,HttpStatus.OK);
	}

}
