package it.alnao.examples;

import javax.persistence.*;

@Entity
@Table(name= "articoli", schema = "public")
public class ExampleMicro3postgresEntity {
	  @Id
	  @GeneratedValue( generator="sq_articoli")
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
	public ExampleMicro3postgresEntity() {
		super();
	}
	@Override
	public String toString() {
		return "ExampleMicro3dbEntity [id=" + id + ", nome=" + nome + ", valore=" + valore + "]";
	}
	  
	  
}
