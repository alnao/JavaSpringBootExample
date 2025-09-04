// Configurazione utente
const USER_API_BASE_URL = '/api/user';
let currentUser = null;
let userPreferences = {};

// Inizializzazione della pagina utente
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    checkAuthentication();
    
    // Carica dati utente
    loadUserProfile();
    
    // Setup event listeners
    setupUserEventListeners();
    
    // Carica attività recente
    loadRecentActivity();
    
    // Carica preferenze
    loadUserPreferences();
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

// Carica profilo utente
function loadUserProfile() {
    const user = localStorage.getItem('currentUser');
    if (user) {
        try {
            currentUser = JSON.parse(user);
            populateUserProfile(currentUser);
        } catch (error) {
            console.error('Errore nel parsing dei dati utente:', error);
            // Carica dati di fallback
            loadFallbackUserData();
        }
    } else {
        loadFallbackUserData();
    }
}

// Carica dati utente di fallback
function loadFallbackUserData() {
    currentUser = {
        id: '1',
        username: 'utente_demo',
        email: 'demo@example.com',
        firstName: 'Demo',
        lastName: 'Utente',
        bio: 'Utente di esempio per la demo',
        location: 'Milano, Italia',
        timezone: 'Europe/Rome',
        phone: '+39 123 456 7890',
        avatar: null,
        role: 'Utente Standard',
        createdAt: new Date(Date.now() - 45 * 24 * 60 * 60 * 1000), // 45 giorni fa
        lastLogin: new Date(),
        stats: {
            annotations: 12,
            tasks: 8,
            completedTasks: 5,
            activeDays: 45,
            activityStreak: 7
        }
    };
    
    populateUserProfile(currentUser);
}

// Popola il profilo nell'interfaccia
function populateUserProfile(user) {
    // Nome nell'header
    const userName = user.firstName ? 
        `${user.firstName} ${user.lastName || ''}`.trim() : 
        user.username || user.email;
    
    document.getElementById('user-name').textContent = userName;
    
    // Dati del profilo
    document.getElementById('profile-name').textContent = userName;
    document.getElementById('profile-email').textContent = user.email || 'Email non disponibile';
    document.getElementById('profile-role').textContent = `Ruolo: ${user.role || 'Utente'}`;
    
    // Statistiche
    if (user.stats) {
        document.getElementById('profile-annotations').textContent = user.stats.annotations || 0;
        document.getElementById('profile-tasks').textContent = user.stats.tasks || 0;
        document.getElementById('profile-days').textContent = user.stats.activeDays || 0;
        document.getElementById('completed-tasks-count').textContent = user.stats.completedTasks || 0;
        document.getElementById('activity-streak').textContent = user.stats.activityStreak || 0;
    }
    
    // Avatar
    if (user.avatar) {
        document.getElementById('profile-avatar').src = user.avatar;
    } else {
        const initials = getInitials(userName);
        document.getElementById('profile-avatar').src = 
            `https://via.placeholder.com/120x120/6c5ce7/ffffff?text=${encodeURIComponent(initials)}`;
    }
    
    // Form dati personali
    if (user.firstName) document.getElementById('firstName').value = user.firstName;
    if (user.lastName) document.getElementById('lastName').value = user.lastName;
    if (user.email) document.getElementById('email').value = user.email;
    if (user.phone) document.getElementById('phone').value = user.phone;
    if (user.bio) document.getElementById('bio').value = user.bio;
    if (user.location) document.getElementById('location').value = user.location;
    if (user.timezone) document.getElementById('timezone').value = user.timezone;
}

// Setup event listeners
function setupUserEventListeners() {
    // Form profilo
    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileUpdate);
    }
    
    // Form sicurezza
    const securityForm = document.getElementById('security-form');
    if (securityForm) {
        securityForm.addEventListener('submit', handlePasswordChange);
    }
    
    // Upload avatar
    const avatarUpload = document.getElementById('avatar-upload');
    if (avatarUpload) {
        avatarUpload.addEventListener('change', handleAvatarUpload);
    }
    
    // Toggle 2FA
    const twoFactorAuth = document.getElementById('twoFactorAuth');
    if (twoFactorAuth) {
        twoFactorAuth.addEventListener('change', handleTwoFactorToggle);
    }
}

