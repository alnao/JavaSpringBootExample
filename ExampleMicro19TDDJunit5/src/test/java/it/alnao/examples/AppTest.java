package it.alnao.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.*;

public class AppTest {

    @Test
    public void shouldAnswerWithTrue(){
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("dividiNormale")
    public void dividiNormale() throws Exception{
        //1) Arrange: la definizione di tutti i dati di partenza 
        Double divisore=new Double(2.0);
        Double dividendo=new Double(5.0);
        Double resultAtteso=new Double(2.5);
        //2) Act: la chiamata alla operazione da testare (di solito è una sola operazione ma può essere anche multipla)
        Double result=App.dividi(dividendo, divisore);
        //3) Assert: la validazione del risultato confrontando la risposta del passo 2 con quanto definito nel punto 1        
        Assertions.assertEquals(resultAtteso,result);
    }

    @Test
    @DisplayName("dividiPerZero")
    public void dividiPerZero(){
        //1) Arrange: la definizione di tutti i dati di partenza 
        Double divisore=new Double(0);
        Double dividendo=new Double(5.0);
        //3) Assert: la validazione del risultato confrontando la risposta del passo 2 con quanto definito nel punto 1        
        Assertions.assertThrows(ArithmeticException.class,
            ()->{ //2) Act: la chiamata alla operazione da testare (di solito è una sola operazione ma può essere anche multipla)
                App.dividi(dividendo, divisore);
            }
        );
    }

    @Test
    @BeforeAll
    @DisplayName("primaDitutti")
    public static void primaDitutti(){
        Assertions.assertTrue(true);
    }
    
}
