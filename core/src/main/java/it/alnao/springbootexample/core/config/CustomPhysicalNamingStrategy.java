package it.alnao.springbootexample.core.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomPhysicalNamingStrategy implements PhysicalNamingStrategy {
    private final String annotazioniMetadataTableName;
    private final String refreshTokenTableName;
    private final String userProviderTableName;
    private final String userTableName;

    public CustomPhysicalNamingStrategy() {
        this.annotazioniMetadataTableName = System.getProperty("gestione-annotazioni.sql.annotazioni-metadata-table-name", "annotazioni_metadata");
        this.refreshTokenTableName = System.getProperty("gestione-annotazioni.sql.refresh-token-table-name", "refresh_tokens");
        this.userProviderTableName = System.getProperty("gestione-annotazioni.sql.user-provider-table-name", "user_providers");
        this.userTableName = System.getProperty("gestione-annotazioni.sql.user-table-name", "users");
    }

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
        switch (logicalTableName) {
            case "nome_tabella_annotazione_metadata":
                return Identifier.toIdentifier(annotazioniMetadataTableName);
            case "nome_tabella_refresh_token":
                return Identifier.toIdentifier(refreshTokenTableName);
            case "nome_tabella_user_provider":
                return Identifier.toIdentifier(userProviderTableName);
            case "nome_tabella_user":
                return Identifier.toIdentifier(userTableName);
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
