// Configurazione autenticazione
const AUTH_API_BASE_URL = '/api/auth';
const REDIRECT_AFTER_LOGIN = 'index.html';

// Variabili globali per l'autenticazione
let currentUser = null;
let authToken = null;

// Inizializzazione della pagina di login
document.addEventListener('DOMContentLoaded', function() {
    // Controlla se l'utente è già autenticato
    checkExistingAuth();
    
    // Setup event listeners
    setupAuthEventListeners();
    
    // Gestione parametri URL per OAuth callback
    handleOAuthCallback();
});

// Setup degli event listeners per l'autenticazione
function setupAuthEventListeners() {
    // Form di login
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    
    // Form di registrazione (se presente)
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    // Form reset password (se presente)
    const forgotForm = document.getElementById('forgot-password-form');
    if (forgotForm) {
        forgotForm.addEventListener('submit', handleForgotPassword);
    }
}

// Gestione login con credenziali locali
async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('remember-me').checked;
    
    if (!username || !password) {
        showAlert('Inserisci username/email e password', 'warning');
        return;
    }
    
    try {
        showLoadingState(true);
        
        const response = await fetch(AUTH_API_BASE_URL + '/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password,
                rememberMe: rememberMe
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Login riuscito
            await handleSuccessfulLogin(data);
        } else {
            // Login fallito
            throw new Error(data.message || 'Credenziali non valide');
        }
        
    } catch (error) {
        console.error('Errore nel login:', error);
        showAlert(error.message || 'Errore durante il login', 'danger');
    } finally {
        showLoadingState(false);
    }
}

// Gestione login riuscito
async function handleSuccessfulLogin(loginData) {
    // Salva i dati di autenticazione
    authToken = loginData.token; // Corretto da accessToken a token
    
    // Crea oggetto utente dai dati della risposta
    currentUser = {
        username: loginData.username,
        email: loginData.email,
        accountType: loginData.accountType,
        tokenType: loginData.tokenType || 'Bearer'
    };
    
    // Salva nel localStorage
    localStorage.setItem('authToken', authToken);
    localStorage.setItem('currentUser', JSON.stringify(currentUser));
    
    // Salva refresh token se presente
    if (loginData.refreshToken) {
        localStorage.setItem('refreshToken', loginData.refreshToken);
    }
    
    showAlert('Login effettuato con successo!', 'success');
    
    // Redirect dopo un breve delay
    setTimeout(() => {
        window.location.href = REDIRECT_AFTER_LOGIN;
    }, 1000);
}

// Login con Google OAuth2
function loginWithGoogle() {
    showLoadingState(true);
    // Redirect all'endpoint OAuth2 di Google
    window.location.href = '/oauth2/authorization/google';
}

// Login con GitHub OAuth2
function loginWithGithub() {
    showLoadingState(true);
    // Redirect all'endpoint OAuth2 di GitHub
    window.location.href = '/oauth2/authorization/github';
}

// Gestione callback OAuth2
function handleOAuthCallback() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const error = urlParams.get('error');
    
    if (error) {
        showAlert('Errore durante l\'autenticazione OAuth: ' + error, 'danger');
        return;
    }
    
    if (token) {
        // OAuth login riuscito
        authToken = token;
        localStorage.setItem('authToken', authToken);
        
        // Recupera i dati dell'utente
        fetchUserProfile().then(() => {
            showAlert('Login OAuth effettuato con successo!', 'success');
            setTimeout(() => {
                window.location.href = REDIRECT_AFTER_LOGIN;
            }, 1000);
        }).catch(error => {
            console.error('Errore nel recupero del profilo utente:', error);
            showAlert('Errore nel recupero del profilo utente', 'danger');
        });
    }
}

// Recupera il profilo dell'utente autenticato
async function fetchUserProfile() {
    try {
        const response = await fetch(AUTH_API_BASE_URL + '/me', {
            headers: {
                'Authorization': 'Bearer ' + authToken
            }
        });
        
        if (response.ok) {
            currentUser = await response.json();
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            return currentUser;
        } else {
            throw new Error('Impossibile recuperare il profilo utente');
        }
    } catch (error) {
        console.error('Errore nel recupero del profilo:', error);
        throw error;
    }
}

// Gestione registrazione
async function handleRegister(event) {
    if (event) event.preventDefault();
    
    const email = document.getElementById('reg-email').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const firstName = document.getElementById('reg-firstname').value.trim();
    const lastName = document.getElementById('reg-lastname').value.trim();
    const password = document.getElementById('reg-password').value;
    const confirmPassword = document.getElementById('reg-confirm-password').value;
    const acceptTerms = document.getElementById('reg-terms').checked;
    
    // Validazioni
    if (!email || !username || !password) {
        showAlert('Email, username e password sono obbligatori', 'warning');
        return;
    }
    
    if (password !== confirmPassword) {
        showAlert('Le password non coincidono', 'warning');
        return;
    }
    
    if (password.length < 8) {
        showAlert('La password deve essere di almeno 8 caratteri', 'warning');
        return;
    }
    
    if (!acceptTerms) {
        showAlert('Devi accettare i termini e condizioni', 'warning');
        return;
    }
    
    try {
        showLoadingState(true);
        
        const response = await fetch(AUTH_API_BASE_URL + '/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                username: username,
                firstName: firstName,
                lastName: lastName,
                password: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showAlert('Registrazione completata! Puoi ora effettuare il login.', 'success');
            
            // Chiudi il modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
            if (modal) modal.hide();
            
            // Pre-compila il form di login
            document.getElementById('username').value = username;
            
        } else {
            throw new Error(data.message || 'Errore durante la registrazione');
        }
        
    } catch (error) {
        console.error('Errore nella registrazione:', error);
        showAlert(error.message || 'Errore durante la registrazione', 'danger');
    } finally {
        showLoadingState(false);
    }
}

