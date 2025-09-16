// Configurazione per la gestione stati
const TASKS_BASE_URL = '/api';

// Variabili globali
let allAnnotations = [];
let availableTransitions = [];
let currentUser = null;
let selectedAnnotation = null;
let selectedTransition = null;

// Mapping dei colori per gli stati
const STATE_COLORS = {
    'INSERITA': 'bg-info',
    'MODIFICATA': 'bg-warning',
    'CONFERMATA': 'bg-success',
    'RIFIUTATA': 'bg-danger',
    'DAINVIARE': 'bg-primary',
    'INVIATA': 'bg-primary',
    'BANNATA': 'bg-dark',
    'ERRORE': 'bg-danger',
    'SCADUTA': 'bg-danger'
};

// Mapping delle icone per gli stati
const STATE_ICONS = {
    'INSERITA': 'bi-plus-circle',
    'MODIFICATA': 'bi-pencil-square',
    'CONFERMATA': 'bi-check-circle',
    'RIFIUTATA': 'bi-x-circle',
    'DAINVIARE': 'bi-globe',
    'INVIATA': 'bi-globe',
    'BANNATA': 'bi-ban',
    'ERRORE': 'bi-exclamation-triangle',
    'SCADUTA': 'bi-clock-history'
};

// Inizializzazione della pagina
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    checkAuthentication();
    
    // Carica i dati iniziali
    loadInitialData();
    
    // Setup event listeners
    setupEventListeners();
});

// Verifica che l'utente sia autenticato
function checkAuthentication() {
    if (!window.authUtils || !window.authUtils.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }
    
    const user = window.authUtils.getCurrentUser();
    if (!user) {
        window.location.href = 'login.html';
        return;
    }
    
    currentUser = user;
    displayUserInfo();
}

// Mostra le informazioni dell'utente
function displayUserInfo() {
    if (currentUser) {
        const userName = currentUser.firstName ? 
            `${currentUser.firstName} ${currentUser.lastName || ''}`.trim() : 
            currentUser.username || currentUser.email;
        
        document.getElementById('user-name').textContent = userName;
    }
}

// Setup event listeners
function setupEventListeners() {
    // Filtri
    document.getElementById('filter-stato').addEventListener('change', applyFilters);
    document.getElementById('search-text').addEventListener('input', debounce(applyFilters, 500));
    
    // Modal conferma cambio stato
    document.getElementById('confirm-state-change-btn').addEventListener('click', executeStateChange);
}

// Debounce function per la ricerca
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Carica i dati iniziali
async function loadInitialData() {
    showLoading(true);
    
    try {
        // Carica annotazioni e transizioni in parallelo
        const [annotationsResult, transitionsResult] = await Promise.all([
            loadAnnotations(),
            loadAvailableTransitions()
        ]);
        
        if (annotationsResult && transitionsResult) {
            displayAnnotations(allAnnotations);
        }
    } catch (error) {
        console.error('Errore nel caricamento dei dati:', error);
        showAlert('Errore nel caricamento dei dati', 'danger');
    } finally {
        showLoading(false);
    }
}

// Carica tutte le annotazioni
async function loadAnnotations() {
    try {
        const response = await window.authUtils.authenticatedFetch(TASKS_BASE_URL + '/annotazioni');
        
        if (response.ok) {
            allAnnotations = await response.json();
            return true;
        } else {
            console.error('Errore nel caricamento delle annotazioni');
            return false;
        }
    } catch (error) {
        console.error('Errore nel caricamento delle annotazioni:', error);
        return false;
    }
}

// Carica le transizioni disponibili
async function loadAvailableTransitions() {
    try {
        const response = await window.authUtils.authenticatedFetch(TASKS_BASE_URL + '/annotazioni/transizioni-stato');
        
        if (response.ok) {
            availableTransitions = await response.json();
            return true;
        } else {
            console.error('Errore nel caricamento delle transizioni');
            return false;
        }
    } catch (error) {
        console.error('Errore nel caricamento delle transizioni:', error);
        return false;
    }
}

// Mostra/nasconde il loading
function showLoading(show) {
    const loading = document.getElementById('loading');
    const tableContainer = document.querySelector('.card');
    const noAnnotations = document.getElementById('no-annotations');
    
    if (show) {
        loading.style.display = 'block';
        tableContainer.style.display = 'none';
        noAnnotations.style.display = 'none';
    } else {
        loading.style.display = 'none';
        tableContainer.style.display = 'block';
    }
}

