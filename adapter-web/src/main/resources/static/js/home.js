// Configurazione per la home
const HOME_BASE_URL = '/api';

// Variabili globali
let dashboardData = null;

// Inizializzazione della pagina home
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    checkAuthentication();
    
    // Carica i dati della dashboard
    loadDashboardData();
    
    // Setup event listeners
    setupEventListeners();
});

// Verifica che l'utente sia autenticato
function checkAuthentication() {
    if (!window.authUtils || !window.authUtils.isAuthenticated()) {
        // Non autenticato, redirect al login
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
        document.getElementById('welcome-user-name').textContent = currentUser.firstName || currentUser.username;
    }
}

// Setup event listeners
function setupEventListeners() {
    // Auto-refresh ogni 5 minuti
    setInterval(loadDashboardData, 5 * 60 * 1000);
}

// Carica i dati della dashboard
async function loadDashboardData() {
    try {
        // Carica statistiche parallele
        const [annotationsStats, tasksStats, recentActivities, publicAnnotations] = await Promise.all([
            loadAnnotationsStats(),
            loadTasksStats(),
            loadRecentActivities(),
            loadPublicAnnotations()
        ]);
        
        // Aggiorna l'interfaccia
        updateStatistics(annotationsStats, tasksStats, publicAnnotations);
        displayRecentActivities(recentActivities);
        displayPublicAnnotations(publicAnnotations.recent);
        
    } catch (error) {
        console.error('Errore nel caricamento della dashboard:', error);
        showAlert('Errore nel caricamento dei dati', 'warning');
    }
}

// Carica statistiche delle annotazioni
async function loadAnnotationsStats() {
    try {
        const response = await window.authUtils.authenticatedFetch(HOME_BASE_URL + '/annotazioni/statistiche');
        
        if (response.ok) {
            return await response.json();
        } else {
            console.warn('Endpoint statistiche annotazioni non disponibile');
            return { totaleAnnotazioni: 0 };
        }
    } catch (error) {
        console.warn('Errore nel caricamento statistiche annotazioni:', error);
        return { totaleAnnotazioni: 0 };
    }
}

// Carica statistiche dei tasks (placeholder per futuro sviluppo)
async function loadTasksStats() {
    try {
        // Placeholder per quando verrà implementato l'endpoint dei tasks
        const response = await window.authUtils.authenticatedFetch(HOME_BASE_URL + '/tasks/statistiche');
        
        if (response.ok) {
            return await response.json();
        } else {
            console.warn('Endpoint statistiche tasks non disponibile');
            return { totaleTasks: 0, tasksInScadenza: 0, completateOggi: 0 };
        }
    } catch (error) {
        console.warn('Errore nel caricamento statistiche tasks:', error);
        return { totaleTasks: 0, tasksInScadenza: 0, completateOggi: 0 };
    }
}

// Carica annotazioni pubbliche per la sezione dedicata
async function loadPublicAnnotations() {
    try {
        const response = await window.authUtils.authenticatedFetch(HOME_BASE_URL + '/annotazioni/pubbliche');
        if (response.ok) {
            const publicAnnotations = await response.json();
            // Restituisce un oggetto con sia il totale che le prime 5
            return {
                total: publicAnnotations.length,
                recent: publicAnnotations
                    .sort((a, b) => new Date(b.dataUltimaModifica) - new Date(a.dataUltimaModifica))
                    .slice(0, 5)
            };
        } else {
            console.warn('Endpoint annotazioni pubbliche non disponibile');
            return { total: 0, recent: [] };
        }
    } catch (error) {
        console.warn('Errore nel caricamento annotazioni pubbliche:', error);
        return { total: 0, recent: [] };
    }
}

// Carica attività recenti
async function loadRecentActivities() {
    try {
        const activities = [];
        
        // Carica SOLO annotazioni pubbliche per la sezione "Attività recenti"
        try {
            const publicAnnotationsResponse = await window.authUtils.authenticatedFetch(HOME_BASE_URL + '/annotazioni/pubbliche');
            if (publicAnnotationsResponse.ok) {
                const publicAnnotations = await publicAnnotationsResponse.json();
                
                // Prendi le 10 annotazioni pubbliche più recenti
                const recentPublicAnnotations = publicAnnotations
                    .sort((a, b) => new Date(b.dataUltimaModifica) - new Date(a.dataUltimaModifica))
                    .slice(0, 10);
                
                recentPublicAnnotations.forEach(annotation => {
                    const userInfo = annotation.utenteUltimaModifica && annotation.utenteUltimaModifica !== annotation.utenteCreazione
                        ? `${annotation.utenteCreazione} (mod. da ${annotation.utenteUltimaModifica})`
                        : annotation.utenteCreazione || annotation.utente || 'Sconosciuto';
                    
                    activities.push({
                        type: 'annotation',
                        title: annotation.descrizione || 'Annotazione',
                        content: annotation.valoreNota || '',
                        date: annotation.dataUltimaModifica,
                        user: userInfo,
                        icon: 'bi-journal-text',
                        color: 'success' // Verde per annotazioni pubbliche
                    });
                });
            }
        } catch (error) {
            console.warn('Errore nel caricamento annotazioni pubbliche:', error);
        }
        
        // TODO: Aggiungere tasks recenti quando saranno implementati
        
        // Ordina per data (già ordinato sopra, ma per sicurezza)
        activities.sort((a, b) => new Date(b.date) - new Date(a.date));
        
        return activities;
        
    } catch (error) {
        console.error('Errore nel caricamento attività recenti:', error);
        return [];
    }
}

