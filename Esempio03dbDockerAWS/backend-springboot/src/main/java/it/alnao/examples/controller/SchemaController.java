package  it.alnao.examples.controller;

import org.hibernate.Session;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Statement;


@RestController
@RequestMapping("/admin/schema")
public class SchemaController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/create")
    public String createSchemaAndTable() {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
//                stmt.execute("CREATE DATABASE IF NOT EXISTS informazioni");
//                stmt.execute("USE informazioni");
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS Persone (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        nome VARCHAR(255),
                        cognome VARCHAR(255),
                        eta INT
                    )
                """);
            }
        });
        return "Schema creato!";
    }

    @PostMapping("/truncate")
    public String truncateTable() {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("TRUNCATE TABLE informazioni.Persone");
            }
        });
        return "Tabella svuotata!";
    }
}