// Visualizza le annotazioni nella tabella
function displayAnnotations(annotations) {
    const tbody = document.getElementById('annotations-table-body');
    const noAnnotations = document.getElementById('no-annotations');
    const tableContainer = document.querySelector('.card');
    
    if (!annotations || annotations.length === 0) {
        tableContainer.style.display = 'none';
        noAnnotations.style.display = 'block';
        return;
    }
    
    tableContainer.style.display = 'block';
    noAnnotations.style.display = 'none';
    
    // Ordina per priorità (decrescente) e poi per versione
    const sortedAnnotations = [...annotations].sort((a, b) => {
        // Prima ordina per priorità (più alta prima)
        const priorityDiff = (b.priorita || 0) - (a.priorita || 0);
        if (priorityDiff !== 0) return priorityDiff;
        
        // Se priorità uguale, ordina per versione (più recente prima)
        const versionA = a.versioneNota || '';
        const versionB = b.versioneNota || '';
        return versionB.localeCompare(versionA);
    });
    
    tbody.innerHTML = '';
    
    sortedAnnotations.forEach(annotation => {
        const row = createAnnotationRow(annotation);
        tbody.appendChild(row);
    });
}

// Crea una riga per l'annotazione
function createAnnotationRow(annotation) {
    const row = document.createElement('tr');
    row.className = 'annotation-row';
    
    const stateColor = STATE_COLORS[annotation.stato] || 'bg-secondary';
    const stateIcon = STATE_ICONS[annotation.stato] || 'bi-question-circle';
    
    // Trova le transizioni possibili per questa annotazione e utente
    const possibleTransitions = getPossibleTransitions(annotation.stato, currentUser);
    
    // Gestisce la priorità con badge colorato
    const priorityBadge = getPriorityBadge(annotation.priorita || 1);
    
    row.innerHTML = `
        <td>
            <div class="text-truncate-content" title="${annotation.descrizione || 'N/A'}">
                ${annotation.descrizione || 'N/A'}
            </div>
        </td>
        <td class="hide">
            <small>${annotation.utenteCreazione || 'N/A'}</small>
        </td>
        <td>
            ${priorityBadge}
        </td>
        <td>
            <small class="text-muted">${annotation.versioneNota || 'N/A'}</small>
        </td>
        <td>
            <span class="badge stato-badge ${stateColor}">
                <i class="bi ${stateIcon}"></i> ${annotation.stato}
            </span>
        </td>
        <td>
            ${createActionButtons(annotation, possibleTransitions)}
        </td>
    `;
    
    return row;
}

// Ottiene le transizioni possibili per uno stato e un utente
function getPossibleTransitions(currentState, user) {
    if (!availableTransitions || !user) return [];
    
    const userRole = getUserRole(user);
    
    return availableTransitions.filter(transition => {
        // Regola speciale: INSERITA non può andare a MODIFICATA
        if (currentState === 'INSERITA' && transition.statoArrivo === 'MODIFICATA') {
            return false;
        }
        
        return transition.statoPartenza === currentState &&
               isRoleAllowed(userRole, transition.ruoloRichiesto);
    });
}

// Determina il ruolo dell'utente
function getUserRole(user) {

    // Usa il role salvato nei dati dell'utente
    if (user.role) {
        //rimuovi "ROLE_" se presente
        if (user.role.startsWith("ROLE_")) {
            return user.role.substring(5);
        }
        return user.role;
    }
    
    // Fallback: logica semplificata basata sul username (per compatibilità)
    if (user.username === 'admin') return 'ADMIN';
    if (user.username === 'moderator') return 'MODERATOR';
    return 'USER';
}

// Verifica se un ruolo utente ha i permessi per una transizione
function isRoleAllowed(userRole, requiredRole) {
    // Gerarchia dei ruoli: SYSTEM > ADMIN > MODERATOR > USER
    const roleHierarchy = {
        'USER': 1,
        'MODERATOR': 2,
        'ADMIN': 3,
        'SYSTEM': 4
    };
    
    const userLevel = roleHierarchy[userRole] || 0;
    const requiredLevel = roleHierarchy[requiredRole] || 0;
    
    return userLevel >= requiredLevel;
}

// Ottiene il badge per la priorità
function getPriorityBadge(priority) {
    const priorityColors = {
        1: { class: 'bg-success', text: 'Bassa' },
        2: { class: 'bg-info', text: 'Normale' },
        3: { class: 'bg-warning text-dark', text: 'Alta' },
        4: { class: 'bg-danger', text: 'Critica' },
        5: { class: 'bg-dark', text: 'Urgente' }
    };
    
    const config = priorityColors[priority] || priorityColors[1];
    return `<span class="badge ${config.class}">${config.text} (${priority})</span>`;
}

// Crea i pulsanti di azione per ogni annotazione
function createActionButtons(annotation, possibleTransitions) {
    if (!possibleTransitions || possibleTransitions.length === 0) {
        return '<small class="text-muted">Nessuna azione disponibile</small>';
    }
    
    let buttonsHtml = '<div class="btn-group gap-1">';
    
    possibleTransitions.forEach(transition => {
        const buttonColor = getButtonColorForState(transition.statoArrivo);
        const buttonIcon = STATE_ICONS[transition.statoArrivo] || 'bi-arrow-right';
        
        buttonsHtml += `
            <button class="btn btn-${buttonColor} btn-stato" 
                    onclick="showStateChangeModal('${annotation.id}', '${annotation.stato}', '${transition.statoArrivo}', '${annotation.descrizione || 'N/A'}')"
                    title="Cambia stato a ${transition.statoArrivo}">
                <i class="bi ${buttonIcon}"></i> ${transition.statoArrivo}
            </button>
        `;
    });
    
    buttonsHtml += '</div>';
    return buttonsHtml;
}