// Gestisce l'aggiornamento del profilo
function handleProfileUpdate(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const updatedUser = {
        ...currentUser,
        firstName: formData.get('firstName') || document.getElementById('firstName').value,
        lastName: formData.get('lastName') || document.getElementById('lastName').value,
        email: formData.get('email') || document.getElementById('email').value,
        phone: formData.get('phone') || document.getElementById('phone').value,
        bio: formData.get('bio') || document.getElementById('bio').value,
        location: formData.get('location') || document.getElementById('location').value,
        timezone: formData.get('timezone') || document.getElementById('timezone').value
    };
    
    // Validazione email
    if (!isValidEmail(updatedUser.email)) {
        showAlert('Inserisci un indirizzo email valido', 'warning');
        return;
    }
    
    // Simulazione aggiornamento
    updateUserProfile(updatedUser);
}

// Aggiorna il profilo utente
async function updateUserProfile(userData) {
    try {
        // Simulazione chiamata API
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Aggiorna i dati locali
        currentUser = userData;
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        // Aggiorna l'interfaccia
        populateUserProfile(currentUser);
        
        showAlert('Profilo aggiornato con successo!', 'success');
        
    } catch (error) {
        console.error('Errore nell\'aggiornamento del profilo:', error);
        showAlert('Errore nell\'aggiornamento del profilo', 'danger');
    }
}

// Gestisce il cambio password
function handlePasswordChange(event) {
    event.preventDefault();
    
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // Validazioni
    if (!currentPassword || !newPassword || !confirmPassword) {
        showAlert('Tutti i campi password sono obbligatori', 'warning');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showAlert('Le password non coincidono', 'warning');
        return;
    }
    
    if (newPassword.length < 8) {
        showAlert('La password deve essere di almeno 8 caratteri', 'warning');
        return;
    }
    
    if (!isStrongPassword(newPassword)) {
        showAlert('La password deve contenere almeno una lettera maiuscola, una minuscola, un numero e un carattere speciale', 'warning');
        return;
    }
    
    // Simulazione cambio password
    changePassword(currentPassword, newPassword);
}

// Cambia la password
async function changePassword(currentPassword, newPassword) {
    try {
        // Simulazione chiamata API
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Reset del form
        document.getElementById('security-form').reset();
        
        showAlert('Password aggiornata con successo!', 'success');
        
    } catch (error) {
        console.error('Errore nel cambio password:', error);
        showAlert('Errore nel cambio password', 'danger');
    }
}

// Gestisce l'upload dell'avatar
function handleAvatarUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validazione file
    if (!file.type.startsWith('image/')) {
        showAlert('Seleziona un\'immagine valida', 'warning');
        return;
    }
    
    if (file.size > 2 * 1024 * 1024) { // 2MB
        showAlert('L\'immagine deve essere inferiore a 2MB', 'warning');
        return;
    }
    
    // Leggi e mostra l'immagine
    const reader = new FileReader();
    reader.onload = function(e) {
        const imgSrc = e.target.result;
        document.getElementById('profile-avatar').src = imgSrc;
        
        // Salva nei dati utente
        currentUser.avatar = imgSrc;
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        showAlert('Avatar aggiornato con successo!', 'success');
    };
    
    reader.readAsDataURL(file);
}

// Gestisce il toggle dell'autenticazione a due fattori
function handleTwoFactorToggle(event) {
    const enabled = event.target.checked;
    
    if (enabled) {
        // Simulazione attivazione 2FA
        showAlert('Autenticazione a due fattori attivata (simulazione)', 'info');
    } else {
        // Simulazione disattivazione 2FA
        showAlert('Autenticazione a due fattori disattivata (simulazione)', 'info');
    }
}

// Carica le preferenze utente
function loadUserPreferences() {
    const savedPreferences = localStorage.getItem('userPreferences');
    if (savedPreferences) {
        try {
            userPreferences = JSON.parse(savedPreferences);
        } catch (error) {
            console.error('Errore nel caricamento delle preferenze:', error);
            userPreferences = getDefaultPreferences();
        }
    } else {
        userPreferences = getDefaultPreferences();
    }
    
    // Applica le preferenze all'interfaccia
    applyPreferencesToUI();
}

// Preferenze di default
function getDefaultPreferences() {
    return {
        language: 'it',
        theme: 'light',
        emailNotifications: true,
        taskReminders: true,
        timezone: 'Europe/Rome'
    };
}

// Applica le preferenze all'interfaccia
function applyPreferencesToUI() {
    if (userPreferences.language) {
        document.getElementById('language').value = userPreferences.language;
    }
    
    if (userPreferences.theme) {
        document.getElementById('theme').value = userPreferences.theme;
    }
    
    document.getElementById('emailNotifications').checked = userPreferences.emailNotifications !== false;
    document.getElementById('taskReminders').checked = userPreferences.taskReminders !== false;
}

