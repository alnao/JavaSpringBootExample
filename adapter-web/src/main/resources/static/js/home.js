// Configurazione per la home
const API_BASE_URL = '/api';

// Variabili globali
let currentUser = null;
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
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('currentUser');
    
    if (!token || !user) {
        // Non autenticato, redirect al login
        window.location.href = 'login.html';
        return;
    }
    
    try {
        currentUser = JSON.parse(user);
        displayUserInfo();
    } catch (error) {
        console.error('Errore nel parsing dei dati utente:', error);
        window.location.href = 'login.html';
    }
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
        const [annotationsStats, tasksStats, recentActivities] = await Promise.all([
            loadAnnotationsStats(),
            loadTasksStats(),
            loadRecentActivities()
        ]);
        
        // Aggiorna l'interfaccia
        updateStatistics(annotationsStats, tasksStats);
        displayRecentActivities(recentActivities);
        
    } catch (error) {
        console.error('Errore nel caricamento della dashboard:', error);
        showAlert('Errore nel caricamento dei dati', 'warning');
    }
}

// Carica statistiche delle annotazioni
async function loadAnnotationsStats() {
    try {
        const response = await window.authUtils.authenticatedFetch(API_BASE_URL + '/annotazioni/statistiche');
        
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
        const response = await window.authUtils.authenticatedFetch(API_BASE_URL + '/tasks/statistiche');
        
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

// Carica attività recenti
async function loadRecentActivities() {
    try {
        // Combina annotazioni recenti e tasks recenti (quando disponibili)
        const activities = [];
        
        // Carica annotazioni recenti
        try {
            const annotationsResponse = await window.authUtils.authenticatedFetch(API_BASE_URL + '/annotazioni');
            if (annotationsResponse.ok) {
                const annotations = await annotationsResponse.json();
                
                // Prendi le 5 annotazioni più recenti
                const recentAnnotations = annotations
                    .sort((a, b) => new Date(b.dataUltimaModifica) - new Date(a.dataUltimaModifica))
                    .slice(0, 5);
                
                recentAnnotations.forEach(annotation => {
                    activities.push({
                        type: 'annotation',
                        title: annotation.descrizione,
                        content: annotation.valoreNota,
                        date: annotation.dataUltimaModifica,
                        icon: 'bi-journal-text',
                        color: 'primary'
                    });
                });
            }
        } catch (error) {
            console.warn('Errore nel caricamento annotazioni recenti:', error);
        }
        
        // TODO: Aggiungere tasks recenti quando saranno implementati
        
        // Ordina per data
        activities.sort((a, b) => new Date(b.date) - new Date(a.date));
        
        return activities.slice(0, 10); // Mostra solo le ultime 10 attività
        
    } catch (error) {
        console.error('Errore nel caricamento attività recenti:', error);
        return [];
    }
}

// Aggiorna le statistiche nell'interfaccia
function updateStatistics(annotationsStats, tasksStats) {
    // Statistiche annotazioni
    document.getElementById('total-annotations').textContent = annotationsStats.totaleAnnotazioni || 0;
    
    // Statistiche tasks (placeholder)
    document.getElementById('total-tasks').textContent = tasksStats.totaleTasks || 0;
    document.getElementById('pending-tasks').textContent = tasksStats.tasksInScadenza || 0;
    document.getElementById('completed-today').textContent = tasksStats.completateOggi || 0;
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
                        <i class="bi bi-clock"></i> ${formatRelativeTime(activity.date)}
                    </small>
                </div>
            </div>
        `;
    });
    
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
