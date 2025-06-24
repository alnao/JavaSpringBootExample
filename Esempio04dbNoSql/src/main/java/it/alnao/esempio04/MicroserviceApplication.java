package it.alnao.esempio04;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class MicroserviceApplication /*implements CommandLineRunner*/ {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceApplication.class);

    public static void main(String[] args) {
        log.info("MicroserviceApplication main starting v8.0.0");
        SpringApplication.run(MicroserviceApplication.class, args);
    }

    /*
    //@Value("${spring.data.mongodb.uri}") // Inietta il valore della proprietà
    //private String mongodb;

    @Override 
    public void run(String... args) throws Exception {
        // Questo metodo viene eseguito una volta che l'applicazione è completamente avviata
        log.info("Valore della proprietà 'spring.data.mongodb.uri': {}", mongodb);
    }
    */
}