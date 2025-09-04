// Configurazione annotazioni
const ANNOTATIONS_API_BASE_URL = '/api/annotazioni';
let currentUser = 'utente-demo';
let allAnnotations = [];
let currentEditingId = null;

// Inizializzazione della pagina annotazioni
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    checkAuthentication();
    
    // Carica i dati dell'utente
    loadUserData();
    
    // Setup event listeners
    setupAnnotationsEventListeners();
    
    // Gestisci hash URL per navigazione diretta o mostra dashboard di default
    if (!handleUrlHash()) {
        showSection('dashboard');
    }
});

// Verifica autenticazione
function checkAuthentication() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Carica dati utente
function loadUserData() {
    const user = localStorage.getItem('currentUser');
    if (user) {
        try {
            const userData = JSON.parse(user);
            currentUser = userData.username || userData.email;
            
            const userName = userData.firstName ? 
                `${userData.firstName} ${userData.lastName || ''}`.trim() : 
                userData.username || userData.email;
            
            document.getElementById('user-name').textContent = userName;
        } catch (error) {
            console.error('Errore nel parsing dei dati utente:', error);
        }
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
        const publicAnnotations = await fetchAnnotationsAPI('/pubbliche');
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
    
    try {
        const response = await fetchAnnotationsAPI('', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        showAlert('Annotazione creata con successo!', 'success');
        resetForm();
        
        // Vai alle annotazioni per vedere il risultato
        showSection('annotazioni');
        
    } catch (error) {
        console.error('Errore nella creazione dell\'annotazione:', error);
        showAlert('Errore nella creazione dell\'annotazione', 'danger');
    }
}

// Reset del form
function resetForm() {
    const createForm = document.getElementById('create-annotation-form');
    if (createForm) {
        createForm.reset();
        document.getElementById('annotation-user').value = currentUser;
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

// Funzioni placeholder per completare l'implementazione
function performGlobalSearch() {
    // TODO: Implementare ricerca globale
    showAlert('Funzione ricerca globale in sviluppo', 'info');
}

function viewAnnotation(id) {
    // TODO: Implementare visualizzazione dettagli
    showAlert('Funzione visualizzazione in sviluppo', 'info');
}

function editAnnotation(id) {
    // TODO: Implementare modifica
    showAlert('Funzione modifica in sviluppo', 'info');
}

function confirmDeleteAnnotation(id) {
    // TODO: Implementare eliminazione
    showAlert('Funzione eliminazione in sviluppo', 'info');
}

function handleEditAnnotation(event) {
    // TODO: Implementare gestione modifica
    event.preventDefault();
    showAlert('Funzione modifica in sviluppo', 'info');
}

function handleAddCategory(event) {
    // TODO: Implementare gestione categorie
    event.preventDefault();
    showAlert('Funzione gestione categorie in sviluppo', 'info');
}

function loadCategories() {
    // TODO: Implementare caricamento categorie
    showAlert('Funzione categorie in sviluppo', 'info');
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
