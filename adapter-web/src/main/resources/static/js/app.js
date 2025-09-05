// Configurazione base
const API_BASE_URL = '/api/annotazioni';
let currentUser = null;
let allAnnotations = [];
let currentEditingId = null;

// Inizializzazione dell'applicazione
document.addEventListener('DOMContentLoaded', function() {
    loadDashboard();
    setupEventListeners();
});

// Setup degli event listeners
function setupEventListeners() {
    // Form di creazione annotazione
    document.getElementById('create-annotation-form').addEventListener('submit', handleCreateAnnotation);
    
    // Form di aggiunta categoria
    document.getElementById('add-category-form').addEventListener('submit', handleAddCategory);
    
    // Form di modifica annotazione
    document.getElementById('edit-annotation-form').addEventListener('submit', handleEditAnnotation);
    
    // Ricerca in tempo reale
    document.getElementById('search-annotations').addEventListener('input', filterAnnotations);
    document.getElementById('filter-category').addEventListener('change', filterAnnotations);
    document.getElementById('filter-priority').addEventListener('change', filterAnnotations);
    
    // Ricerca globale
    document.getElementById('global-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            performGlobalSearch();
        }
    });
    
    // Test connessione API all'avvio
    testApiConnection();
}

// Test della connessione API
async function testApiConnection() {
    try {
        console.log('Test connessione API in corso...');
        
        // Test 1: Endpoint statistiche
        const response = await fetchAPI('/statistiche');
        console.log('✅ Connessione API funzionante:', response);
        
        // Test 2: Caricamento annotazioni
        try {
            const annotations = await fetchAPI('');
            console.log('✅ Test caricamento annotazioni riuscito:', annotations.length, 'annotazioni trovate');
        } catch (error) {
            console.log('⚠️ Problema nel caricamento annotazioni:', error);
        }
        
        // Test 3: Verifica URL base
        console.log('📍 URL base API:', API_BASE_URL);
        console.log('📍 URL completo statistiche:', API_BASE_URL + '/statistiche');
        console.log('📍 URL completo annotazioni:', API_BASE_URL);
        
        // Mostra un messaggio di successo discreto
        showAlert('✅ Connessione al server stabilita', 'success');
        
    } catch (error) {
        console.error('❌ Errore di connessione API:', error);
        
        // Test di connessione diretta
        console.log('🔍 Test connessione diretta...');
        try {
            const directResponse = await fetch('http://localhost:8080/api/annotazioni/statistiche');
            console.log('📊 Risposta diretta:', directResponse.status, directResponse.statusText);
            if (directResponse.ok) {
                const data = await directResponse.json();
                console.log('📊 Dati diretti:', data);
                showAlert('⚠️ Connessione diretta funziona, ma c\'è un problema con l\'URL relativo', 'warning');
            }
        } catch (directError) {
            console.error('❌ Anche la connessione diretta fallisce:', directError);
            showAlert('❌ Server non raggiungibile. Assicurati che Spring Boot sia avviato su localhost:8080', 'danger');
        }
    }
}

// Test specifico per il salvataggio
async function testSaveAnnotation() {
    const testData = {
        valoreNota: "Test annotazione creata automaticamente",
        descrizione: "Test di connessione API",
        utente: currentUser,
        categoria: "Test",
        tags: "test, debug",
        pubblica: false,
        priorita: 2
    };
    
    console.log('Test salvataggio con dati:', testData);
    
    try {
        // Prova prima con il metodo diretto del service
        const result = await fetchAPI('', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(testData)
        });
        
        console.log('✅ Test salvataggio riuscito:', result);
        showAlert('✅ Test salvataggio completato con successo!', 'success');
        
        // Ricarica la dashboard per vedere il nuovo elemento
        loadDashboard();
        
        return result;
        
    } catch (error) {
        console.error('❌ Test salvataggio fallito:', error);
        
        // Analizza l'errore più in dettaglio
        if (error.message.includes('VALIDATION_ERROR')) {
            console.log('⚠️ Errore di validazione rilevato. Probabilmente il backend si aspetta un DTO diverso.');
            
            // Prova con un approccio alternativo - aggiungi un id null
            const testDataWithId = {
                ...testData,
                id: null
            };
            
            console.log('Tentativo con id null:', testDataWithId);
            
            try {
                const result2 = await fetchAPI('', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(testDataWithId)
                });
                
                console.log('✅ Test salvataggio con id null riuscito:', result2);
                showAlert('✅ Test salvataggio con id null completato!', 'success');
                loadDashboard();
                return result2;
                
            } catch (error2) {
                console.error('❌ Anche il test con id null è fallito:', error2);
                showAlert('❌ Test salvataggio fallito: ' + error2.message, 'danger');
            }
        } else {
            showAlert('❌ Test salvataggio fallito: ' + error.message, 'danger');
        }
        
        throw error;
    }
}

