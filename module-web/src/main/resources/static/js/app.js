// Configurazione base
const API_BASE_URL = '/api/annotazioni';
let currentUser = 'utente-demo';
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
                    <th>Utente</th>
                    <th>Ultima Modifica</th>
                    <th>Azioni</th>
                </tr>
            </thead>
            <tbody>
    `;
    
    recent.forEach(annotation => {
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
                    <i class="bi bi-person"></i> ${escapeHtml(annotation.utenteCreazione)}
                </td>
                <td>
                    <small>${formatDate(annotation.dataUltimaModifica)}</small>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="viewAnnotation('${annotation.id}')">
                        <i class="bi bi-eye"></i>
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

// Visualizzazione delle annotazioni
function displayAnnotations(annotations) {
    const container = document.getElementById('annotations-container');
    
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
        
        html += `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card card-annotazione h-100">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">${escapeHtml(annotation.descrizione)}</h6>
                        <span class="badge ${priorityClass} badge-priority">${priorityText}</span>
                    </div>
                    <div class="card-body">
                        <div class="annotazione-content mb-2">
                            ${escapeHtml(annotation.valoreNota)}
                        </div>
                        
                        <div class="mb-2">
                            ${annotation.categoria ? `<span class="badge bg-secondary me-1">${escapeHtml(annotation.categoria)}</span>` : ''}
                            ${annotation.pubblica ? '<span class="badge bg-success me-1"><i class="bi bi-globe"></i> Pubblica</span>' : ''}
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
                            <button class="btn btn-outline-danger btn-sm" onclick="deleteAnnotation('${annotation.id}')">
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
        displayAnnotations(publicAnnotations);
    } catch (error) {
        console.error('Errore nel caricamento delle annotazioni pubbliche:', error);
        showAlert('Errore nel caricamento delle annotazioni pubbliche', 'danger');
    }
}

// Gestione creazione annotazione
async function handleCreateAnnotation(event) {
    event.preventDefault();
    
    const formData = {
        valoreNota: document.getElementById('annotation-content').value,
        descrizione: document.getElementById('annotation-title').value,
        utente: document.getElementById('annotation-user').value,
        categoria: document.getElementById('annotation-category').value || null,
        tags: document.getElementById('annotation-tags').value || null,
        pubblica: document.getElementById('annotation-public').checked,
        priorita: parseInt(document.getElementById('annotation-priority').value)
    };
    
    try {
        const response = await fetchAPI('', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        showAlert('Annotazione creata con successo!', 'success');
        resetForm();
        showSection('annotazioni');
        
    } catch (error) {
        console.error('Errore nella creazione dell\'annotazione:', error);
        showAlert('Errore nella creazione dell\'annotazione', 'danger');
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
        showAlert('Inserisci un testo da cercare', 'warning');
        return;
    }
    
    try {
        const results = await fetchAPI(`/cerca?testo=${encodeURIComponent(searchText)}`);
        const container = document.getElementById('search-results-container');
        
        if (results.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle"></i> Nessun risultato trovato per "${escapeHtml(searchText)}"
                    </div>
                </div>
            `;
        } else {
            displayAnnotations(results);
        }
        
    } catch (error) {
        console.error('Errore nella ricerca:', error);
        showAlert('Errore nella ricerca', 'danger');
    }
}

// Visualizzazione dettagli annotazione
async function viewAnnotation(id) {
    try {
        const annotation = await fetchAPI(`/${id}`);
        showAnnotationModal(annotation, 'view');
    } catch (error) {
        console.error('Errore nel caricamento dell\'annotazione:', error);
        showAlert('Errore nel caricamento dell\'annotazione', 'danger');
    }
}

// Modifica annotazione
async function editAnnotation(id) {
    try {
        const annotation = await fetchAPI(`/${id}`);
        showAnnotationModal(annotation, 'edit');
    } catch (error) {
        console.error('Errore nel caricamento dell\'annotazione:', error);
        showAlert('Errore nel caricamento dell\'annotazione', 'danger');
    }
}

// Eliminazione annotazione
async function deleteAnnotation(id) {
    if (!confirm('Sei sicuro di voler eliminare questa annotazione?')) {
        return;
    }
    
    try {
        await fetchAPI(`/${id}`, { method: 'DELETE' });
        showAlert('Annotazione eliminata con successo!', 'success');
        loadAllAnnotations(); // Ricarica la lista
    } catch (error) {
        console.error('Errore nell\'eliminazione dell\'annotazione:', error);
        showAlert('Errore nell\'eliminazione dell\'annotazione', 'danger');
    }
}

// Mostra modal annotazione
function showAnnotationModal(annotation, mode) {
    const modal = new bootstrap.Modal(document.getElementById('annotationModal'));
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const modalFooter = document.getElementById('modalFooter');
    
    if (mode === 'view') {
        modalTitle.textContent = 'Dettagli Annotazione';
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-12 mb-3">
                    <h5>${escapeHtml(annotation.descrizione)}</h5>
                </div>
                <div class="col-12 mb-3">
                    <label class="form-label fw-bold">Contenuto:</label>
                    <div class="border rounded p-3" style="min-height: 100px; white-space: pre-wrap;">${escapeHtml(annotation.valoreNota)}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Categoria:</label>
                    <div>${annotation.categoria || 'Non specificata'}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Priorità:</label>
                    <div><span class="badge ${getPriorityClass(annotation.priorita)}">${getPriorityText(annotation.priorita)}</span></div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Visibilità:</label>
                    <div>${annotation.pubblica ? '<span class="badge bg-success">Pubblica</span>' : '<span class="badge bg-secondary">Privata</span>'}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Tag:</label>
                    <div>${annotation.tags || 'Nessun tag'}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Creata da:</label>
                    <div>${escapeHtml(annotation.utenteCreazione)}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Data creazione:</label>
                    <div>${formatDate(annotation.dataInserimento)}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Ultima modifica:</label>
                    <div>${formatDate(annotation.dataUltimaModifica)}</div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label fw-bold">Modificata da:</label>
                    <div>${escapeHtml(annotation.utenteUltimaModifica)}</div>
                </div>
            </div>
        `;
        modalFooter.innerHTML = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
            <button type="button" class="btn btn-primary" onclick="editAnnotation('${annotation.id}')">
                <i class="bi bi-pencil"></i> Modifica
            </button>
        `;
    }
    
    modal.show();
}

// Funzioni di utilità
async function fetchAPI(endpoint, options = {}) {
    const response = await fetch(API_BASE_URL + endpoint, options);
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
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
