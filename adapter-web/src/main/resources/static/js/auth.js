// Utilities per l'autenticazione - utilizzate da tutte le pagine dell'applicazione
const API_BASE_URL = '/api';
const AUTH_API_BASE_URL = '/api/auth';

// Oggetto globale per utilities di autenticazione
window.authUtils = {
    // Verifica se l'utente è autenticato
    isAuthenticated: function() {
        const token = localStorage.getItem('authToken');
        return token !== null && token !== '';
    },
    
    // Ottiene il token di autenticazione
    getToken: function() {
        return localStorage.getItem('authToken');
    },
    
    // Ottiene i dati dell'utente corrente
    getCurrentUser: function() {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    },
    
    // Salva i dati di autenticazione
    saveAuthData: function(loginData) {
        localStorage.setItem('authToken', loginData.token);
        
        const userData = {
            username: loginData.username,
            email: loginData.email,
            accountType: loginData.accountType,
            role: loginData.role,
            tokenType: loginData.tokenType || 'Bearer'
        };
        
        localStorage.setItem('currentUser', JSON.stringify(userData));
        
        if (loginData.refreshToken) {
            localStorage.setItem('refreshToken', loginData.refreshToken);
        }
    },
    
    // Salva solo il token (per OAuth)
    saveToken: function(token) {
        localStorage.setItem('authToken', token);
    },
    
    // Effettua il logout
    logout: function() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        localStorage.removeItem('refreshToken');
        
        // Redirect alla pagina di login
        window.location.href = '/login.html';
    },
    
    // Richiesta HTTP autenticata
    authenticatedFetch: async function(url, options = {}) {
        const token = this.getToken();
        
        if (!token) {
            throw new Error('Non autenticato');
        }
        
        const authOptions = {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': 'Bearer ' + token
            }
        };
        
        try {
            const response = await fetch(url, authOptions);
            
            // Se unauthorized, redirect al login
            if (response.status === 401) {
                this.logout();
                return;
            }
            
            return response;
        } catch (error) {
            console.error('Errore nella richiesta autenticata:', error);
            throw error;
        }
    },
    
    // Richiesta GET autenticata con parsing JSON
    get: async function(url) {
        const response = await this.authenticatedFetch(url);
        if (response && response.ok) {
            return await response.json();
        }
        throw new Error('Errore nella richiesta GET');
    },
    
    // Richiesta POST autenticata
    post: async function(url, data) {
        const response = await this.authenticatedFetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (response && response.ok) {
            return await response.json();
        }
        throw new Error('Errore nella richiesta POST');
    },
    
    // Richiesta PUT autenticata
    put: async function(url, data) {
        const response = await this.authenticatedFetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (response && response.ok) {
            return await response.json();
        }
        throw new Error('Errore nella richiesta PUT');
    },
    
    // Richiesta DELETE autenticata
    delete: async function(url) {
        const response = await this.authenticatedFetch(url, {
            method: 'DELETE'
        });
        
        if (response && response.ok) {
            return true;
        }
        throw new Error('Errore nella richiesta DELETE');
    },
    
    // Verifica validità del token
    validateToken: async function() {
        try {
            const response = await this.authenticatedFetch(AUTH_API_BASE_URL + '/validate');
            return response && response.ok;
        } catch (error) {
            return false;
        }
    },
    
    // Refresh del token se supportato
    refreshToken: async function() {
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
                this.saveAuthData(data);
                return true;
            }
        } catch (error) {
            console.error('Errore nel refresh del token:', error);
        }
        
        return false;
    }
};

// Auto-inizializzazione delle utility di autenticazione
document.addEventListener('DOMContentLoaded', function() {
    // Le utility sono sempre disponibili
    console.log('Auth utilities caricate');
    
    // Non facciamo redirect automatico qui, lasciamo che ogni pagina gestisca la propria logica
});
