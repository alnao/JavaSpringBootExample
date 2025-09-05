// Configurazione tasks
const TASKS_API_BASE_URL = '/api/tasks';
let allTasks = [];

// Inizializzazione della pagina tasks
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    checkAuthentication();
    
    // Carica i dati dell'utente
    loadUserData();
    
    // Carica dashboard tasks
    loadTasksDashboard();
    
    // Setup event listeners
    setupTasksEventListeners();
    
    // Gestisci hash URL per navigazione diretta
    handleUrlHash();
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
            currentUser = userData;
            
            const userName = userData.firstName ? 
                `${userData.firstName} ${userData.lastName || ''}`.trim() : 
                userData.username || userData.email;
            
            document.getElementById('user-name').textContent = userName;
        } catch (error) {
            console.error('Errore nel parsing dei dati utente:', error);
        }
    }
}

// Setup event listeners
function setupTasksEventListeners() {
    // Form di creazione task
    const createForm = document.getElementById('create-task-form');
    if (createForm) {
        // Event listeners per i filtri
        const searchInput = document.getElementById('search-tasks');
        if (searchInput) {
            searchInput.addEventListener('input', filterTasks);
        }
        
        const statusFilter = document.getElementById('filter-status');
        if (statusFilter) {
            statusFilter.addEventListener('change', filterTasks);
        }
        
        const priorityFilter = document.getElementById('filter-priority');
        if (priorityFilter) {
            priorityFilter.addEventListener('change', filterTasks);
        }
        
        const categoryFilter = document.getElementById('filter-category');
        if (categoryFilter) {
            categoryFilter.addEventListener('change', filterTasks);
        }
        
        const sortSelect = document.getElementById('sort-tasks');
        if (sortSelect) {
            sortSelect.addEventListener('change', sortTasks);
        }
    }
}

// Gestione hash URL
function handleUrlHash() {
    const hash = window.location.hash.substring(1);
    if (hash) {
        switch(hash) {
            case 'create':
                showSection('crea');
                break;
            case 'completed':
                showSection('completati');
                break;
            case 'due':
                showSection('scadenza');
                break;
            default:
                showSection('tasks');
        }
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
    
    // Mostra la sezione selezionata
    const targetSection = document.getElementById(sectionName + '-section');
    if (targetSection) {
        targetSection.style.display = 'block';
    }
    
    // Carica i dati specifici della sezione
    switch(sectionName) {
        case 'dashboard':
            loadTasksDashboard();
            break;
        case 'tasks':
            loadAllTasks();
            break;
        case 'completati':
            loadCompletedTasks();
            break;
        case 'scadenza':
            loadDueTasks();
            break;
        case 'crea':
            resetTaskForm();
            break;
    }
}

// Carica dashboard tasks
async function loadTasksDashboard() {
    try {
        // Per ora usiamo dati mock dato che l'API non è ancora implementata
        const mockStats = generateMockTaskStats();
        updateTaskStatistics(mockStats);
        
        // Carica tasks urgenti
        const urgentTasks = generateMockUrgentTasks();
        displayUrgentTasks(urgentTasks);
        
    } catch (error) {
        console.error('Errore nel caricamento della dashboard tasks:', error);
        showAlert('Errore nel caricamento della dashboard', 'danger');
    }
}

// Genera statistiche mock per i tasks
function generateMockTaskStats() {
    return {
        total: 12,
        pending: 4,
        inProgress: 3,
        completed: 5
    };
}

// Genera tasks urgenti mock
function generateMockUrgentTasks() {
    return [
        {
            id: '1',
            title: 'Preparare presentazione cliente',
            description: 'Finalizzare le slides per il meeting di venerdì',
            dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // Domani
            priority: 'high',
            status: 'pending'
        },
        {
            id: '2',
            title: 'Revisione codice PR #123',
            description: 'Controllare le modifiche al sistema di autenticazione',
            dueDate: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000), // Dopodomani
            priority: 'medium',
            status: 'pending'
        }
    ];
}

// Aggiorna statistiche nell'interfaccia
function updateTaskStatistics(stats) {
    document.getElementById('total-tasks').textContent = stats.total;
    document.getElementById('pending-tasks').textContent = stats.pending;
    document.getElementById('in-progress-tasks').textContent = stats.inProgress;
    document.getElementById('completed-tasks').textContent = stats.completed;
}

