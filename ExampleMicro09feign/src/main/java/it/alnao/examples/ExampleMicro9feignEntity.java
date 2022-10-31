package it.alnao.examples;



//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prodotti")
public class ExampleMicro9feignEntity {
    @Id
    private String _id;
    
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

    public ExampleMicro9feignEntity(String _id, String nome) {
        super();
        this._id = _id;
        this.nome = nome;
    }
    public ExampleMicro9feignEntity() {
        super();
    }

    @Override
    public String toString() {
        return "ExampleMicro9feignEntity [_id=" + _id + ", nome=" + nome + "]";
    }
}