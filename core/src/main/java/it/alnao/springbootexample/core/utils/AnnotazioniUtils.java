package it.alnao.springbootexample.core.utils;

public class AnnotazioniUtils {
    /**
     * Incrementa la versione da stringa (es. "1.0" -> "1.1", "v2.3" -> "v2.4")
     */
    public static String incrementaVersione(String versioneCorrente) {
        if (versioneCorrente == null || versioneCorrente.isEmpty()) {
            return "1.0";
        }
        
        // Cerca pattern come "1.0", "2.3", "v1.5"
        if (versioneCorrente.matches("^v?\\d+\\.\\d+$")) {
            String prefix = versioneCorrente.startsWith("v") ? "v" : "";
            String numberPart = versioneCorrente.replace("v", "");
            String[] parts = numberPart.split("\\.");
            
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                minor++;
                return prefix + major + "." + minor;
            } catch (NumberFormatException e) {
                // Fallback: aggiungi .1
                return versioneCorrente + ".1";
            }
        } else {
            // Per versioni non standard, aggiungi .1
            return versioneCorrente + ".1";
        }
    }
}
