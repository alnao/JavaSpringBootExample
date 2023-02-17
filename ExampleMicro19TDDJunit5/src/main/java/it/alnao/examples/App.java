package it.alnao.examples;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    public static Double dividi(Double divisore, Double dividendo) throws Exception{
        if (divisore==null || dividendo==null){
            throw new NullPointerException("Parametro null");
        }
        if (dividendo.equals(new Double(0.0))){
            throw new ArithmeticException();
        }
        return new Double(divisore.doubleValue() / dividendo.doubleValue());
    }
}
