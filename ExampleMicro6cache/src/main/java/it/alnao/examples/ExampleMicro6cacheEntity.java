package it.alnao.examples;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name= "articoli", schema = "MyWeb")
public class ExampleMicro6cacheEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	public ExampleMicro6cacheEntity() {
		super();
	}
	@Override
	public String toString() {
		return "ExampleMicro2dbEntity [id=" + id + ", nome=" + nome + ", valore=" + valore + "]";
	}
	  
	  
}
