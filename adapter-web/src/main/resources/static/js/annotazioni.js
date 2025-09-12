// Configurazione annotazioni
const ANNOTATIONS_API_BASE_URL = '/api/annotazioni';
let allAnnotations = [];
let currentEditingId = null;

// Inizializzazione quando la pagina è caricata
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione semplice
    if (!window.authUtils || !window.authUtils.isAuthenticated()) {
        console.log('Utente non autenticato, redirect al login');
        window.location.href = 'login.html';
        return;
    }
    
    console.log('Utente autenticato, inizializzazione pagina annotazioni');
    
    // Carica i dati utente
    loadUserData();
    
    // Carica le statistiche iniziali
    loadStats();
    
    // Carica le categorie disponibili
    loadCategories();
    
    // Mostra la dashboard di default
    showSection('annotazioni');
    
    // Configura gli event listeners
    setupAnnotationsEventListeners();
    
    // Inizializza il form con l'utente corrente
    initializeAnnotationForm();
});

// Inizializza il form delle annotazioni con i valori di default
function initializeAnnotationForm() {
    const userField = document.getElementById('annotation-user');
    if (userField && currentUser) {
        userField.value = currentUser;
    }
}

// Carica dati utente
function loadUserData() {
    const user = window.authUtils ? window.authUtils.getCurrentUser() : null;
    if (user) {
        currentUser = user.username || user.email;
        
        const userName = user.firstName ? 
            `${user.firstName} ${user.lastName || ''}`.trim() : 
            user.username || user.email;
        
        const userNameElement = document.getElementById('user-name');
        if (userNameElement) {
            userNameElement.textContent = userName;
        }
    }
}

// Carica le statistiche iniziali
async function loadStats() {
    try {
        // Carica le statistiche dalla dashboard
        //await loadAnnotationsDashboard();
    } catch (error) {
        console.error('Errore nel caricamento delle statistiche:', error);
    }
}

// Setup degli event listeners
function setupAnnotationsEventListeners() {
    // Form di creazione annotazione
    const createForm = document.getElementById('create-annotation-form');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateAnnotation);
    }
    
    // Form di aggiunta categoria
    const categoryForm = document.getElementById('add-category-form');
    if (categoryForm) {
        categoryForm.addEventListener('submit', handleAddCategory);
    }
    
    // Form di modifica annotazione
    const editForm = document.getElementById('edit-annotation-form');
    if (editForm) {
        editForm.addEventListener('submit', handleEditAnnotation);
    }
    
    // Ricerca in tempo reale
    const searchInput = document.getElementById('search-annotations');
    if (searchInput) {
        searchInput.addEventListener('input', filterAnnotations);
    }
    
    const categoryFilter = document.getElementById('filter-category');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterAnnotations);
    }
    
    const priorityFilter = document.getElementById('filter-priority');
    if (priorityFilter) {
        priorityFilter.addEventListener('change', filterAnnotations);
    }
    
    const statoFilter = document.getElementById('filter-stato');
    if (statoFilter) {
        statoFilter.addEventListener('change', filterAnnotations);
    }
    
    // Ricerca globale
    const globalSearch = document.getElementById('global-search');
    if (globalSearch) {
        globalSearch.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performGlobalSearch();
            }
        });
    }
    
    // Test connessione API all'avvio
    testApiConnection();
}

// Gestione hash URL
function handleUrlHash() {
    const hash = window.location.hash.substring(1);
    if (hash) {
        switch(hash) {
            case 'create':
                showSection('crea');
                return true;
            case 'search':
                showSection('cerca');
                return true;
            case 'public':
                showSection('pubbliche');
                return true;
            case 'categories':
                showSection('categorie');
                return true;
            default:
                showSection('annotazioni');
                return true;
        }
    }
    return false;
}