// Mostra tasks urgenti
function displayUrgentTasks(tasks) {
    const container = document.getElementById('urgent-tasks');
    
    if (!tasks || tasks.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-check-circle text-success display-1"></i>
                <h4 class="text-success mt-3">Ottimo lavoro!</h4>
                <p class="text-muted">Non hai tasks in scadenza immediata</p>
            </div>
        `;
        return;
    }
    
    let html = '';
    tasks.forEach(task => {
        const priorityClass = getPriorityClass(task.priority);
        const daysLeft = Math.ceil((task.dueDate - new Date()) / (1000 * 60 * 60 * 24));
        
        html += `
            <div class="card task-card ${task.status} ${priorityClass} mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <h6 class="card-title mb-2">${escapeHtml(task.title)}</h6>
                            <p class="card-text text-muted mb-2">${escapeHtml(task.description)}</p>
                            <div class="d-flex align-items-center">
                                <span class="badge ${getPriorityBadgeClass(task.priority)} me-2">${getPriorityText(task.priority)}</span>
                                <small class="text-muted">
                                    <i class="bi bi-clock"></i> 
                                    ${daysLeft === 0 ? 'Oggi' : daysLeft === 1 ? 'Domani' : `${daysLeft} giorni`}
                                </small>
                            </div>
                        </div>
                        <div class="flex-shrink-0 ms-3">
                            <button class="btn btn-sm btn-outline-primary" onclick="editTask('${task.id}')">
                                <i class="bi bi-pencil"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Carica tutti i tasks
async function loadAllTasks() {
    try {
        // Per ora usiamo dati mock
        allTasks = generateMockTasks();
        displayTasks(allTasks);
        populateTaskFilters(allTasks);
        
    } catch (error) {
        console.error('Errore nel caricamento dei tasks:', error);
        showAlert('Errore nel caricamento dei tasks', 'danger');
    }
}

// Genera tasks mock
function generateMockTasks() {
    return [
        {
            id: '1',
            title: 'Preparare presentazione cliente',
            description: 'Finalizzare le slides per il meeting di venerdì',
            dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000),
            priority: 'high',
            status: 'pending',
            category: 'Lavoro',
            tags: ['urgente', 'cliente'],
            estimatedHours: 4,
            createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
        },
        {
            id: '2',
            title: 'Revisione codice PR #123',
            description: 'Controllare le modifiche al sistema di autenticazione',
            dueDate: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000),
            priority: 'medium',
            status: 'in-progress',
            category: 'Sviluppo',
            tags: ['code-review', 'security'],
            estimatedHours: 2,
            createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
        },
        {
            id: '3',
            title: 'Aggiornare documentazione API',
            description: 'Documentare i nuovi endpoint per l\'autenticazione',
            dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
            priority: 'low',
            status: 'pending',
            category: 'Documentazione',
            tags: ['api', 'docs'],
            estimatedHours: 3,
            createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
        },
        {
            id: '4',
            title: 'Setup ambiente di testing',
            description: 'Configurare Docker e CI/CD per i test automatici',
            dueDate: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
            priority: 'high',
            status: 'completed',
            category: 'DevOps',
            tags: ['docker', 'testing', 'ci-cd'],
            estimatedHours: 6,
            createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000),
            completedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
        }
    ];
}