// Ottiene il colore del pulsante per uno stato
function getButtonColorForState(state) {
    const colors = {
        'INSERITA': 'info',
        'MODIFICATA': 'warning',
        'CONFERMATA': 'success',
        'RIFIUTATA': 'danger',
        'DAINVIARE': 'primary',
        'INVIATA': 'primary',
        'BANNATA': 'dark',
        'ERRORE': 'danger',
        'SCADUTA': 'danger'
    };
    return colors[state] || 'secondary';
}

// Mostra il modal di conferma cambio stato
function showStateChangeModal(annotationId, oldState, newState, description) {
    const annotation = allAnnotations.find(a => a.id === annotationId);
    if (!annotation) return;
    
    selectedAnnotation = annotation;
    selectedTransition = { from: oldState, to: newState };
    
    // Popola il modal con la descrizione
    document.getElementById('modal-annotation-description').textContent = description;
    document.getElementById('modal-old-state').textContent = oldState;
    document.getElementById('modal-new-state').textContent = newState;
    
    // Applica i colori appropriati
    const oldStateColor = STATE_COLORS[oldState] || 'bg-secondary';
    const newStateColor = STATE_COLORS[newState] || 'bg-primary';
    
    document.getElementById('modal-old-state').className = `badge ${oldStateColor}`;
    document.getElementById('modal-new-state').className = `badge ${newStateColor}`;
    
    // Mostra il modal
    const modal = new bootstrap.Modal(document.getElementById('confirmStateChangeModal'));
    modal.show();
}

// Esegue il cambio stato
async function executeStateChange() {
    if (!selectedAnnotation || !selectedTransition) return;
    
    const confirmBtn = document.getElementById('confirm-state-change-btn');
    const originalText = confirmBtn.innerHTML;
    
    try {
        // Disabilita il pulsante e mostra loading
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Elaborazione...';
        
        const requestBody = {
            vecchioStato: selectedTransition.from,
            nuovoStato: selectedTransition.to,
            utente: currentUser.username || 'utente-demo'
        };
        
        const response = await window.authUtils.authenticatedFetch(
            `${TASKS_BASE_URL}/annotazioni/${selectedAnnotation.id}/stato`,
            {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            }
        );
        
        if (response.ok) {
            // Nascondi il modal di conferma
            const confirmModal = bootstrap.Modal.getInstance(document.getElementById('confirmStateChangeModal'));
            confirmModal.hide();
            
            // Mostra il modal di successo
            const successModal = new bootstrap.Modal(document.getElementById('successModal'));
            successModal.show();
            
        } else {
            const errorText = await response.text();
            throw new Error(`Errore nel cambio stato: ${errorText}`);
        }
        
    } catch (error) {
        console.error('Errore nel cambio stato:', error);
        showAlert('Errore nel cambio stato: ' + error.message, 'danger');
    } finally {
        // Ripristina il pulsante
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalText;
    }
}

// Va alla home page
function goToHome() {
    window.location.href = 'home.html';
}

// Applica i filtri
function applyFilters() {
    const statoFilter = document.getElementById('filter-stato').value;
    const searchText = document.getElementById('search-text').value.toLowerCase();
    
    let filteredAnnotations = allAnnotations;
    
    // Filtro per stato
    if (statoFilter) {
        filteredAnnotations = filteredAnnotations.filter(a => a.stato === statoFilter);
    }
    
    // Filtro per testo nella descrizione
    if (searchText) {
        filteredAnnotations = filteredAnnotations.filter(a => 
            (a.descrizione && a.descrizione.toLowerCase().includes(searchText)) ||
            (a.versioneNota && a.versioneNota.toLowerCase().includes(searchText))
        );
    }
    
    displayAnnotations(filteredAnnotations);
}

// Pulisce i filtri
function clearFilters() {
    document.getElementById('filter-stato').value = '';
    document.getElementById('search-text').value = '';
    displayAnnotations(allAnnotations);
}

// Aggiorna le annotazioni
async function refreshAnnotations() {
    await loadInitialData();
    clearFilters();
}

// Formatta una data
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return 'N/A';
    
    return date.toLocaleDateString('it-IT', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Mostra un alert
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x`;
    alertDiv.style.zIndex = '9999';
    alertDiv.style.marginTop = '20px';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Rimuovi automaticamente dopo 5 secondi
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.parentNode.removeChild(alertDiv);
        }
    }, 5000);
}

// Toggle sidebar (per mobile)
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('show');
    } else {
        sidebar.classList.toggle('collapsed');
        mainContent.classList.toggle('expanded');
    }
}

// Logout
function logout() {
    if (window.authUtils) {
        window.authUtils.logout();
    }
    window.location.href = 'login.html';
}