// Test della connessione API
async function testApiConnection() {
    try {
        console.log('Test connessione API annotazioni in corso...');
        
        const response = await window.authUtils.authenticatedFetch(ANNOTATIONS_API_BASE_URL + '/statistiche');
        console.log('✅ Connessione API annotazioni funzionante:', response);
        
        showAlert('✅ Connessione al server stabilita', 'success');
        
    } catch (error) {
        console.error('❌ Errore di connessione API annotazioni:', error);
        showAlert('⚠️ Problemi di connessione al server', 'warning');
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
    
    // Carica i dati specifici della sezione
    switch(sectionName) {
        case 'dashboard':
            loadAnnotationsDashboard();
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

// Caricamento della dashboard annotazioni
async function loadAnnotationsDashboard() {
    try {
        // Carica statistiche
        const stats = await fetchAnnotationsAPI('/statistiche');
        document.getElementById('total-annotations').textContent = stats.totaleAnnotazioni || 0;
        
        // Carica tutte le annotazioni per calcolare le statistiche locali
        const allAnnotations = await fetchAnnotationsAPI('');
        
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
        console.error('Errore nel caricamento della dashboard annotazioni:', error);
        //showAlert('Errore nel caricamento della dashboard', 'danger');
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
                    <div class="d-flex flex-column">
                        <small><i class="bi bi-person-plus"></i> Creata da: ${escapeHtml(annotation.utenteCreazione)}</small>
                        ${annotation.utenteUltimaModifica && annotation.utenteUltimaModifica !== annotation.utenteCreazione ? 
                            `<small class="text-muted"><i class="bi bi-pencil"></i> Modificata da: ${escapeHtml(annotation.utenteUltimaModifica)}</small>` : 
                            ''}
                    </div>
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
        allAnnotations = await fetchAnnotationsAPI('');
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
        const statoClass = getStatoClass(annotation.stato);
        const statoText = getStatoText(annotation.stato);
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
                            ${annotation.stato ? `<span class="badge ${statoClass} me-1"><i class="bi bi-flag"></i> ${statoText}</span>` : ''}
                            ${annotation.categoria ? `<span class="badge bg-secondary me-1">${escapeHtml(annotation.categoria)}</span>` : ''}
                            ${annotation.pubblica ? '<span class="badge bg-success me-1"><i class="bi bi-globe"></i> Pubblica</span>' : ''}
                            ${annotation.tags ? annotation.tags.split(',').map(tag => `<span class="badge bg-outline-secondary me-1">${escapeHtml(tag.trim())}</span>`).join('') : ''}
                        </div>
                        <small class="text-muted">
                            <i class="bi bi-person"></i> 
                            ${annotation.utenteUltimaModifica && annotation.utenteUltimaModifica !== annotation.utenteCreazione 
                                ? `Creata da ${escapeHtml(annotation.utenteCreazione)}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Modificata da ${escapeHtml(annotation.utenteUltimaModifica)}`
                                : `${escapeHtml(annotation.utenteCreazione)}`
                            }
                            <br>
                            <i class="bi bi-clock"></i> ${formatDate(annotation.dataUltimaModifica)}
                        </small>
                    </div>
                    <div class="card-footer">
                        <div class="btn-group w-100" role="group">
                            <button class="btn btn-outline-primary btn-sm" onclick="viewAnnotation('${annotation.id}')">
                                <i class="bi bi-eye"></i> Visualizza
                            </button>
                            <button class="btn btn-outline-secondary btn-sm" 
                                    onclick="editAnnotation('${annotation.id}')"
                                    ${annotation.stato && !['INSERITA', 'MODIFICATA'].includes(annotation.stato) ? 'disabled title="Modifica non consentita per annotazioni in stato ' + annotation.stato + '"' : ''}>
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
    if (!categoryFilter) return;
    
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
    const searchText = document.getElementById('search-annotations')?.value?.toLowerCase() || '';
    const categoryFilter = document.getElementById('filter-category')?.value || '';
    const priorityFilter = document.getElementById('filter-priority')?.value || '';
    const statoFilter = document.getElementById('filter-stato')?.value || '';
    
    const filtered = allAnnotations.filter(annotation => {
        const matchesSearch = !searchText || 
            annotation.descrizione.toLowerCase().includes(searchText) ||
            annotation.valoreNota.toLowerCase().includes(searchText);
        
        const matchesCategory = !categoryFilter || annotation.categoria === categoryFilter;
        const matchesPriority = !priorityFilter || annotation.priorita.toString() === priorityFilter;
        const matchesStato = !statoFilter || annotation.stato === statoFilter;
        
        return matchesSearch && matchesCategory && matchesPriority && matchesStato;
    });
    
    displayAnnotations(filtered);
}

// Caricamento annotazioni pubbliche
async function loadPublicAnnotations() {
    try {
        const publicAnnotations = await fetchAnnotationsAPI('/pubbliche');
        const container = document.getElementById('public-annotations-container');
        displayAnnotations(publicAnnotations, container);
    } catch (error) {
        console.error('Errore nel caricamento delle annotazioni pubbliche:', error);
        showAlert('Errore nel caricamento delle annotazioni pubbliche', 'danger');
    }
}

// Gestione creazione e modifica annotazione
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
    
    // Se siamo in modalità modifica, aggiungi l'ID
    if (currentEditingId) {
        formData.id = currentEditingId;
    }

    try {
        let response;
        
        if (currentEditingId) {
            // Modalità modifica
            response = await fetchAnnotationsAPI(`/${currentEditingId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            showAlert('Annotazione aggiornata con successo!', 'success');
        } else {
            // Modalità creazione
            response = await fetchAnnotationsAPI('', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            showAlert('Annotazione creata con successo!', 'success');
        }
        
        resetForm();
        
        // Vai alle annotazioni per vedere il risultato
        showSection('annotazioni');
        
    } catch (error) {
        console.error('Errore nell\'operazione:', error);
        showAlert('Errore nell\'operazione', 'danger');
    }
}

// Reset del form
function resetForm() {
    const createForm = document.getElementById('create-annotation-form');
    if (createForm) {
        createForm.reset();
        // Assicurati che l'utente sia sempre popolato con l'utente corrente
        document.getElementById('annotation-user').value = currentUser || '';
        document.getElementById('annotation-priority').value = '2';
    }
    currentEditingId = null;
}

// Utility per chiamate API annotazioni
async function fetchAnnotationsAPI(endpoint, options = {}) {
    return await window.authUtils.authenticatedFetch(ANNOTATIONS_API_BASE_URL + endpoint, options)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        });
}

// Utility functions
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return escapeHtml(text);
    return escapeHtml(text.substring(0, maxLength)) + '...';
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
        case 2: return 'bg-warning text-dark';
        case 3: return 'bg-danger';
        default: return 'bg-secondary';
    }
}

function getPriorityText(priority) {
    switch(priority) {
        case 1: return 'Bassa';
        case 2: return 'Media';
        case 3: return 'Alta';
        default: return 'Non definita';
    }
}

// Funzioni helper per gestire gli stati delle annotazioni
function getStatoClass(stato) {
    switch(stato) {
        case 'INSERITA': return 'bg-light text-dark';
        case 'MODIFICATA': return 'bg-info';
        case 'CONFERMATA': return 'bg-primary';
        case 'RIFIUTATA': return 'bg-warning text-dark';
        case 'PUBBLICATA': return 'bg-success';
        case 'BANNATA': return 'bg-danger';
        case 'ERRORE': return 'bg-dark';
        default: return 'bg-secondary';
    }
}

function getStatoText(stato) {
    switch(stato) {
        case 'INSERITA': return 'Inserita';
        case 'MODIFICATA': return 'Modificata';
        case 'CONFERMATA': return 'Confermata';
        case 'RIFIUTATA': return 'Rifiutata';
        case 'PUBBLICATA': return 'Pubblicata';
        case 'BANNATA': return 'Bannata';
        case 'ERRORE': return 'Errore';
        default: return 'Non definito';
    }
}

function showAlert(message, type = 'info') {
    // Rimuovi alert precedenti
    const existingAlert = document.querySelector('.alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto-rimozione dopo 5 secondi
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

// Funzioni per gestione annotazioni
function viewAnnotation(id) {
    // Mostra i dettagli dell'annotazione in un modal
    showAnnotationDetails(id);
}

async function showAnnotationDetails(id) {
    try {
        const annotation = await fetchAnnotationsAPI(`/${id}`);
        
        const modalHtml = `
            <div class="modal fade" id="viewAnnotationModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">
                                <i class="bi bi-journal-text"></i> ${escapeHtml(annotation.descrizione)}
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label fw-bold">Contenuto:</label>
                                <div class="border p-3 bg-light rounded">
                                    ${escapeHtml(annotation.valoreNota)}
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <strong>Categoria:</strong> ${annotation.categoria ? escapeHtml(annotation.categoria) : 'Nessuna'}
                                </div>
                                <div class="col-md-6">
                                    <strong>Priorità:</strong> ${getPriorityText(annotation.priorita)}
                                </div>
                            </div>
                            <div class="row mt-2">
                                <div class="col-md-6">
                                    <strong>Stato:</strong> 
                                    <span class="badge ${getStatoClass(annotation.stato)}">${getStatoText(annotation.stato)}</span>
                                </div>
                                <div class="col-md-6">
                                    <strong>Pubblica:</strong> ${annotation.pubblica ? 'Sì' : 'No'}
                                </div>
                            </div>
                            <div class="row mt-2">
                                <div class="col-md-6">
                                    <strong>Creato da:</strong> ${escapeHtml(annotation.utenteCreazione)}
                                </div>
                                <div class="col-md-6">
                                    <strong>Versione:</strong> ${annotation.versioneNota || '1.0'}
                                </div>
                            </div>
                            ${annotation.utenteUltimaModifica && annotation.utenteUltimaModifica !== annotation.utenteCreazione ? 
                                `<div class="row mt-2">
                                    <div class="col-md-6">
                                        <strong>Ultima modifica da:</strong> ${escapeHtml(annotation.utenteUltimaModifica)}
                                    </div>
                                </div>` : ''
                            }
                            <div class="row mt-2">
                                <div class="col-md-6">
                                    <strong>Creata:</strong> ${formatDate(annotation.dataCreazione)}
                                </div>
                                <div class="col-md-6">
                                    <strong>Modificata:</strong> ${formatDate(annotation.dataUltimaModifica)}
                                </div>
                            </div>
                            ${annotation.tags ? `<div class="mt-2"><strong>Tags:</strong> ${annotation.tags}</div>` : ''}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
                            <button type="button" class="btn btn-primary" onclick="editAnnotation('${annotation.id}')">
                                <i class="bi bi-pencil"></i> Modifica
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Rimuovi modal esistente
        const existingModal = document.getElementById('viewAnnotationModal');
        if (existingModal) existingModal.remove();
        
        // Aggiungi nuovo modal
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        // Mostra modal
        const modal = new bootstrap.Modal(document.getElementById('viewAnnotationModal'));
        modal.show();
        
    } catch (error) {
        console.error('Errore nel caricamento dei dettagli annotazione:', error);
        showAlert('Errore nel caricamento dei dettagli', 'danger');
    }
}

function editAnnotation(id) {
    // Chiudi il modal se aperto
    const modal = bootstrap.Modal.getInstance(document.getElementById('viewAnnotationModal'));
    if (modal) modal.hide();
    
    // Carica l'annotazione nel form di modifica
    loadAnnotationForEdit(id);
    showSection('crea');
}

async function loadAnnotationForEdit(id) {
    try {
        const annotation = await fetchAnnotationsAPI(`/${id}`);
        
        // Imposta modalità modifica
        currentEditingId = id;
        
        // Popola il form
        document.getElementById('annotation-title').value = annotation.descrizione || '';
        document.getElementById('annotation-content').value = annotation.valoreNota || '';
        document.getElementById('annotation-category').value = annotation.categoria || '';
        document.getElementById('annotation-tags').value = annotation.tags || '';
        // IMPORTANTE: Usa l'utente corrente per la modifica, non l'utente originale
        document.getElementById('annotation-user').value = currentUser || '';
        document.getElementById('annotation-public').checked = annotation.pubblica || false;
        document.getElementById('annotation-priority').value = annotation.priorita || 2;
        
        // Cambia il titolo della sezione
        const sectionTitle = document.querySelector('#crea-section h2');
        if (sectionTitle) {
            sectionTitle.innerHTML = '<i class="bi bi-pencil"></i> Modifica Annotazione';
        }
        
        // Cambia il testo del bottone
        const submitBtn = document.querySelector('#create-annotation-form button[type="submit"]');
        if (submitBtn) {
            submitBtn.innerHTML = '<i class="bi bi-save"></i> Aggiorna Annotazione';
        }
        
    } catch (error) {
        console.error('Errore nel caricamento annotazione per modifica:', error);
        showAlert('Errore nel caricamento annotazione', 'danger');
    }
}

async function confirmDeleteAnnotation(id) {
    if (confirm('Sei sicuro di voler eliminare questa annotazione? L\'operazione non può essere annullata.')) {
        try {
            await fetchAnnotationsAPI(`/${id}`, {
                method: 'DELETE'
            });
            
            showAlert('Annotazione eliminata con successo!', 'success');
            
            // Ricarica la sezione corrente
            const currentSection = document.querySelector('.content-section[style*="block"]');
            if (currentSection) {
                const sectionId = currentSection.id.replace('-section', '');
                showSection(sectionId);
            }
            
        } catch (error) {
            console.error('Errore nell\'eliminazione annotazione:', error);
            showAlert('Errore nell\'eliminazione annotazione', 'danger');
        }
    }
}

function handleEditAnnotation(event) {
    // Questa funzione gestisce il submit del form quando siamo in modalità modifica
    // La logica è già gestita in handleCreateAnnotation che controlla currentEditingId
    event.preventDefault();
    handleCreateAnnotation(event);
}

// Funzione per resettare il form e tornare in modalità creazione
function resetForm() {
    currentEditingId = null;
    
    // Reset campi del form
    document.getElementById('annotation-content').value = '';
    document.getElementById('annotation-title').value = '';
    document.getElementById('annotation-user').value = currentUser || ''; // Mantieni l'utente loggato
    document.getElementById('annotation-category').value = '';
    document.getElementById('annotation-tags').value = '';
    document.getElementById('annotation-public').checked = false;
    document.getElementById('annotation-priority').value = '1';
    
    // Aggiorna l'interfaccia per la modalità creazione
    const formTitle = document.querySelector('#create .card-header h3');
    if (formTitle) {
        formTitle.textContent = 'Crea Nuova Annotazione';
    }
    
    const submitButton = document.querySelector('#create button[type="submit"]');
    if (submitButton) {
        submitButton.innerHTML = '<i class="fas fa-plus me-2"></i>Crea Annotazione';
        submitButton.className = 'btn btn-primary';
    }
}

function handleAddCategory(event) {
    // TODO: Implementare gestione categorie
    event.preventDefault();
    showAlert('Funzione gestione categorie in sviluppo', 'info');
}

async function performGlobalSearch(event) {
    event.preventDefault();
    
    const searchTerm = document.getElementById('global-search').value.trim();
    
    if (!searchTerm) {
        showAlert('Inserisci un termine di ricerca', 'warning');
        return;
    }
    
    try {
        // Chiama l'endpoint di ricerca API
        const annotations = await fetchAnnotationsAPI(`/cerca?q=${encodeURIComponent(searchTerm)}`);
        
        // Mostra la sezione annotazioni con i risultati della ricerca
        showSection('annotazioni');
        
        // Aggiorna il titolo per indicare che stiamo mostrando risultati di ricerca
        const sectionTitle = document.querySelector('#annotazioni .card-header h3');
        if (sectionTitle) {
            sectionTitle.innerHTML = `<i class="fas fa-search me-2"></i>Risultati ricerca: "${searchTerm}"`;
        }
        
        // Aggiungi un pulsante per tornare a tutte le annotazioni
        const cardHeader = document.querySelector('#annotazioni .card-header');
        if (cardHeader && !cardHeader.querySelector('.btn-outline-secondary')) {
            const clearSearchBtn = document.createElement('button');
            clearSearchBtn.className = 'btn btn-outline-secondary btn-sm ms-auto';
            clearSearchBtn.innerHTML = '<i class="fas fa-times me-1"></i>Mostra tutte';
            clearSearchBtn.onclick = () => {
                document.getElementById('global-search').value = '';
                sectionTitle.innerHTML = '<i class="fas fa-sticky-note me-2"></i>Le Mie Annotazioni';
                clearSearchBtn.remove();
                loadAllAnnotations();
            };
            cardHeader.appendChild(clearSearchBtn);
        }
        
        // Mostra i risultati
        displayAnnotations(annotations);
        
        if (annotations.length === 0) {
            showAlert(`Nessuna annotazione trovata per "${searchTerm}"`, 'info');
        } else {
            showAlert(`Trovate ${annotations.length} annotazioni per "${searchTerm}"`, 'success');
        }
        
    } catch (error) {
        console.error('Errore nella ricerca:', error);
        showAlert('Errore durante la ricerca', 'danger');
    }
}

function loadCategories() {
    // Implemento il caricamento delle categorie dalle annotazioni esistenti
    fetchAnnotationsAPI('')
        .then(annotations => {
            const categories = new Set();
            annotations.forEach(annotation => {
                if (annotation.categoria && annotation.categoria.trim()) {
                    categories.add(annotation.categoria.trim());
                }
            });
            
            const categorySelect = document.getElementById('annotation-category');
            if (categorySelect) {
                // Mantieni l'opzione vuota
                categorySelect.innerHTML = '<option value="">Seleziona categoria (opzionale)</option>';
                
                // Aggiungi le categorie trovate
                Array.from(categories).sort().forEach(category => {
                    const option = document.createElement('option');
                    option.value = category;
                    option.textContent = category;
                    categorySelect.appendChild(option);
                });
            }
        })
        .catch(error => {
            console.error('Errore nel caricamento delle categorie:', error);
        });
}

// Logout
function logout() {
    if (window.authUtils && window.authUtils.logout) {
        window.authUtils.logout();
    } else {
        localStorage.clear();
        window.location.href = 'login.html';
    }
}

// Assicuriamoci che le funzioni principali siano disponibili globalmente
window.showSection = showSection;
window.toggleSidebar = toggleSidebar;
window.viewAnnotation = viewAnnotation;
window.editAnnotation = editAnnotation;
window.confirmDeleteAnnotation = confirmDeleteAnnotation;
window.performGlobalSearch = performGlobalSearch;
window.logout = logout;