// Visualizza i tasks
function displayTasks(tasks, container = document.getElementById('tasks-container')) {
    if (!tasks || tasks.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-5">
                    <i class="bi bi-list-task display-1 text-muted"></i>
                    <h3 class="text-muted mt-3">Nessun task trovato</h3>
                    <p class="text-muted">Inizia creando il tuo primo task!</p>
                    <button class="btn btn-primary" onclick="showSection('crea')">
                        <i class="bi bi-plus"></i> Crea Task
                    </button>
                </div>
            </div>
        `;
        return;
    }
    
    let html = '';
    tasks.forEach(task => {
        const priorityClass = getPriorityClass(task.priority);
        const statusClass = getStatusClass(task.status);
        const isOverdue = task.dueDate && new Date() > task.dueDate && task.status !== 'completed';
        
        html += `
            <div class="col-lg-6 col-xl-4 mb-4">
                <div class="card task-card ${statusClass} ${priorityClass} h-100 ${isOverdue ? 'border-danger' : ''}">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">${escapeHtml(task.title)}</h6>
                        <div class="dropdown">
                            <button class="btn btn-sm btn-outline-secondary" data-bs-toggle="dropdown">
                                <i class="bi bi-three-dots-vertical"></i>
                            </button>
                            <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="#" onclick="editTask('${task.id}')">
                                    <i class="bi bi-pencil"></i> Modifica
                                </a></li>
                                <li><a class="dropdown-item" href="#" onclick="toggleTaskStatus('${task.id}')">
                                    <i class="bi bi-check-circle"></i> ${task.status === 'completed' ? 'Riapri' : 'Completa'}
                                </a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item text-danger" href="#" onclick="deleteTask('${task.id}')">
                                    <i class="bi bi-trash"></i> Elimina
                                </a></li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="card-body">
                        <p class="card-text mb-3">${escapeHtml(task.description)}</p>
                        
                        <div class="mb-3">
                            <span class="badge ${getPriorityBadgeClass(task.priority)} me-1">${getPriorityText(task.priority)}</span>
                            <span class="badge ${getStatusBadgeClass(task.status)} me-1">${getStatusText(task.status)}</span>
                            ${task.category ? `<span class="badge bg-secondary me-1">${escapeHtml(task.category)}</span>` : ''}
                        </div>
                        
                        ${task.tags && task.tags.length > 0 ? `
                            <div class="mb-3">
                                ${task.tags.map(tag => `<span class="badge bg-outline-secondary me-1">${escapeHtml(tag)}</span>`).join('')}
                            </div>
                        ` : ''}
                        
                        <div class="text-muted small">
                            ${task.dueDate ? `
                                <div class="mb-1 ${isOverdue ? 'text-danger' : ''}">
                                    <i class="bi bi-calendar"></i> 
                                    Scadenza: ${formatDate(task.dueDate)}
                                    ${isOverdue ? '<i class="bi bi-exclamation-triangle text-danger ms-1"></i>' : ''}
                                </div>
                            ` : ''}
                            ${task.estimatedHours ? `
                                <div class="mb-1">
                                    <i class="bi bi-clock"></i> 
                                    Stima: ${task.estimatedHours}h
                                </div>
                            ` : ''}
                            <div>
                                <i class="bi bi-plus-circle"></i> 
                                Creato: ${formatDate(task.createdAt)}
                            </div>
                            ${task.completedAt ? `
                                <div class="text-success">
                                    <i class="bi bi-check-circle"></i> 
                                    Completato: ${formatDate(task.completedAt)}
                                </div>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Popola i filtri
function populateTaskFilters(tasks) {
    const categoryFilter = document.getElementById('filter-category');
    if (categoryFilter) {
        const categories = new Set(tasks.map(task => task.category).filter(cat => cat));
        
        categoryFilter.innerHTML = '<option value="">Tutte le categorie</option>';
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            categoryFilter.appendChild(option);
        });
    }
}

// Filtra i tasks
function filterTasks() {
    const searchText = document.getElementById('search-tasks')?.value?.toLowerCase() || '';
    const statusFilter = document.getElementById('filter-status')?.value || '';
    const priorityFilter = document.getElementById('filter-priority')?.value || '';
    const categoryFilter = document.getElementById('filter-category')?.value || '';
    
    const filtered = allTasks.filter(task => {
        const matchesSearch = !searchText || 
            task.title.toLowerCase().includes(searchText) ||
            task.description.toLowerCase().includes(searchText);
        
        const matchesStatus = !statusFilter || task.status === statusFilter;
        const matchesPriority = !priorityFilter || task.priority === priorityFilter;
        const matchesCategory = !categoryFilter || task.category === categoryFilter;
        
        return matchesSearch && matchesStatus && matchesPriority && matchesCategory;
    });
    
    displayTasks(filtered);
}

// Ordina i tasks
function sortTasks() {
    const sortBy = document.getElementById('sort-tasks')?.value || 'dueDate';
    
    const sorted = [...allTasks].sort((a, b) => {
        switch(sortBy) {
            case 'dueDate':
                return (a.dueDate || new Date('2099-12-31')) - (b.dueDate || new Date('2099-12-31'));
            case 'priority':
                const priorityOrder = { 'high': 3, 'medium': 2, 'low': 1 };
                return priorityOrder[b.priority] - priorityOrder[a.priority];
            case 'created':
                return b.createdAt - a.createdAt;
            case 'title':
                return a.title.localeCompare(b.title);
            default:
                return 0;
        }
    });
    
    displayTasks(sorted);
}

// Crea nuovo task
function createTask() {
    const title = document.getElementById('task-title').value.trim();
    const description = document.getElementById('task-description').value.trim();
    const priority = document.getElementById('task-priority').value;
    const category = document.getElementById('task-category').value.trim();
    const dueDate = document.getElementById('task-due-date').value;
    const estimatedHours = document.getElementById('task-estimated-hours').value;
    const tags = document.getElementById('task-tags').value.trim();
    const notifications = document.getElementById('task-notifications').checked;
    
    if (!title) {
        showAlert('Il titolo del task è obbligatorio', 'warning');
        return;
    }
    
    // Simulazione creazione task
    const newTask = {
        id: Date.now().toString(),
        title: title,
        description: description,
        priority: priority,
        category: category || null,
        dueDate: dueDate ? new Date(dueDate) : null,
        estimatedHours: estimatedHours ? parseFloat(estimatedHours) : null,
        tags: tags ? tags.split(',').map(tag => tag.trim()).filter(tag => tag) : [],
        status: 'pending',
        createdAt: new Date(),
        notifications: notifications
    };
    
    // Aggiungi alla lista mock
    allTasks.unshift(newTask);
    
    showAlert('Task creato con successo!', 'success');
    resetTaskForm();
    showSection('tasks');
}

// Reset form task
function resetTaskForm() {
    const form = document.getElementById('create-task-form');
    if (form) {
        form.reset();
        document.getElementById('task-priority').value = 'medium';
    }
}

// Carica tasks completati
function loadCompletedTasks() {
    const completedTasks = allTasks.filter(task => task.status === 'completed');
    const container = document.getElementById('completed-tasks-container');
    displayTasks(completedTasks, container);
}

// Carica tasks in scadenza
function loadDueTasks() {
    const now = new Date();
    const threeDaysFromNow = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
    
    const dueTasks = allTasks.filter(task => 
        task.dueDate && 
        task.dueDate <= threeDaysFromNow && 
        task.status !== 'completed'
    );
    
    const container = document.getElementById('due-tasks-container');
    displayTasks(dueTasks, container);
}

// Utility functions
function getPriorityClass(priority) {
    switch(priority) {
        case 'high': return 'priority-high';
        case 'medium': return 'priority-medium';
        case 'low': return 'priority-low';
        default: return '';
    }
}

function getPriorityBadgeClass(priority) {
    switch(priority) {
        case 'high': return 'bg-danger';
        case 'medium': return 'bg-warning text-dark';
        case 'low': return 'bg-success';
        default: return 'bg-secondary';
    }
}

function getPriorityText(priority) {
    switch(priority) {
        case 'high': return 'Alta';
        case 'medium': return 'Media';
        case 'low': return 'Bassa';
        default: return 'Non definita';
    }
}

function getStatusClass(status) {
    switch(status) {
        case 'completed': return 'completed';
        case 'in-progress': return 'in-progress';
        case 'pending': return 'pending';
        default: return '';
    }
}

function getStatusBadgeClass(status) {
    switch(status) {
        case 'completed': return 'bg-success';
        case 'in-progress': return 'bg-info';
        case 'pending': return 'bg-warning text-dark';
        default: return 'bg-secondary';
    }
}

function getStatusText(status) {
    switch(status) {
        case 'completed': return 'Completato';
        case 'in-progress': return 'In Corso';
        case 'pending': return 'In Attesa';
        default: return 'Sconosciuto';
    }
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('it-IT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
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

// Funzioni placeholder per completare l'implementazione
function editTask(id) {
    showAlert('Funzione modifica task in sviluppo', 'info');
}

function toggleTaskStatus(id) {
    const task = allTasks.find(t => t.id === id);
    if (task) {
        if (task.status === 'completed') {
            task.status = 'pending';
            delete task.completedAt;
        } else {
            task.status = 'completed';
            task.completedAt = new Date();
        }
        
        // Ricarica la visualizzazione corrente
        const currentSection = document.querySelector('.content-section[style="display: block;"]');
        if (currentSection) {
            const sectionId = currentSection.id.replace('-section', '');
            showSection(sectionId);
        }
        
        showAlert(`Task ${task.status === 'completed' ? 'completato' : 'riaperto'}!`, 'success');
    }
}

function deleteTask(id) {
    if (confirm('Sei sicuro di voler eliminare questo task?')) {
        allTasks = allTasks.filter(t => t.id !== id);
        
        // Ricarica la visualizzazione corrente
        const currentSection = document.querySelector('.content-section[style="display: block;"]');
        if (currentSection) {
            const sectionId = currentSection.id.replace('-section', '');
            showSection(sectionId);
        }
        
        showAlert('Task eliminato!', 'success');
    }
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
