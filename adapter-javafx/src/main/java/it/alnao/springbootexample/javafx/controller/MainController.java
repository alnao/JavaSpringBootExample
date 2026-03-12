package it.alnao.springbootexample.javafx.controller;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.javafx.model.AnnotazioneViewModel;
import it.alnao.springbootexample.javafx.service.ViewModelConverterService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller principale per la gestione delle annotazioni
 */
@Component
public class MainController {

    @FXML private TableView<AnnotazioneViewModel> annotazioniTable;
    @FXML private TableColumn<AnnotazioneViewModel, String> descrizioneColumn;
    @FXML private TableColumn<AnnotazioneViewModel, String> statoColumn;
    @FXML private TableColumn<AnnotazioneViewModel, String> categoriaColumn;
    @FXML private TableColumn<AnnotazioneViewModel, Integer> prioritaColumn;
    @FXML private TableColumn<AnnotazioneViewModel, String> utenteColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoriaFilter;
    @FXML private ComboBox<String> statoFilter;
    
    @FXML private TextArea valoreNotaArea;
    @FXML private TextField descrizioneField;
    @FXML private TextField categoriaField;
    @FXML private Spinner<Integer> prioritaSpinner;
    @FXML private CheckBox pubblicaCheckBox;
    @FXML private TextField tagsField;
    @FXML private ComboBox<String> statoComboBox;
    
    @FXML private Button creaButton;
    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;
    @FXML private Button salvaButton;
    @FXML private Button annullaButton;
    
    @FXML private Label statusLabel;
    @FXML private Label currentUserLabel;

    private final AnnotazioneService annotazioneService;
    private final ViewModelConverterService converterService;
    
    private ObservableList<AnnotazioneViewModel> annotazioni;
    private String currentUser;
    private UUID selectedAnnotazioneId;
    private boolean isEditMode = false;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MainController(AnnotazioneService annotazioneService, 
                         ViewModelConverterService converterService) {
        this.annotazioneService = annotazioneService;
        this.converterService = converterService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupDetailPanel();
        setupListeners();
        
        // Inizializza la tabella vuota
        annotazioni = FXCollections.observableArrayList();
        annotazioniTable.setItems(annotazioni);
        
        setEditMode(false);
    }