// Aggiorna le statistiche nell'interfaccia
function updateStatistics(annotationsStats, tasksStats, publicAnnotations) {
    // Statistiche annotazioni
    document.getElementById('total-annotations').textContent = annotationsStats.totaleAnnotazioni || 0;
    
    // Statistiche annotazioni pubbliche
    const publicCount = publicAnnotations && publicAnnotations.total ? publicAnnotations.total : 0;
    document.getElementById('public-annotations').textContent = publicCount;
    
    // Statistiche tasks (placeholder)
    document.getElementById('total-tasks').textContent = tasksStats.totaleTasks || 0;
    document.getElementById('pending-tasks').textContent = tasksStats.tasksInScadenza || 0;
}

// Mostra le attività recenti
function displayRecentActivities(activities) {
    const container = document.getElementById('recent-activities');
    
    if (!activities || activities.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-inbox text-muted display-4"></i>
                <p class="text-muted mt-2">Nessuna attività recente</p>
            </div>
        `;
        return;
    }
    
    let html = '';
    activities.forEach((activity, index) => {
        const isLast = index === activities.length - 1;
        html += `
            <div class="d-flex align-items-start mb-3 ${isLast ? '' : 'border-bottom pb-3'}">
                <div class="flex-shrink-0 me-3">
                    <div class="bg-${activity.color} text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                        <i class="bi ${activity.icon}"></i>
                    </div>
                </div>
                <div class="flex-grow-1">
                    <h6 class="mb-1">${escapeHtml(activity.title)}</h6>
                    <p class="mb-1 text-muted small">${truncateText(activity.content, 80)}</p>
                    <small class="text-muted">
                        <i class="bi bi-person"></i> ${escapeHtml(activity.user || 'Sconosciuto')} • 
                        <i class="bi bi-clock"></i> ${formatRelativeTime(activity.date)}
                    </small>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Mostra le annotazioni pubbliche
function displayPublicAnnotations(annotations) {
    const container = document.getElementById('public-annotations-list');
    
    if (!annotations || annotations.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-globe text-muted display-4"></i>
                <p class="text-muted mt-2">Nessuna annotazione pubblica disponibile</p>
            </div>
        `;
        return;
    }
    
    const html = annotations.map(annotation => {
        const userInfo = annotation.utenteUltimaModifica && annotation.utenteUltimaModifica !== annotation.utenteCreazione
            ? `Creata da ${escapeHtml(annotation.utenteCreazione)}, Modificata da ${escapeHtml(annotation.utenteUltimaModifica)}`
            : `Creata da ${escapeHtml(annotation.utenteCreazione)}`;
            
        return `
            <div class="border-bottom pb-3 mb-3">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <h6 class="mb-1">${escapeHtml(annotation.descrizione)}</h6>
                        <p class="text-muted mb-2 small">${escapeHtml(annotation.valoreNota.substring(0, 100))}${annotation.valoreNota.length > 100 ? '...' : ''}</p>
                        <div class="d-flex align-items-center text-muted small">
                            <i class="bi bi-person me-1"></i>
                            <span class="me-3">${userInfo}</span>
                            <i class="bi bi-clock me-1"></i>
                            <span>${formatDate(annotation.dataUltimaModifica)}</span>
                        </div>
                        ${annotation.categoria ? `<span class="badge bg-secondary me-1 mt-1">${escapeHtml(annotation.categoria)}</span>` : ''}
                        ${annotation.tags ? annotation.tags.split(',').map(tag => `<span class="badge bg-outline-primary me-1 mt-1">${escapeHtml(tag.trim())}</span>`).join('') : ''}
                    </div>
                    <button class="btn btn-sm btn-outline-primary ms-2" onclick="viewPublicAnnotation('${annotation.id}')">
                        <i class="bi bi-eye"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
    
    container.innerHTML = html;
}

// Toggle della sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
}

// Logout
function logout() {
    if (window.authUtils && window.authUtils.logout) {
        window.authUtils.logout();
    } else {
        // Fallback se authUtils non è disponibile
        localStorage.clear();
        window.location.href = 'login.html';
    }
}

// Visualizza una annotazione pubblica (reindirizza alla pagina annotazioni)
function viewPublicAnnotation(id) {
    window.location.href = `annotazioni.html?view=${id}`;
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

function formatRelativeTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffMins < 1) return 'Ora';
    if (diffMins < 60) return `${diffMins} minuti fa`;
    if (diffHours < 24) return `${diffHours} ore fa`;
    if (diffDays < 7) return `${diffDays} giorni fa`;
    
    return date.toLocaleDateString('it-IT');
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('it-IT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
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

// Rendi le funzioni disponibili globalmente
window.toggleSidebar = toggleSidebar;
window.logout = logout;
window.viewPublicAnnotation = viewPublicAnnotation;
