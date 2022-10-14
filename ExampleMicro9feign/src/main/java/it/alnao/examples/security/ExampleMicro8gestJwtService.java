package it.alnao.examples.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class ExampleMicro8gestJwtService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return new User(username, "" , new ArrayList<>() );
	}
	/*
	@Autowired
	ExampleMicro8gestJwtRepository repository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ExampleMicro8gestJwtEntity user=null;
		List<ExampleMicro8gestJwtEntity> users = repository.findByUserId(username);
		if (users!=null) {
			//System.out.println("ExampleMicro8gestJwtService"+users.size() );
			if (users.size()>0) {
				user=users.get(0);
			}
		}
		//System.out.println("ExampleMicro8gestJwtService"+user );
		if (user != null){
			ArrayList<ExampleMicro8gestJwtGrantedAuthority> lista= new ArrayList<ExampleMicro8gestJwtGrantedAuthority>();
			user.getRuoli().forEach( (e) -> lista.add( new ExampleMicro8gestJwtGrantedAuthority(e) ) );
			return new User(user.getUserId(), new BCryptPasswordEncoder().encode(user.getPassword()) ,
					 lista//new ArrayList<>()
					);
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}
	*/
}