    private void setupTableColumns() {
        descrizioneColumn.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        statoColumn.setCellValueFactory(new PropertyValueFactory<>("stato"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        prioritaColumn.setCellValueFactory(new PropertyValueFactory<>("priorita"));
        utenteColumn.setCellValueFactory(new PropertyValueFactory<>("utenteCreazione"));
    }

    private void setupFilters() {
        // Popola filtro stato
        statoFilter.setItems(FXCollections.observableArrayList(
            "TUTTI",
            StatoAnnotazione.INSERITA.getValue(),
            StatoAnnotazione.MODIFICATA.getValue(),
            StatoAnnotazione.CONFERMATA.getValue(),
            StatoAnnotazione.RIFIUTATA.getValue(),
            StatoAnnotazione.DAINVIARE.getValue(),
            StatoAnnotazione.INVIATA.getValue(),
            StatoAnnotazione.SCADUTA.getValue(),
            StatoAnnotazione.BANNATA.getValue(),
            StatoAnnotazione.ERRORE.getValue()
        ));
        statoFilter.setValue("TUTTI");
        
        // Popola categorie (hardcoded per ora, potrebbe venire da DB)
        categoriaFilter.setItems(FXCollections.observableArrayList(
            "TUTTE", "Lavoro", "Personale", "Studio", "Altro"
        ));
        categoriaFilter.setValue("TUTTE");
    }

    private void setupDetailPanel() {
        // Setup spinner priorità
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5);
        prioritaSpinner.setValueFactory(valueFactory);
        
        // Popola ComboBox stati
        statoComboBox.setItems(FXCollections.observableArrayList(
            StatoAnnotazione.INSERITA.getValue(),
            StatoAnnotazione.MODIFICATA.getValue(),
            StatoAnnotazione.CONFERMATA.getValue(),
            StatoAnnotazione.RIFIUTATA.getValue(),
            StatoAnnotazione.DAINVIARE.getValue(),
            StatoAnnotazione.INVIATA.getValue(),
            StatoAnnotazione.SCADUTA.getValue(),
            StatoAnnotazione.BANNATA.getValue(),
            StatoAnnotazione.ERRORE.getValue()
        ));
    }

    private void setupListeners() {
        // Listener selezione tabella
        annotazioniTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null && !isEditMode) {
                    loadAnnotazioneDetails(newSelection);
                }
            }
        );
        
        // Listener filtri
        statoFilter.setOnAction(e -> applyFilters());
        categoriaFilter.setOnAction(e -> applyFilters());
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
        currentUserLabel.setText("Utente: " + username);
        loadAnnotazioni();
    }

    private void loadAnnotazioni() {
        try {
            List<AnnotazioneCompleta> list = annotazioneService.trovaTutte();
            annotazioni.clear();
            annotazioni.addAll(
                list.stream()
                    .map(converterService::toViewModel)
                    .collect(Collectors.toList())
            );
            updateStatus("Caricate " + annotazioni.size() + " annotazioni");
        } catch (Exception e) {
            showError("Errore nel caricamento delle annotazioni: " + e.getMessage());
        }
    }

    private void loadAnnotazioneDetails(AnnotazioneViewModel viewModel) {
        selectedAnnotazioneId = viewModel.getId();
        valoreNotaArea.setText(viewModel.getValoreNota());
        descrizioneField.setText(viewModel.getDescrizione());
        categoriaField.setText(viewModel.getCategoria());
        prioritaSpinner.getValueFactory().setValue(viewModel.getPriorita());
        pubblicaCheckBox.setSelected(viewModel.getPubblica());
        tagsField.setText(viewModel.getTags());
        statoComboBox.setValue(viewModel.getStato());
    }

    private void clearDetailPanel() {
        selectedAnnotazioneId = null;
        valoreNotaArea.clear();
        descrizioneField.clear();
        categoriaField.clear();
        prioritaSpinner.getValueFactory().setValue(5);
        pubblicaCheckBox.setSelected(false);
        tagsField.clear();
        statoComboBox.setValue(StatoAnnotazione.INSERITA.getValue());
    }

    @FXML
    private void handleCrea() {
        clearDetailPanel();
        setEditMode(true);
        selectedAnnotazioneId = null;
        updateStatus("Nuova annotazione");
    }

    @FXML
    private void handleModifica() {
        AnnotazioneViewModel selected = annotazioniTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            setEditMode(true);
            updateStatus("Modifica annotazione in corso");
        } else {
            showError("Seleziona un'annotazione da modificare");
        }
    }

    @FXML
    private void handleElimina() {
        AnnotazioneViewModel selected = annotazioniTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleziona un'annotazione da eliminare");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare l'annotazione?");
        alert.setContentText("Descrizione: " + selected.getDescrizione());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                annotazioneService.eliminaAnnotazione(selected.getId());
                loadAnnotazioni();
                clearDetailPanel();
                updateStatus("Annotazione eliminata");
            } catch (Exception e) {
                showError("Errore nell'eliminazione: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSalva() {
        try {
            String valoreNota = valoreNotaArea.getText();
            String descrizione = descrizioneField.getText();
            
            if (valoreNota.isEmpty() || descrizione.isEmpty()) {
                showError("Valore nota e descrizione sono obbligatori");
                return;
            }

            if (selectedAnnotazioneId == null) {
                // Crea nuova annotazione
                AnnotazioneCompleta nuova = annotazioneService.creaAnnotazione(
                    valoreNota, descrizione, currentUser
                );
                
                // Aggiorna metadata opzionali
                updateMetadata(nuova.getAnnotazione().getId());
                
                updateStatus("Annotazione creata con successo");
            } else {
                // Aggiorna annotazione esistente
                annotazioneService.aggiornaAnnotazione(
                    selectedAnnotazioneId, valoreNota, descrizione, currentUser
                );
                
                // Aggiorna metadata opzionali
                updateMetadata(selectedAnnotazioneId);
                
                updateStatus("Annotazione aggiornata con successo");
            }
            
            loadAnnotazioni();
            setEditMode(false);
            
        } catch (Exception e) {
            showError("Errore nel salvataggio: " + e.getMessage());
        }
    }

    private void updateMetadata(UUID annotazioneId) {
        String categoria = categoriaField.getText();
        if (!categoria.isEmpty()) {
            annotazioneService.impostaCategoria(annotazioneId, categoria, currentUser);
        }
        
        Integer priorita = prioritaSpinner.getValue();
        annotazioneService.impostaPriorita(annotazioneId, priorita, currentUser);
        
        boolean pubblica = pubblicaCheckBox.isSelected();
        annotazioneService.impostaVisibilitaPubblica(annotazioneId, pubblica, currentUser);
        
        String tags = tagsField.getText();
        if (!tags.isEmpty()) {
            annotazioneService.impostaTags(annotazioneId, tags, currentUser);
        }
        
        String stato = statoComboBox.getValue();
        if (stato != null && !stato.isEmpty()) {
            annotazioneService.cambiaStato(annotazioneId, stato, currentUser);
        }
    }

    @FXML
    private void handleAnnulla() {
        AnnotazioneViewModel selected = annotazioniTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadAnnotazioneDetails(selected);
        } else {
            clearDetailPanel();
        }
        setEditMode(false);
        updateStatus("Operazione annullata");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadAnnotazioni();
        } else {
            try {
                List<AnnotazioneCompleta> results = annotazioneService.cercaPerTesto(searchText);
                annotazioni.clear();
                annotazioni.addAll(
                    results.stream()
                        .map(converterService::toViewModel)
                        .collect(Collectors.toList())
                );
                updateStatus("Trovate " + annotazioni.size() + " annotazioni");
            } catch (Exception e) {
                showError("Errore nella ricerca: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadAnnotazioni();
    }

    private void applyFilters() {
        try {
            String statoSelezionato = statoFilter.getValue();
            String categoriaSelezionata = categoriaFilter.getValue();
            
            List<AnnotazioneCompleta> results = annotazioneService.trovaTutte();
            
            // Applica filtro stato
            if (!"TUTTI".equals(statoSelezionato)) {
                results = results.stream()
                    .filter(a -> statoSelezionato.equals(a.getMetadata().getStato()))
                    .collect(Collectors.toList());
            }
            
            // Applica filtro categoria
            if (!"TUTTE".equals(categoriaSelezionata)) {
                results = results.stream()
                    .filter(a -> categoriaSelezionata.equals(a.getMetadata().getCategoria()))
                    .collect(Collectors.toList());
            }
            
            annotazioni.clear();
            annotazioni.addAll(
                results.stream()
                    .map(converterService::toViewModel)
                    .collect(Collectors.toList())
            );
            
            updateStatus("Filtro applicato: " + annotazioni.size() + " risultati");
            
        } catch (Exception e) {
            showError("Errore nell'applicazione dei filtri: " + e.getMessage());
        }
    }

    private void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        
        valoreNotaArea.setEditable(editMode);
        descrizioneField.setEditable(editMode);
        categoriaField.setEditable(editMode);
        prioritaSpinner.setDisable(!editMode);
        pubblicaCheckBox.setDisable(!editMode);
        tagsField.setEditable(editMode);
        statoComboBox.setDisable(!editMode);
        
        salvaButton.setDisable(!editMode);
        annullaButton.setDisable(!editMode);
        
        creaButton.setDisable(editMode);
        modificaButton.setDisable(editMode);
        eliminaButton.setDisable(editMode);
        annotazioniTable.setDisable(editMode);
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
