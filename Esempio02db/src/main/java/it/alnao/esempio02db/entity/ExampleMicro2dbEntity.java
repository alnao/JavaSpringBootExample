package it.alnao.esempio02db.entity;

//import javax.persistence.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name= "articoli", schema = "MyWeb")
public class ExampleMicro2dbEntity {
	  @Id
	  @GeneratedValue
	  private Long id;
	  @Column(nullable = false)
	  private String nome;
	  @Column(nullable = false)
	  private String valore;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getValore() {
		return valore;
	}
	public void setValore(String valore) {
		this.valore = valore;
	}
	public ExampleMicro2dbEntity() {
		super();
	}
	public ExampleMicro2dbEntity(Long id, String nome, String valore) {
		super();
		this.id=id;
		this.nome=nome;
		this.valore=valore;
	}
	@Override
	public String toString() {
		return "ExampleMicro2dbEntity [id=" + id + ", nome=" + nome + ", valore=" + valore + "]";
	}
	  
	  
}
