#!/bin/bash
# Controllo statico dei tag AWS negli script sh (change openspec verifica-tag-script-aws).
#
# Per ogni comando che crea una risorsa (aws <servizio> create-*, ec2 run-instances,
# ec2 allocate-address, ecs run-task, ecs register-task-definition, eksctl create) verifica
# che i sei tag standard arrivino dalle funzioni di script/aws-tags.sh. Un comando e' conforme se:
#   1. nel comando compare aws_tags_kv|aws_tags_map|aws_tags_json|aws_tags_spec, oppure
#      "terraform output -raw <nome>_tags_json" (tag standard generati da Terraform);
#   2. passa --cli-input-json file://<percorso> e lo stesso script scrive quel percorso con un
#      heredoc che contiene aws_tags_json;
#   3. non ha tag propri e lo stesso script contiene il comando di tagging dedicato previsto per
#      quel tipo di risorsa (es. create-queue -> sqs tag-queue), che usa le funzioni condivise.
# Tag presenti ma scritti a mano (senza le funzioni condivise) non sono mai conformi.
# Una forma nuova di tagging va aggiunta qui, nella change che la introduce.
#
# Nessuna chiamata verso AWS: il controllo legge soltanto i file.
#
# Uso (dalla root del progetto):
#   ./script/automatic-test/test-tag-aws-script.sh               # analizza script/
#   ./script/automatic-test/test-tag-aws-script.sh <file|cartella>...

# --- Logging: scrive su automatic-test-YYYYMMDD.log
if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE

# Conteggio dei test e riepilogo finale
source "$(dirname "$0")/lib-report.sh"
report_run_inizio "tag aws (statico)"
trap 'RC=$?; report_chiusura $RC' EXIT

echo "[$(date '+%Y-%m-%d %H:%M:%S')] === INIZIO: test-tag-aws-script.sh ==="

