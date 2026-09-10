## Context

Le transizioni vengono caricate una sola volta all'avvio, in un metodo di
inizializzazione del bean che valida le transizioni, leggendo un file dal
classpath e convertendo ogni voce in un oggetto di dominio. La conversione
risolve stati e ruoli tramite gli enum: una voce che nomina uno stato o un ruolo
inesistente solleva un errore di conversione.

Oggi l'intero caricamento e' racchiuso in un blocco che cattura qualunque
eccezione, logga un avviso e restituisce un insieme di ripiego con una sola
transizione. Vedi proposal.md - Why per le conseguenze.

Vincolo rilevante: la configurazione e' impacchettata nell'artefatto, uguale per
tutti i profili, e non e' sovrascrivibile per ambiente. Non c'e' quindi un caso
legittimo in cui un ambiente debba partire senza di essa.

## Goals / Non-Goals

**Goals:**

- Trasformare un guasto silenzioso in un errore di avvio diagnosticabile dal
  primo messaggio di log.
- Rendere identificabile la singola voce di configurazione che causa l'errore.
- Eliminare la seconda fonte di verita' rappresentata dal ripiego cablato.

**Non-Goals:**

- Rendere la configurazione delle transizioni sovrascrivibile per profilo o per
  ambiente: e' un tema a se', da valutare separatamente.
- Cambiare il meccanismo con cui il file viene letto e deserializzato.
- Ridefinire quali transizioni sono permesse: l'insieme delle regole resta quello
  attuale.
- Introdurre un endpoint di health dedicato allo stato della configurazione: se
  l'avvio fallisce, il probe esistente gia' non passa.

## Decisions

**Fallire durante l'inizializzazione del bean, non a valle.**
L'errore viene sollevato dallo stesso punto in cui oggi avviene il caricamento,
cosi' il contesto Spring non si completa e il processo termina con codice diverso
da zero. L'alternativa considerata era un controllo successivo all'avvio, che
avrebbe lasciato una finestra in cui l'applicazione accetta richieste con regole
non caricate; e' peggio dello stato attuale, non meglio.

**Un'eccezione dedicata invece di lasciar salire quella di conversione.**
L'errore di conversione di un enum non dice quale voce lo ha causato: il
messaggio nomina il valore ma non la posizione. Convertendo le voci una alla
volta e arricchendo il messaggio con l'indice e la descrizione della voce, chi
legge il log individua la riga da correggere senza aprire un debugger.
L'alternativa "lascia salire l'eccezione originale" costa meno codice ma
restituisce un messaggio che, con una configurazione lunga, non basta a
localizzare il problema.

**Insieme vuoto trattato come errore.**
Una configurazione che si carica ma non dichiara transizioni produce
un'applicazione che rifiuta ogni cambio di stato: e' lo stesso guasto di prima
con un'altra causa, e va trattato allo stesso modo. Il caso non ha usi legittimi:
un sistema in cui nessuno puo' cambiare stato ad alcuna annotazione non serve a
niente.

**Rimozione del ripiego, non sua correzione.**
Un ripiego "migliore", con un insieme ragionevole di transizioni cablato nel
codice, sembra piu' robusto ma introduce due descrizioni delle stesse regole che
divergono al primo cambiamento della configurazione, e maschera di nuovo il
guasto. Meglio nessun ripiego.

**Il file impacchettato viene verificato da un test.**
Un test del modulo `core` carica la configurazione realmente spedita e verifica
che si deserializzi e che ogni voce nomini stati e ruoli esistenti. E' la
contropartita del fail-fast: sposta la scoperta dell'errore dal deploy alla
build.

## Risks / Trade-offs

**Un errore di configurazione blocca il rilascio invece di degradarlo** → E'
l'effetto voluto, ma va reso innocuo in fase di build: il test sul file spedito
intercetta il caso normale (qualcuno modifica lo YAML e sbaglia) prima che
arrivi a un ambiente.

**Su Kubernetes, ECS e ACI il container entra in ciclo di riavvio** → Il
messaggio deve essere leggibile nei log del container senza attivare il debug: un
solo messaggio di errore, esplicito, prima della terminazione. Va verificato
sull'esecuzione reale del profilo `kube`, non solo nei test.

**Un ambiente oggi in esercizio con configurazione rotta smetterebbe di partire
dopo l'aggiornamento** → Situazione che oggi si manifesterebbe come cambi di
stato tutti rifiutati: chi si trovasse in quel caso lo scoprirebbe
all'aggiornamento. Vale la pena verificare i log degli ambienti attivi prima del
rilascio.

## Migration Plan

Nessuna migrazione di dati. Il rilascio segue il percorso ordinario: build,
immagine, deploy per profilo. In caso di problemi il rollback e' il ripristino
dell'immagine precedente, che riparte anche con configurazione non valida.
