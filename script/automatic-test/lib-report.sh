#!/bin/bash
# Libreria condivisa per il conteggio dei test e il report finale.
#
# Ogni script di test la carica con:
#   source "$(dirname "$0")/lib-report.sh"
#
# I test si registrano con test_ok / test_ko, che stampano il messaggio come
# prima e in più annotano l'esito in un registro condiviso. Il registro è un
# file temporaneo il cui percorso viaggia in REPORT_FILE: gli script figli lo
# ereditano dall'ambiente, così i loro test finiscono nel conteggio del profilo
# che li ha invocati.
#
# Alla fine dell'esecuzione lo script che ha creato il registro stampa il
# riepilogo, che finisce nel log automatic-test-YYYYMMDD.log come tutto il resto.

# Colori: definiti solo se non già presenti nello script chiamante
: "${GREEN:=\033[0;32m}"
: "${RED:=\033[0;31m}"
: "${YELLOW:=\033[1;33m}"
: "${BLUE:=\033[0;34m}"
: "${NC:=\033[0m}"

# Registro condiviso. Chi lo crea è anche chi stamperà il riepilogo finale.
if [ -z "$REPORT_FILE" ]; then
    REPORT_FILE="/tmp/automatic-test-report-$$.tsv"
    : > "$REPORT_FILE"
    REPORT_OWNER="$$"
fi
export REPORT_FILE
export REPORT_OWNER

# Numero di test falliti da QUESTO script: non ereditato, serve a decidere il
# proprio exit code senza contare i fallimenti di altri script.
REPORT_KO_LOCALI=0

# Profilo a cui attribuire i test che seguono. Ereditato dagli script figli.
: "${PROFILO_CORRENTE:=standalone}"
export PROFILO_CORRENTE

# --- Registrazione dei singoli test -----------------------------------------

# test_ok "descrizione"  → test superato
test_ok() {
    echo -e "${GREEN}✓ $1${NC}"
    printf 'TEST\t%s\tOK\t%s\n' "$PROFILO_CORRENTE" "$1" >> "$REPORT_FILE"
}

# test_ko "descrizione"  → test fallito. Non interrompe: sta al chiamante
# decidere se proseguire o uscire, così un run può contare più fallimenti.
test_ko() {
    echo -e "${RED}✗ $1${NC}"
    printf 'TEST\t%s\tKO\t%s\n' "$PROFILO_CORRENTE" "$1" >> "$REPORT_FILE"
    REPORT_KO_LOCALI=$((REPORT_KO_LOCALI + 1))
}

# --- Registrazione delle esecuzioni di profilo ------------------------------

# report_run_inizio "nome-profilo"
report_run_inizio() {
    PROFILO_CORRENTE="$1"
    export PROFILO_CORRENTE
    REPORT_RUN_START=$(date +%s)
    REPORT_RUN_START_HR=$(date '+%H:%M:%S')
    printf 'INIZIO\t%s\t-\t%s\n' "$PROFILO_CORRENTE" "$REPORT_RUN_START_HR" >> "$REPORT_FILE"
}

# report_run_fine <exit_code>
report_run_fine() {
    local rc="${1:-0}"
    local esito="OK"
    [ "$rc" -ne 0 ] && esito="KO"
    local durata="-"
    if [ -n "$REPORT_RUN_START" ]; then
        local secondi=$(( $(date +%s) - REPORT_RUN_START ))
        durata=$(printf '%dm%02ds' $((secondi / 60)) $((secondi % 60)))
    fi
    printf 'RUN\t%s\t%s\t%s\t%s\t%s\n' \
        "$PROFILO_CORRENTE" "$esito" "${REPORT_RUN_START_HR:--}" "$(date '+%H:%M:%S')" "$durata" \
        >> "$REPORT_FILE"
}

# --- Riepilogo finale --------------------------------------------------------