# Elenco dei file da analizzare: argomenti (file o cartelle) oppure script/ per default
if [ $# -eq 0 ]; then
    set -- script
fi
FILES=$(for target in "$@"; do
    if [ -d "$target" ]; then
        find "$target" -type f -name '*.sh' \
            -not -path '*/.terraform/*' \
            -not -path '*/azure-onprem-non-funziona/*'
    elif [ -f "$target" ]; then
        echo "$target"
    else
        echo "Percorso non trovato: $target" >&2
    fi
done | sort -u)

# Analisi di un file: stampa una riga "KO<TAB>riga<TAB>comando<TAB>motivo" per ogni comando non
# conforme e "OK<TAB>riga<TAB>comando" per ogni comando conforme.
analizza_file() {
    awk '
    function normalizza(p) { sub(/^\.\//, "", p); return p }
    # Vero se la riga lascia aperto un apice singolo (es. JSON su piu righe dentro apici),
    # ignorando gli apostrofi fra virgolette doppie, quelli preceduti da backslash e i commenti.
    function apice_aperto(s,    i, ch, prec, sq, dq) {
        sq = 0; dq = 0; prec = " "
        for (i = 1; i <= length(s); i++) {
            ch = substr(s, i, 1)
            if (!sq && ch == "\\") { i++; prec = "x"; continue }
            if (!sq && ch == "\"") dq = !dq
            else if (!dq && ch == "\047") sq = !sq
            else if (!sq && !dq && ch == "#" && prec ~ /[ \t]/) break
            prec = ch
        }
        return sq
    }
    # Comando di tagging dedicato accettato per ogni tipo di creazione (regola 3)
    BEGIN {
        dedicato["ec2 create-security-group"] = "ec2 create-tags"
        dedicato["ec2 allocate-address"]      = "ec2 create-tags"
        dedicato["sqs create-queue"]          = "sqs tag-queue"
        dedicato["iam create-role"]           = "iam tag-role"
        dedicato["iam create-instance-profile"] = "iam tag-instance-profile"
        dedicato["dynamodb create-table"]     = "dynamodb tag-resource"
        dedicato["ecr create-repository"]     = "ecr tag-resource"
        dedicato["ecs create-cluster"]        = "ecs tag-resource"
        dedicato["ecs create-service"]        = "ecs tag-resource"
        dedicato["logs create-log-group"]     = "logs tag-log-group"
        dedicato["rds create-db-cluster"]     = "rds add-tags-to-resource"
        dedicato["rds create-db-instance"]    = "rds add-tags-to-resource"
        dedicato["elasticache create-cache-cluster"] = "elasticache add-tags-to-resource"
        dedicato["elasticache create-cache-subnet-group"] = "elasticache add-tags-to-resource"
        n = 0; heredoc_fine = ""
    }
    {
        riga = $0
        # Dentro un heredoc che scrive un file: annota se contiene i tag condivisi (regola 2)
        if (heredoc_fine != "") {
            if (riga ~ /aws_tags_json/) json_taggato[heredoc_file] = 1
            if (riga == heredoc_fine) heredoc_fine = ""
            next
        }
        if (match(riga, /cat[ \t]*>[ \t]*[^ \t<]+[ \t]*<<-?[ \t]*["\047]?[A-Za-z_]+/)) {
            h = substr(riga, RSTART, RLENGTH)
            f = h; sub(/^cat[ \t]*>[ \t]*/, "", f); sub(/[ \t]*<<.*/, "", f)
            d = h; sub(/.*<<-?[ \t]*["\047]?/, "", d)
            heredoc_file = normalizza(f); heredoc_fine = d
            next
        }
        if (riga ~ /^[ \t]*#/) next
        # Unione delle righe continuate con "\" o con un apice singolo ancora aperto
        inizio = NR
        while ((riga ~ /\\[ \t]*$/ || apice_aperto(riga)) && (getline prossima) > 0) {
            sub(/\\[ \t]*$/, " ", riga); riga = riga " " prossima
        }
        usa_funzioni = (riga ~ /aws_tags_(kv|map|json|spec)/ || riga ~ /terraform output -raw [A-Za-z0-9_]*tags_json/)

        # Comandi di tagging dedicati che usano le funzioni condivise
        resto = riga
        while (match(resto, /aws[ \t]+[a-z0-9-]+[ \t]+(create-tags|tag-[a-z-]+|add-tags-to-resource)/)) {
            t = substr(resto, RSTART, RLENGTH); gsub(/[ \t]+/, " ", t); sub(/^aws /, "", t)
            if (usa_funzioni) tagging[t] = 1
            resto = substr(resto, RSTART + RLENGTH)
        }

        # Comandi di creazione
        resto = riga
        while (match(resto, /(aws[ \t]+[a-z0-9-]+[ \t]+(create-[a-z0-9-]+|run-instances|allocate-address|run-task|register-task-definition)|eksctl[ \t]+create[ \t]+[a-z]+)/)) {
            c = substr(resto, RSTART, RLENGTH); gsub(/[ \t]+/, " ", c); sub(/^aws /, "", c)
            resto = substr(resto, RSTART + RLENGTH)
            if (c ~ / create-tags$/) continue
            n++; cmd[n] = c; riga_cmd[n] = inizio; funzioni[n] = usa_funzioni
            tag_propri[n] = (riga ~ /--tags?[ \t=]|--tag-specifications/)
            json[n] = ""
            if (match(riga, /--cli-input-json[ \t]+file:\/\/[^ \t"]+/)) {
                j = substr(riga, RSTART, RLENGTH); sub(/.*file:\/\//, "", j); json[n] = normalizza(j)
            }
        }
    }
    END {
        for (i = 1; i <= n; i++) {
            c = cmd[i]
            if (funzioni[i]) { print "OK\t" riga_cmd[i] "\t" c; continue }
            if (json[i] != "" && json_taggato[json[i]]) { print "OK\t" riga_cmd[i] "\t" c; continue }
            if (tag_propri[i]) { print "KO\t" riga_cmd[i] "\t" c "\ttag scritti a mano, non dalle funzioni di aws-tags.sh"; continue }
            if ((c in dedicato) && tagging[dedicato[c]]) { print "OK\t" riga_cmd[i] "\t" c; continue }
            print "KO\t" riga_cmd[i] "\t" c "\tnessun tag standard"
        }
    }' "$1"
}

FILE_ANALIZZATI=0
FILE_CON_CREAZIONI=0
for file in $FILES; do
    FILE_ANALIZZATI=$((FILE_ANALIZZATI + 1))
    ESITI=$(analizza_file "$file")
    [ -z "$ESITI" ] && continue
    FILE_CON_CREAZIONI=$((FILE_CON_CREAZIONI + 1))
    TOTALE=$(echo "$ESITI" | wc -l)
    NON_CONFORMI=$(echo "$ESITI" | awk -F'\t' '$1 == "KO"')
    if [ -z "$NON_CONFORMI" ]; then
        test_ok "Tag AWS conformi in $file ($TOTALE comandi di creazione)"
    else
        test_ko "Tag AWS non conformi in $file"
        echo "$NON_CONFORMI" | awk -F'\t' -v f="$file" '{ printf "   - %s:%s  %s: %s\n", f, $2, $3, $4 }'
    fi
done

echo "File analizzati: $FILE_ANALIZZATI, con comandi di creazione: $FILE_CON_CREAZIONI"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === FINE: test-tag-aws-script.sh ==="

[ "$(report_ko_locali)" -eq 0 ]
