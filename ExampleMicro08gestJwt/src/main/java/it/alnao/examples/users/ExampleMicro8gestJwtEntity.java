package it.alnao.examples.users;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "utenti")
public class ExampleMicro8gestJwtEntity{
	@Id
	private String id;
	
	@Indexed(unique = true)
	@Size(min = 5, max = 80, message = "UserId non valido")
	@NotNull(message = "UserId non valido")
	private String userId;
	
	@Size(min = 8, max = 80, message = "Password non valida")
	private String password;
	
	private String attivo;
	
	private List<String> ruoli;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAttivo() {
		return attivo;
	}

	public void setAttivo(String attivo) {
		this.attivo = attivo;
	}

	public List<String> getRuoli() {
		return ruoli;
	}

	public void setRuoli(List<String> ruoli) {
		this.ruoli = ruoli;
	}
	@Override
	public String toString() {
		return "ExampleMicro8gestJwtEntity [id=" + id + ", userId=" + userId + ", password=" + password + ", attivo="
				+ attivo + ", ruoli=" + ruoli + "]";
	}

}