package it.alnao.examples;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Component
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = ExampleMicro11asyncOrdineEntity.class)
@Document(collection = "magazzino")
public class ExampleMicro11asyncOrdineEntity  implements Serializable {
    private static final long serialVersionUID = 1L;
    
	private String userId;
    private String idOrdine;
    private String idProdotto;
    private String quantita;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getIdOrdine() {
		return idOrdine;
	}
	public void setIdOrdine(String idOrdine) {
		this.idOrdine = idOrdine;
	}
	public String getQuantita() {
		return quantita;
	}
	public void setQuantita(String quantita) {
		this.quantita = quantita;
	}
	public ExampleMicro11asyncOrdineEntity(/*String userId, String idOrdine, String quantita*/) {
		super();
		//this.userId = userId;
		//this.idOrdine = idOrdine;
		//this.quantita = quantita;
	}
	public String getIdProdotto() {
		return idProdotto;
	}
	public void setIdProdotto(String idProdotto) {
		this.idProdotto = idProdotto;
	}
	@Override
	public String toString() {
		return "ExampleMicro11asyncOrdineEntity [userId=" + userId + ", idOrdine=" + idOrdine + ", idProdotto="
				+ idProdotto + ", quantita=" + quantita + "]";
	}


}
