package it.alnao.springbootexample.core.config;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomPhysicalNamingStrategyTest {

    private final CustomPhysicalNamingStrategy strategy = new CustomPhysicalNamingStrategy();

    @Test
    void toPhysicalTableName_mapsThePlaceholdersToTheDefaultTableNames() {
        assertEquals("annotazioni_metadata", strategy.toPhysicalTableName(
                Identifier.toIdentifier("nome_tabella_annotazione_metadata"), null).getText());
        assertEquals("refresh_tokens", strategy.toPhysicalTableName(
                Identifier.toIdentifier("nome_tabella_refresh_token"), null).getText());
        assertEquals("user_providers", strategy.toPhysicalTableName(
                Identifier.toIdentifier("nome_tabella_user_provider"), null).getText());
        assertEquals("users", strategy.toPhysicalTableName(
                Identifier.toIdentifier("nome_tabella_user"), null).getText());
    }

    @Test
    void toPhysicalTableName_leavesAnyOtherNameUntouched() {
        Identifier logical = Identifier.toIdentifier("una_tabella_qualsiasi");
        assertSame(logical, strategy.toPhysicalTableName(logical, null));
    }

    @Test
    void toPhysicalTableName_withNull_returnsNull() {
        assertNull(strategy.toPhysicalTableName(null, null));
    }

    @Test
    void theOtherNameMappingsAreIdentity() {
        Identifier logical = Identifier.toIdentifier("qualcosa");
        assertSame(logical, strategy.toPhysicalCatalogName(logical, null));
        assertSame(logical, strategy.toPhysicalSchemaName(logical, null));
        assertSame(logical, strategy.toPhysicalSequenceName(logical, null));
        assertSame(logical, strategy.toPhysicalColumnName(logical, null));
    }

    @Test
    void systemPropertiesOverrideTheDefaultTableNames() {
        System.setProperty("gestione-annotazioni.sql.user-table-name", "utenti_custom");
        try {
            CustomPhysicalNamingStrategy custom = new CustomPhysicalNamingStrategy();
            assertEquals("utenti_custom", custom.toPhysicalTableName(
                    Identifier.toIdentifier("nome_tabella_user"), null).getText());
        } finally {
            System.clearProperty("gestione-annotazioni.sql.user-table-name");
        }
    }
}