// Gestione reset password
async function handleForgotPassword(event) {
    if (event) event.preventDefault();
    
    const email = document.getElementById('forgot-email').value.trim();
    
    if (!email) {
        showAlert('Inserisci la tua email', 'warning');
        return;
    }
    
    try {
        showLoadingState(true);
        
        const response = await fetch(AUTH_API_BASE_URL + '/forgot-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email
            })
        });
        
        if (response.ok) {
            showAlert('Istruzioni per il reset inviate alla tua email', 'success');
            
            // Chiudi il modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal'));
            if (modal) modal.hide();
            
        } else {
            const data = await response.json();
            throw new Error(data.message || 'Errore durante il reset password');
        }
        
    } catch (error) {
        console.error('Errore nel reset password:', error);
        showAlert(error.message || 'Errore durante il reset password', 'danger');
    } finally {
        showLoadingState(false);
    }
}

// Controlla se l'utente è già autenticato
function checkExistingAuth() {
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('currentUser');
    
    if (token && user) {
        // L'utente potrebbe essere già autenticato, verifica il token
        verifyToken(token).then(isValid => {
            if (isValid) {
                // Token valido, redirect alla home
                window.location.href = REDIRECT_AFTER_LOGIN;
            } else {
                // Token non valido, pulisci il localStorage
                clearAuthData();
            }
        });
    }
}

// Verifica validità del token
async function verifyToken(token) {
    try {
        const response = await fetch(AUTH_API_BASE_URL + '/me', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        return response.ok;
    } catch (error) {
        return false;
    }
}

// Pulisce i dati di autenticazione
function clearAuthData() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('refreshToken');
    authToken = null;
    currentUser = null;
}

// Logout
async function logout() {
    try {
        // Chiama l'endpoint di logout se disponibile
        if (authToken) {
            await fetch(AUTH_API_BASE_URL + '/logout', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + authToken
                }
            });
        }
    } catch (error) {
        console.error('Errore durante il logout:', error);
    } finally {
        // Pulisci sempre i dati locali
        clearAuthData();
        window.location.href = 'login.html';
    }
}

// Mostra/nascondi password
function togglePassword() {
    const passwordField = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle-icon');
    
    if (passwordField.type === 'password') {
        passwordField.type = 'text';
        toggleIcon.className = 'bi bi-eye-slash';
    } else {
        passwordField.type = 'password';
        toggleIcon.className = 'bi bi-eye';
    }
}

// Mostra modal registrazione
function showRegister() {
    const modal = new bootstrap.Modal(document.getElementById('registerModal'));
    modal.show();
}

// Mostra modal reset password
function showForgotPassword() {
    const modal = new bootstrap.Modal(document.getElementById('forgotPasswordModal'));
    modal.show();
}

// Stato di caricamento
function showLoadingState(isLoading) {
    const submitBtns = document.querySelectorAll('button[type="submit"], .btn-primary');
    
    submitBtns.forEach(btn => {
        if (isLoading) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Caricamento...';
        } else {
            btn.disabled = false;
            // Ripristina il testo originale (questo potrebbe essere migliorato)
            if (btn.id === 'login-btn' || btn.closest('#login-form')) {
                btn.innerHTML = '<i class="bi bi-box-arrow-in-right me-2"></i>Accedi';
            }
        }
    });
}

// Mostra alert
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

// Utility per le chiamate API autenticate
async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
        throw new Error('Utente non autenticato');
    }
    
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token,
        ...options.headers
    };
    
    const response = await fetch(url, {
        ...options,
        headers
    });
    
    // Se il token è scaduto, prova a rinnovarlo
    if (response.status === 401) {
        const refreshed = await tryRefreshToken();
        if (refreshed) {
            // Riprova la chiamata con il nuovo token
            headers['Authorization'] = 'Bearer ' + localStorage.getItem('authToken');
            return fetch(url, { ...options, headers });
        } else {
            // Refresh fallito, redirect al login
            clearAuthData();
            window.location.href = 'login.html';
            throw new Error('Sessione scaduta');
        }
    }
    
    return response;
}

// Prova a rinnovare il token usando il refresh token
async function tryRefreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (!refreshToken) {
        return false;
    }
    
    try {
        const response = await fetch(AUTH_API_BASE_URL + '/refresh', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                refreshToken: refreshToken
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('authToken', data.accessToken);
            if (data.refreshToken) {
                localStorage.setItem('refreshToken', data.refreshToken);
            }
            return true;
        }
        
        return false;
    } catch (error) {
        console.error('Errore nel refresh del token:', error);
        return false;
    }
}

// Esporta le funzioni utili per altri script
window.authUtils = {
    getCurrentUser: () => currentUser,
    getAuthToken: () => authToken,
    isAuthenticated: () => !!authToken,
    logout: logout,
    authenticatedFetch: authenticatedFetch
};