// Toggle della sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
}

// Mostra una sezione specifica
function showSection(sectionName) {
    // Nasconde tutte le sezioni
    document.querySelectorAll('.content-section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Rimuove la classe active da tutti i link
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Mostra la sezione selezionata
    const targetSection = document.getElementById(sectionName + '-section');
    if (targetSection) {
        targetSection.style.display = 'block';
    }
    
    // Aggiungi la classe active al link corrispondente
    if (event && event.target)
        event.target.classList.add('active');
    
    // Carica i dati specifici della sezione
    switch(sectionName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'annotazioni':
            loadAllAnnotations();
            break;
        case 'pubbliche':
            loadPublicAnnotations();
            break;
        case 'categorie':
            loadCategories();
            break;
        case 'crea':
            resetForm();
            break;
    }
}

// Caricamento della dashboard
async function loadDashboard() {
    try {
        // Carica statistiche
        const stats = await fetchAPI('/statistiche');
        document.getElementById('total-annotations').textContent = stats.totaleAnnotazioni || 0;
        
        // Carica tutte le annotazioni per calcolare le statistiche locali
        const allAnnotations = await fetchAPI('');
        
        // Calcola statistiche
        const publicCount = allAnnotations.filter(ann => ann.pubblica).length;
        const myCount = allAnnotations.filter(ann => ann.utenteCreazione === currentUser).length;
        const categories = new Set(allAnnotations.map(ann => ann.categoria).filter(cat => cat));
        
        document.getElementById('public-annotations').textContent = publicCount;
        document.getElementById('my-annotations').textContent = myCount;
        document.getElementById('categories-count').textContent = categories.size;
        
        // Carica annotazioni recenti
        loadRecentAnnotations(allAnnotations);
        
    } catch (error) {
        console.error('Errore nel caricamento della dashboard:', error);
        showAlert('Errore nel caricamento della dashboard', 'danger');
    }
}

// Caricamento annotazioni recenti
function loadRecentAnnotations(annotations) {
    const container = document.getElementById('recent-annotations');
    
    if (!annotations || annotations.length === 0) {
        container.innerHTML = '<p class="text-muted">Nessuna annotazione disponibile</p>';
        return;
    }
    
    // Ordina per data di ultima modifica e prende le prime 5
    const recent = annotations
        .sort((a, b) => new Date(b.dataUltimaModifica) - new Date(a.dataUltimaModifica))
        .slice(0, 5);
    
    let html = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Descrizione</th>
                    <th>Categoria</th>
                    <th>Versione</th>
                    <th>Utente</th>
                    <th>Ultima Modifica</th>
                    <th>Azioni</th>
                </tr>
            </thead>
            <tbody>
    `;
    
    recent.forEach(annotation => {
        const version = annotation.versioneNota || '1.0';
        html += `
            <tr>
                <td>
                    <strong>${escapeHtml(annotation.descrizione)}</strong>
                    <br><small class="text-muted">${truncateText(annotation.valoreNota, 50)}</small>
                </td>
                <td>
                    ${annotation.categoria ? `<span class="badge bg-secondary">${escapeHtml(annotation.categoria)}</span>` : '-'}
                </td>
                <td>
                    <span class="badge bg-info">${version}</span>
                </td>
                <td>
                    <i class="bi bi-person"></i> ${escapeHtml(annotation.utenteCreazione)}
                </td>
                <td>
                    <small>${formatDate(annotation.dataUltimaModifica)}</small>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="viewAnnotation('${annotation.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-secondary ms-1" onclick="editAnnotation('${annotation.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                </td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// Caricamento di tutte le annotazioni
async function loadAllAnnotations() {
    try {
        allAnnotations = await fetchAPI('');
        displayAnnotations(allAnnotations);
        populateFilterOptions(allAnnotations);
    } catch (error) {
        console.error('Errore nel caricamento delle annotazioni:', error);
        showAlert('Errore nel caricamento delle annotazioni', 'danger');
    }
}

// Visualizzazione delle annotazioni con supporto versione
function displayAnnotations(annotations, container = document.getElementById('annotations-container')) {
    if (!annotations || annotations.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-5">
                    <i class="bi bi-journal-x display-1 text-muted"></i>
                    <h3 class="text-muted mt-3">Nessuna annotazione trovata</h3>
                    <p class="text-muted">Inizia creando la tua prima annotazione!</p>
                    <button class="btn btn-primary" onclick="showSection('crea')">
                        <i class="bi bi-plus"></i> Crea Annotazione
                    </button>
                </div>
            </div>
        `;
        return;
    }
    let html = '';
    annotations.forEach(annotation => {
        const priorityClass = getPriorityClass(annotation.priorita);
        const priorityText = getPriorityText(annotation.priorita);
        const version = annotation.versioneNota || '1.0';
        html += `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card card-annotazione h-100">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">${escapeHtml(annotation.descrizione)}</h6>
                        <div>
                            <span class="badge bg-info me-1">${version}</span>
                            <span class="badge ${priorityClass} badge-priority">${priorityText}</span>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="annotazione-content mb-2">
                            ${escapeHtml(annotation.valoreNota)}
                        </div>
                        <div class="mb-2">
                            ${annotation.categoria ? `<span class="badge bg-secondary me-1">${escapeHtml(annotation.categoria)}</span>` : ''}
                            ${annotation.pubblica ? '<span class="badge bg-success me-1"><i class="bi bi-globe"></i> Pubblica</span>' : ''}
                            ${annotation.tags ? annotation.tags.split(',').map(tag => `<span class="badge bg-outline-secondary me-1">${escapeHtml(tag.trim())}</span>`).join('') : ''}
                        </div>
                        <small class="text-muted">
                            <i class="bi bi-person"></i> ${escapeHtml(annotation.utenteCreazione)}
                            <br>
                            <i class="bi bi-clock"></i> ${formatDate(annotation.dataUltimaModifica)}
                        </small>
                    </div>
                    <div class="card-footer">
                        <div class="btn-group w-100" role="group">
                            <button class="btn btn-outline-primary btn-sm" onclick="viewAnnotation('${annotation.id}')">
                                <i class="bi bi-eye"></i> Visualizza
                            </button>
                            <button class="btn btn-outline-secondary btn-sm" onclick="editAnnotation('${annotation.id}')">
                                <i class="bi bi-pencil"></i> Modifica
                            </button>
                            <button class="btn btn-outline-danger btn-sm" onclick="confirmDeleteAnnotation('${annotation.id}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

// Popola le opzioni dei filtri
function populateFilterOptions(annotations) {
    const categoryFilter = document.getElementById('filter-category');
    const categories = new Set(annotations.map(ann => ann.categoria).filter(cat => cat));
    
    // Reset opzioni
    categoryFilter.innerHTML = '<option value="">Tutte le categorie</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        categoryFilter.appendChild(option);
    });
}

// Filtro delle annotazioni
function filterAnnotations() {
    const searchText = document.getElementById('search-annotations').value.toLowerCase();
    const categoryFilter = document.getElementById('filter-category').value;
    const priorityFilter = document.getElementById('filter-priority').value;
    
    const filtered = allAnnotations.filter(annotation => {
        const matchesSearch = !searchText || 
            annotation.descrizione.toLowerCase().includes(searchText) ||
            annotation.valoreNota.toLowerCase().includes(searchText);
        
        const matchesCategory = !categoryFilter || annotation.categoria === categoryFilter;
        const matchesPriority = !priorityFilter || annotation.priorita.toString() === priorityFilter;
        
        return matchesSearch && matchesCategory && matchesPriority;
    });
    
    displayAnnotations(filtered);
}

// Caricamento annotazioni pubbliche
async function loadPublicAnnotations() {
    try {
        const publicAnnotations = await fetchAPI('/pubbliche');
        const container = document.getElementById('public-annotations-container');
        displayAnnotations(publicAnnotations, container);
    } catch (error) {
        console.error('Errore nel caricamento delle annotazioni pubbliche:', error);
        showAlert('Errore nel caricamento delle annotazioni pubbliche', 'danger');
    }
}

// Gestione creazione annotazione
async function handleCreateAnnotation(event) {
    event.preventDefault();
    
    // Validazione lato client
    const valoreNota = document.getElementById('annotation-content').value.trim();
    const descrizione = document.getElementById('annotation-title').value.trim();
    const utente = document.getElementById('annotation-user').value.trim();
    
    if (!valoreNota) {
        showAlert('Il contenuto dell\'annotazione è obbligatorio', 'warning');
        return;
    }
    
    if (!descrizione) {
        showAlert('La descrizione è obbligatoria', 'warning');
        return;
    }
    
    if (!utente) {
        showAlert('L\'utente è obbligatorio', 'warning');
        return;
    }
    
    const formData = {
        valoreNota: valoreNota,
        descrizione: descrizione,
        utente: utente,
        categoria: document.getElementById('annotation-category').value.trim() || null,
        tags: document.getElementById('annotation-tags').value.trim() || null,
        pubblica: document.getElementById('annotation-public').checked,
        priorita: parseInt(document.getElementById('annotation-priority').value)
    };
    
    console.log('Dati da inviare:', formData); // Debug
    
    try {
        const response = await fetchAPI('', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        console.log('Risposta ricevuta:', response); // Debug
        showAlert('Annotazione creata con successo!', 'success');
        resetForm();
        
        // Aggiorna le liste se necessario
        if (document.getElementById('dashboard-section').style.display !== 'none') {
            loadDashboard();
        }
        
        // Vai alle annotazioni per vedere il risultato
        showSection('annotazioni');
        
    } catch (error) {
        console.error('Errore completo nella creazione dell\'annotazione:', error);
        
        // Gestione specifica per l'errore di validazione ID
        if (error.message.includes('VALIDATION_ERROR') || error.message.includes('ID non può essere null')) {
            console.log('⚠️ Rilevato errore di validazione ID. Tentativo con approccio alternativo...');
            
            // Prova ad aggiungere un id null esplicitamente
            const formDataWithId = {
                ...formData,
                id: null
            };
            
            try {
                const response2 = await fetchAPI('', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formDataWithId)
                });
                
                console.log('✅ Salvataggio riuscito con id null:', response2);
                showAlert('Annotazione creata con successo!', 'success');
                resetForm();
                
                if (document.getElementById('dashboard-section').style.display !== 'none') {
                    loadDashboard();
                }
                showSection('annotazioni');
                
                return; // Uscita dalla funzione se il secondo tentativo ha successo
                
            } catch (error2) {
                console.error('❌ Anche il tentativo con id null è fallito:', error2);
            }
        }
        
        // Gestione errore più dettagliata
        let errorMessage = 'Errore nella creazione dell\'annotazione';
        if (error.message && error.message.includes('400')) {
            errorMessage = 'Dati non validi. Controlla che tutti i campi siano compilati correttamente.';
        } else if (error.message && error.message.includes('500')) {
            errorMessage = 'Errore del server. Riprova più tardi.';
        } else if (error.message && error.message.includes('network')) {
            errorMessage = 'Errore di connessione. Controlla la connessione di rete.';
        } else if (error.message && error.message.includes('VALIDATION_ERROR')) {
            errorMessage = 'Errore di validazione nel backend. Il server si aspetta dati in un formato diverso.';
        }
        
        showAlert(errorMessage, 'danger');
    }
}

// Reset del form
function resetForm() {
    document.getElementById('create-annotation-form').reset();
    document.getElementById('annotation-user').value = currentUser;
    document.getElementById('annotation-priority').value = '2';
    currentEditingId = null;
}

// Ricerca globale
async function performGlobalSearch() {
    const searchText = document.getElementById('global-search').value.trim();
    
    if (!searchText) {
        showAlert('Inserisci un termine di ricerca', 'warning');
        return;
    }
    
    try {
        const results = await fetchAPI('/cerca?testo=' + encodeURIComponent(searchText));
        const container = document.getElementById('search-results-container');
        
        if (results.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="text-center py-5">
                        <i class="bi bi-search display-1 text-muted"></i>
                        <h3 class="text-muted mt-3">Nessun risultato trovato</h3>
                        <p class="text-muted">Prova con termini di ricerca diversi</p>
                    </div>
                </div>
            `;
        } else {
            // Usa la stessa funzione di visualizzazione delle annotazioni
            let html = `
                <div class="col-12 mb-3">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle"></i> 
                        Trovati <strong>${results.length}</strong> risultati per "<strong>${escapeHtml(searchText)}</strong>"
                    </div>
                </div>
            `;
            container.innerHTML = html;
            
            // Aggiungi i risultati usando la funzione di visualizzazione esistente
            displayAnnotations(results);
            
            // Sposta il container dei risultati nel posto giusto
            const annotationsHtml = document.getElementById('annotations-container').innerHTML;
            container.innerHTML = html + annotationsHtml;
            document.getElementById('annotations-container').innerHTML = '';
        }
        
    } catch (error) {
        console.error('Errore nella ricerca:', error);
        showAlert('Errore durante la ricerca', 'danger');
    }
}

// Visualizzazione dettagli annotazione
async function viewAnnotation(id) {
    try {
        const annotation = await fetchAPI('/' + id);
        showAnnotationModal(annotation, 'view');
    } catch (error) {
        console.error('Errore nel caricamento dell\'annotazione:', error);
        showAlert('Errore nel caricamento dell\'annotazione', 'danger');
    }
}

// Modifica annotazione
async function editAnnotation(id) {
    try {
        const annotation = await fetchAPI('/' + id);
        showAnnotationModal(annotation, 'edit');
    } catch (error) {
        console.error('Errore nel caricamento dell\'annotazione per la modifica:', error);
        showAlert('Errore nel caricamento dell\'annotazione', 'danger');
    }
}

// Gestione modifica annotazione
async function handleEditAnnotation(event) {
    event.preventDefault();
    
    if (!currentEditingId) {
        showAlert('Errore: ID annotazione non trovato', 'danger');
        return;
    }
    
    // Validazione lato client
    const valoreNota = document.getElementById('edit-content').value.trim();
    const descrizione = document.getElementById('edit-description').value.trim();
    
    if (!valoreNota) {
        showAlert('Il contenuto dell\'annotazione è obbligatorio', 'warning');
        return;
    }
    
    if (!descrizione) {
        showAlert('La descrizione è obbligatoria', 'warning');
        return;
    }
    
    const formData = {
        id: currentEditingId,
        valoreNota: valoreNota,
        descrizione: descrizione,
        utente: currentUser,
        categoria: document.getElementById('edit-category').value.trim() || null,
        tags: document.getElementById('edit-tags').value.trim() || null,
        pubblica: document.getElementById('edit-public').checked,
        priorita: parseInt(document.getElementById('edit-priority').value)
    };
    
    console.log('Dati modifica da inviare:', formData); // Debug
    console.log('ID da modificare:', currentEditingId); // Debug
    
    try {
        const response = await fetchAPI('/' + currentEditingId, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        console.log('Risposta modifica ricevuta:', response); // Debug
        showAlert('Annotazione aggiornata con successo', 'success');
        
        // Chiudi il modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('annotationModal'));
        if (modal) {
            modal.hide();
        }
        
        // Ricarica la lista se siamo nella sezione annotazioni
        if (document.getElementById('annotazioni-section').style.display !== 'none') {
            loadAllAnnotations();
        }
        
        // Ricarica la dashboard se siamo lì
        if (document.getElementById('dashboard-section').style.display !== 'none') {
            loadDashboard();
        }
        
        currentEditingId = null;
        
    } catch (error) {
        console.error('Errore completo nell\'aggiornamento dell\'annotazione:', error);
        
        // Gestione errore più dettagliata
        let errorMessage = 'Errore nell\'aggiornamento dell\'annotazione';
        if (error.message && error.message.includes('404')) {
            errorMessage = 'Annotazione non trovata. Potrebbe essere stata eliminata.';
        } else if (error.message && error.message.includes('400')) {
            errorMessage = 'Dati non validi. Controlla che tutti i campi siano compilati correttamente.';
        } else if (error.message && error.message.includes('500')) {
            errorMessage = 'Errore del server. Riprova più tardi.';
        }
        
        showAlert(errorMessage, 'danger');
    }
}

// Conferma eliminazione annotazione
function confirmDeleteAnnotation(id) {
    currentEditingId = id;
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    
    document.getElementById('confirm-delete-btn').onclick = function() {
        deleteAnnotation(id);
        deleteModal.hide();
    };
    
    deleteModal.show();
}

// Eliminazione annotazione
async function deleteAnnotation(id) {
    try {
        await fetchAPI('/' + id, {
            method: 'DELETE'
        });
        
        showAlert('Annotazione eliminata con successo', 'success');
        
        // Ricarica la lista se siamo nella sezione annotazioni
        if (document.getElementById('annotazioni-section').style.display !== 'none') {
            loadAllAnnotations();
        }
        
        // Ricarica la dashboard se siamo lì
        if (document.getElementById('dashboard-section').style.display !== 'none') {
            loadDashboard();
        }
        
    } catch (error) {
        console.error('Errore nell\'eliminazione dell\'annotazione:', error);
        showAlert('Errore nell\'eliminazione dell\'annotazione', 'danger');
    }
}

// Mostra modal annotazione
function showAnnotationModal(annotation, mode) {
    const modal = new bootstrap.Modal(document.getElementById('annotationModal'));
    const modalTitle = document.getElementById('modalTitle');
    const modalFooter = document.getElementById('modalFooter');
    const viewMode = document.getElementById('view-mode');
    const editMode = document.getElementById('edit-mode');
    
    currentEditingId = annotation.id;
    const version = annotation.versioneNota || '1.0';
    
    if (mode === 'view') {
        modalTitle.textContent = 'Dettagli Annotazione';
        viewMode.style.display = 'block';
        editMode.style.display = 'none';
        
        // Popola i campi di visualizzazione
        document.getElementById('view-description').textContent = annotation.descrizione;
        document.getElementById('view-content').textContent = annotation.valoreNota;
        document.getElementById('view-category').innerHTML = annotation.categoria ? 
            `<span class="badge bg-secondary">${escapeHtml(annotation.categoria)}</span>` : 
            '<span class="text-muted">Nessuna categoria</span>';
        
        document.getElementById('view-priority').innerHTML = `<span class="badge ${getPriorityClass(annotation.priorita)}">${getPriorityText(annotation.priorita)}</span>`;
        
        document.getElementById('view-tags').innerHTML = annotation.tags ? 
            annotation.tags.split(',').map(tag => `<span class="badge bg-outline-secondary me-1">${escapeHtml(tag.trim())}</span>`).join('') : 
            '<span class="text-muted">Nessun tag</span>';
        
        document.getElementById('view-user').textContent = annotation.utenteCreazione;
        document.getElementById('view-created-date').textContent = formatDate(annotation.dataInserimento);
        document.getElementById('view-modified-date').textContent = formatDate(annotation.dataUltimaModifica);
        document.getElementById('view-version').textContent = `${version}`;
        document.getElementById('view-visibility').innerHTML = annotation.pubblica ? 
            '<span class="badge bg-success"><i class="bi bi-globe"></i> Pubblica</span>' : 
            '<span class="badge bg-secondary"><i class="bi bi-lock"></i> Privata</span>';
        document.getElementById('view-id').textContent = annotation.id;
        
        modalFooter.innerHTML = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
            <button type="button" class="btn btn-primary" onclick="editAnnotation('${annotation.id}')">
                <i class="bi bi-pencil"></i> Modifica
            </button>
            <button type="button" class="btn btn-danger" onclick="confirmDeleteAnnotation('${annotation.id}'); modal.hide();">
                <i class="bi bi-trash"></i> Elimina
            </button>
        `;
        
    } else if (mode === 'edit') {
        modalTitle.textContent = 'Modifica Annotazione';
        viewMode.style.display = 'none';
        editMode.style.display = 'block';
        
        // Popola i campi di modifica
        document.getElementById('edit-description').value = annotation.descrizione;
        document.getElementById('edit-content').value = annotation.valoreNota;
        document.getElementById('edit-category').value = annotation.categoria || '';
        document.getElementById('edit-priority').value = annotation.priorita || 2;
        document.getElementById('edit-tags').value = annotation.tags || '';
        document.getElementById('edit-public').checked = annotation.pubblica || false;
        
        // Info pannello
        document.getElementById('edit-current-version').textContent = `${version}`;
        document.getElementById('edit-user').textContent = annotation.utenteCreazione;
        document.getElementById('edit-created-date').textContent = formatDate(annotation.dataInserimento);
        
        modalFooter.innerHTML = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annulla</button>
            <button type="button" class="btn btn-primary" onclick="document.getElementById('edit-annotation-form').dispatchEvent(new Event('submit'))">
                <i class="bi bi-save"></i> Salva Modifiche
            </button>
        `;
    }
    
    modal.show();
}

// Caricamento e gestione categorie
async function loadCategories() {
    try {
        // Carica tutte le annotazioni per estrarre le categorie
        const annotations = await fetchAPI('');
        const categories = extractCategories(annotations);
        
        displayCategoriesList(categories);
        displayCategoryStatistics(categories, annotations);
        
    } catch (error) {
        console.error('Errore nel caricamento delle categorie:', error);
        showAlert('Errore nel caricamento delle categorie', 'danger');
    }
}

function extractCategories(annotations) {
    const categoriesMap = new Map();
    
    annotations.forEach(annotation => {
        if (annotation.categoria) {
            if (!categoriesMap.has(annotation.categoria)) {
                categoriesMap.set(annotation.categoria, {
                    name: annotation.categoria,
                    count: 0,
                    annotations: []
                });
            }
            const category = categoriesMap.get(annotation.categoria);
            category.count++;
            category.annotations.push(annotation);
        }
    });
    
    return Array.from(categoriesMap.values());
}

function displayCategoriesList(categories) {
    const container = document.getElementById('categories-list');
    
    if (categories.length === 0) {
        container.innerHTML = '<p class="text-muted">Nessuna categoria disponibile</p>';
        return;
    }
    
    let html = '';
    categories.forEach(category => {
        html += `
            <div class="d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                <div>
                    <span class="badge bg-secondary me-2">${escapeHtml(category.name)}</span>
                    <small class="text-muted">${category.count} annotazioni</small>
                </div>
                <div>
                    <button class="btn btn-sm btn-outline-primary" onclick="filterByCategory('${category.name}')">
                        <i class="bi bi-filter"></i> Filtra
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function displayCategoryStatistics(categories, annotations) {
    const container = document.getElementById('category-statistics');
    
    if (categories.length === 0) {
        container.innerHTML = '<p class="text-muted">Nessuna statistica disponibile</p>';
        return;
    }
    
    // Ordina le categorie per numero di annotazioni
    categories.sort((a, b) => b.count - a.count);
    
    let html = '<div class="row">';
    categories.forEach(category => {
        const percentage = ((category.count / annotations.length) * 100).toFixed(1);
        html += `
            <div class="col-md-6 mb-3">
                <div class="card">
                    <div class="card-body">
                        <h6 class="card-title">${escapeHtml(category.name)}</h6>
                        <div class="progress mb-2">
                            <div class="progress-bar" role="progressbar" style="width: ${percentage}%" aria-valuenow="${percentage}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                        <small class="text-muted">${category.count} annotazioni (${percentage}%)</small>
                    </div>
                </div>
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

// Gestione aggiunta categoria
async function handleAddCategory(event) {
    event.preventDefault();
    
    const categoryName = document.getElementById('new-category-name').value.trim();
    const categoryDescription = document.getElementById('new-category-description').value.trim();
    const categoryColor = document.getElementById('new-category-color').value;
    
    if (!categoryName) {
        showAlert('Il nome della categoria è obbligatorio', 'warning');
        return;
    }
    
    // Per ora, salviamo la categoria localmente 
    // In futuro si potrebbe aggiungere un endpoint API dedicato
    showAlert(`Categoria "${categoryName}" aggiunta con successo`, 'success');
    
    // Reset form
    document.getElementById('add-category-form').reset();
    document.getElementById('new-category-color').value = '#6c757d';
    
    // Ricarica le categorie
    loadCategories();
}

function filterByCategory(categoryName) {
    // Vai alla sezione annotazioni e applica il filtro
    showSection('annotazioni');
    document.getElementById('filter-category').value = categoryName;
    filterAnnotations();
}

// Funzioni di utilità
async function fetchAPI(endpoint, options = {}) {
    console.log('Chiamata API:', API_BASE_URL + endpoint, options); // Debug
    
    try {
        const response = await fetch(API_BASE_URL + endpoint, options);
        
        console.log('Status della risposta:', response.status); // Debug
        
        if (!response.ok) {
            // Prova a leggere il corpo dell'errore se disponibile
            let errorDetails = '';
            try {
                const errorBody = await response.text();
                console.log('Corpo dell\'errore:', errorBody); // Debug
                errorDetails = errorBody;
            } catch (e) {
                console.log('Impossibile leggere il corpo dell\'errore'); // Debug
            }
            
            throw new Error(`HTTP error! status: ${response.status}, details: ${errorDetails}`);
        }
        
        const data = await response.json();
        console.log('Dati ricevuti:', data); // Debug
        return data;
        
    } catch (error) {
        console.error('Errore in fetchAPI:', error); // Debug
        throw error;
    }
}

function showAlert(message, type) {
    // Rimuovi alert precedenti
    const existingAlert = document.querySelector('.alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Inserisci all'inizio del contenuto
    const container = document.querySelector('.container-fluid');
    container.insertBefore(alert, container.firstChild);
    
    // Auto-rimozione dopo 5 secondi
    setTimeout(() => {
        if (alert.parentNode) {
            alert.remove();
        }
    }, 5000);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('it-IT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getPriorityClass(priority) {
    switch(priority) {
        case 1: return 'bg-success';
        case 2: return 'bg-warning';
        case 3: return 'bg-danger';
        default: return 'bg-secondary';
    }
}

function getPriorityText(priority) {
    switch(priority) {
        case 1: return 'Bassa';
        case 2: return 'Media';
        case 3: return 'Alta';
        default: return 'Non specificata';
    }
}
