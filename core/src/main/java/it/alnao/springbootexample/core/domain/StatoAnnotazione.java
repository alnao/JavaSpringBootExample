package it.alnao.springbootexample.core.domain;

public enum StatoAnnotazione {
    INSERITA("INSERITA"),
    MODIFICATA("MODIFICATA"),
    CONFERMATA("CONFERMATA"),
    RIFIUTATA("RIFIUTATA"),
    PUBBLICATA("PUBBLICATA"),
    BANNATA("BANNATA"),
    ERRORE("ERRORE");

    private final String value;

    StatoAnnotazione(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
