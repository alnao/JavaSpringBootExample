package it.alnao.examples;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Component
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = ExampleMicro11asyncConsumerMagazzinoEntity.class)
@Document(collection = "magazzino")
public class ExampleMicro11asyncConsumerMagazzinoEntity  implements Serializable {
    public ExampleMicro11asyncConsumerMagazzinoEntity() {
		super();
	}
	private static final long serialVersionUID = 1L;
    
	private String idOrdine;
    private String idProdotto;
    private String quantita;
	public String getIdOrdine() {
		return idOrdine;
	}
	public void setIdOrdine(String idOrdine) {
		this.idOrdine = idOrdine;
	}
	public String getIdProdotto() {
		return idProdotto;
	}
	public void setIdProdotto(String idProdotto) {
		this.idProdotto = idProdotto;
	}
	public String getQuantita() {
		return quantita;
	}
	public void setQuantita(String quantita) {
		this.quantita = quantita;
	}
	@Override
	public String toString() {
		return "ExampleMicro11asyncConsumerMagazzinoEntity [idOrdine=" + idOrdine + ", idProdotto=" + idProdotto + ", quantita="
				+ quantita + "]";
	}


}