// Salva le preferenze
function savePreferences() {
    const preferences = {
        language: document.getElementById('language').value,
        theme: document.getElementById('theme').value,
        emailNotifications: document.getElementById('emailNotifications').checked,
        taskReminders: document.getElementById('taskReminders').checked
    };
    
    userPreferences = { ...userPreferences, ...preferences };
    localStorage.setItem('userPreferences', JSON.stringify(userPreferences));
    
    // Applica il tema se cambiato
    if (preferences.theme === 'dark') {
        document.body.classList.add('dark-theme');
    } else {
        document.body.classList.remove('dark-theme');
    }
    
    showAlert('Preferenze salvate con successo!', 'success');
}

// Carica attività recente
function loadRecentActivity() {
    const activities = generateMockActivities();
    displayRecentActivity(activities);
}

// Genera attività mock
function generateMockActivities() {
    return [
        {
            id: '1',
            type: 'task_completed',
            description: 'Task "Preparare presentazione" completato',
            timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 ore fa
            icon: 'bi-check-circle',
            iconColor: 'text-success'
        },
        {
            id: '2',
            type: 'annotation_created',
            description: 'Nuova annotazione "Note riunione cliente" creata',
            timestamp: new Date(Date.now() - 5 * 60 * 60 * 1000), // 5 ore fa
            icon: 'bi-bookmark',
            iconColor: 'text-info'
        },
        {
            id: '3',
            type: 'profile_updated',
            description: 'Profilo utente aggiornato',
            timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 giorno fa
            icon: 'bi-person',
            iconColor: 'text-primary'
        },
        {
            id: '4',
            type: 'login',
            description: 'Accesso effettuato',
            timestamp: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 giorni fa
            icon: 'bi-box-arrow-in-right',
            iconColor: 'text-secondary'
        }
    ];
}

// Visualizza attività recente
function displayRecentActivity(activities) {
    const container = document.getElementById('recent-activity');
    
    if (!activities || activities.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-activity display-4 text-muted"></i>
                <h5 class="text-muted mt-3">Nessuna attività recente</h5>
                <p class="text-muted">Le tue attività appariranno qui</p>
            </div>
        `;
        return;
    }
    
    let html = '';
    activities.forEach(activity => {
        html += `
            <div class="activity-item p-3 mb-3 rounded">
                <div class="d-flex align-items-start">
                    <div class="flex-shrink-0 me-3">
                        <i class="bi ${activity.icon} ${activity.iconColor} fs-4"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-medium">${escapeHtml(activity.description)}</div>
                        <small class="text-muted">${formatTimeAgo(activity.timestamp)}</small>
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Esporta dati utente
function exportUserData() {
    if (!confirm('Vuoi esportare tutti i tuoi dati?')) {
        return;
    }
    
    const userData = {
        profile: currentUser,
        preferences: userPreferences,
        exportDate: new Date().toISOString(),
        version: '1.0'
    };
    
    const blob = new Blob([JSON.stringify(userData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `user_data_export_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    showAlert('Dati esportati con successo!', 'success');
}

// Elimina account
function deleteAccount() {
    if (!confirm('ATTENZIONE: Questa azione eliminerà permanentemente il tuo account e tutti i tuoi dati. Sei sicuro di voler continuare?')) {
        return;
    }
    
    const confirmText = prompt('Per confermare, digita "ELIMINA" (tutto maiuscolo):');
    if (confirmText !== 'ELIMINA') {
        showAlert('Eliminazione annullata', 'info');
        return;
    }
    
    // Simulazione eliminazione account
    showAlert('Eliminazione account in corso... (simulazione)', 'warning');
    
    setTimeout(() => {
        localStorage.clear();
        showAlert('Account eliminato con successo', 'success');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
    }, 3000);
}

// Toggle della sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
}

// Utility functions
function getInitials(name) {
    if (!name) return 'U';
    const names = name.split(' ');
    if (names.length === 1) {
        return names[0].charAt(0).toUpperCase();
    }
    return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isStrongPassword(password) {
    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/;
    return strongPasswordRegex.test(password);
}

function formatTimeAgo(date) {
    const now = new Date();
    const diff = now - new Date(date);
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return 'Proprio ora';
    if (minutes < 60) return `${minutes} minuti fa`;
    if (hours < 24) return `${hours} ore fa`;
    if (days < 7) return `${days} giorni fa`;
    
    return new Date(date).toLocaleDateString('it-IT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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

// Logout
function logout() {
    if (window.authUtils && window.authUtils.logout) {
        window.authUtils.logout();
    } else {
        localStorage.clear();
        window.location.href = 'login.html';
    }
}
