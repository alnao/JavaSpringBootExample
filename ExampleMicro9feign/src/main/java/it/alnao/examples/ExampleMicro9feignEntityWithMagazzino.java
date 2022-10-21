package it.alnao.examples;

public class ExampleMicro9feignEntityWithMagazzino extends ExampleMicro9feignEntity{
    
    public ExampleMicro9feignEntityWithMagazzino(ExampleMicro9feignEntity el){
        this.set_id(el.get_id());
        this.setNome(el.getNome());
        this.magazzino="NULL";
    }
    
    private String magazzino;

    public String getMagazzino() {
        return magazzino;
    }

    public void setMagazzino(String magazzino) {
        this.magazzino = magazzino;
    }
    
    
    
}
