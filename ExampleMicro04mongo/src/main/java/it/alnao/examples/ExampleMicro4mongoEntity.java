package it.alnao.examples;

import java.util.List;

//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elenco")
public class ExampleMicro4mongoEntity {
	@Id
	private String _id;
	
	//@Indexed(unique = true)
	//@Size(min = 5, max = 80, message = "Name tra 5 e 80 caratteri")
	//@NotNull(message = "Nome non può essere vuoto")
	private String nome;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public ExampleMicro4mongoEntity() {
		super();
	}

	public ExampleMicro4mongoEntity(String _id, String nome) {
		super();
		this._id = _id;
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "ExampleMicro4mongoEntity [_id=" + _id + ", nome=" + nome + "]";
	}
	

	
}


