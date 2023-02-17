package it.alnao.examples;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void dividiNormale(){
        //1) Arrange: la definizione di tutti i dati di partenza 
        Double dividendo=new Double(5.0);
        Double divisore=new Double(2.0);
        Double resultAtteso=new Double(2.5);
        //2) Act: la chiamata alla operazione da testare (di solito è una sola operazione ma può essere anche multipla)
        try{
            Double result=App.dividi(dividendo, divisore);
            //3) Assert: la validazione del risultato confrontando la risposta del passo 2 con quanto definito nel punto 1        
            assertEquals(resultAtteso,result);
        }catch (Exception e){
            assertTrue( false ); //error if exception
        }
    }

    @Test
    public void dividiPerZero(){
        //1) Arrange: la definizione di tutti i dati di partenza 
        Double dividendo=new Double(5.0);
        Double divisore=new Double(0);
        //2) Act: la chiamata alla operazione da testare (di solito è una sola operazione ma può essere anche multipla)
        try{
            App.dividi(dividendo, divisore);
            //3) Assert: la validazione del risultato confrontando la risposta del passo 2 con quanto definito nel punto 1        
            assertTrue( false ); //errore senza exception
        }catch (ArithmeticException e){    
            assertTrue( true ); //ok se ArithmeticException
        }catch (Exception e){
            assertTrue( false ); //errore se altre  exception
        }
    }

}