report_finale() {
    [ -s "$REPORT_FILE" ] || return 0

    echo ""
    echo -e "${BLUE}========================================================================${NC}"
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] RIEPILOGO ESECUZIONE TEST${NC}"
    echo -e "${BLUE}========================================================================${NC}"

    echo ""
    echo "Profili eseguiti:"
    awk -F'\t' '$1 == "RUN" { righe[++n] = $0; if (length($2) > larghezza) larghezza = length($2) }
    END {
        if (n == 0) { print "  (nessuna esecuzione di profilo registrata)"; exit }
        if (larghezza < 20) larghezza = 20
        for (i = 1; i <= n; i++) {
            split(righe[i], c, "\t")
            printf "  %d) %-*s  %s → %s  %-9s %s\n", i, larghezza, c[2], c[4], c[5], "(" c[6] ")", c[3]
        }
    }' "$REPORT_FILE"

    echo ""
    echo "Test per profilo:"
    awk -F'\t' '
    $1 == "TEST" {
        if (!($2 in visti)) { ordine[++n] = $2; visti[$2] = 1 }
        totale[$2]++
        if ($3 == "KO") ko[$2]++
        tot++
        if ($3 == "KO") tot_ko++
        if (length($2) > larghezza) larghezza = length($2)
    }
    function riga(w,   i, s) { s = "  +"; for (i = 0; i < w + 2; i++) s = s "-"; return s "+---------+---------+---------+" }
    END {
        if (n == 0) { print "  (nessun test registrato)"; exit }
        if (larghezza < 20) larghezza = 20
        print riga(larghezza)
        printf "  | %-*s | %7s | %7s | %7s |\n", larghezza, "Profilo", "Test", "OK", "KO"
        print riga(larghezza)
        for (i = 1; i <= n; i++) {
            p = ordine[i]
            k = (p in ko) ? ko[p] : 0
            printf "  | %-*s | %7d | %7d | %7d |\n", larghezza, p, totale[p], totale[p] - k, k
        }
        print riga(larghezza)
        printf "  | %-*s | %7d | %7d | %7d |\n", larghezza, "TOTALE", tot, tot - tot_ko, tot_ko
        print riga(larghezza)
    }' "$REPORT_FILE"

    local ko_totali
    ko_totali=$(awk -F'\t' '$1 == "TEST" && $3 == "KO" { n++ } END { print n + 0 }' "$REPORT_FILE")

    echo ""
    if [ "$ko_totali" -gt 0 ]; then
        echo "Test falliti:"
        awk -F'\t' '$1 == "TEST" && $3 == "KO" { printf "  - [%s] %s\n", $2, $4 }' "$REPORT_FILE"
        echo ""
        if [ "$ko_totali" -eq 1 ]; then
            echo -e "${RED}ESITO COMPLESSIVO: 1 test fallito${NC}"
        else
            echo -e "${RED}ESITO COMPLESSIVO: $ko_totali test falliti${NC}"
        fi
    else
        echo -e "${GREEN}ESITO COMPLESSIVO: tutti i test superati${NC}"
    fi
    echo -e "${BLUE}========================================================================${NC}"
    echo ""
}

# Da richiamare nel trap EXIT: chiude l'esecuzione corrente e, se questo script
# possiede il registro, stampa il riepilogo e lo rimuove.
report_chiusura() {
    local rc="${1:-0}"
    [ -n "$REPORT_RUN_START" ] && report_run_fine "$rc"
    if [ "$REPORT_OWNER" = "$$" ]; then
        report_finale
        rm -f "$REPORT_FILE"
    fi
}

# Numero di test falliti da questo script: serve a decidere il proprio exit code.
report_ko_locali() {
    echo "$REPORT_KO_LOCALI"
}

# Numero di test falliti in tutto il registro, compresi gli script figli.
report_ko_totali() {
    awk -F'\t' '$1 == "TEST" && $3 == "KO" { n++ } END { print n + 0 }' "$REPORT_FILE"
}
