package it.alnao.springbootexample.port.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Autowired
    private SqlTableConfig sqlTableConfig;

    @Override
    public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName == null) {
            return null;
        }
        
        String logicalTableName = logicalName.getText();
        
        // Mappa i nomi logici ai nomi configurati
        switch (logicalTableName) {
            case "nome_tabella_annotazione_metadata":
                return Identifier.toIdentifier(sqlTableConfig.getAnnotazioniMetadataTableName());
            default:
                return logicalName;
        }
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }
}
