#!/bin/bash

# Build and run script for Guess Game Java application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Build the application
build_app() {
    print_status "Building Java application..."
    mvn clean package -DskipTests
    print_status "Application built successfully!"
}

# Build Docker image
build_docker() {
    print_status "Building Docker image..."
    docker build -t guessgame:latest .
    print_status "Docker image built successfully!"
}

# Run with Docker Compose
run_compose() {
    print_status "Starting application with Docker Compose..."
    docker-compose up --build -d
    
    print_status "Waiting for services to be ready..."
    sleep 30
    
    print_status "Application is starting up..."
    print_status "MongoDB Admin UI: http://localhost:8081 (admin/admin123)"
    print_status "Guess Game: http://localhost:8080"
    
    print_status "To view logs: docker-compose logs -f"
    print_status "To stop: docker-compose down"
}

# Deploy to Kubernetes
deploy_k8s() {
    print_status "Deploying to Kubernetes..."
    
    # Create namespace
    kubectl create namespace guessgame --dry-run=client -o yaml | kubectl apply -f -
    
    # Apply MongoDB
    kubectl apply -f k8s/mongodb.yaml
    
    # Wait for MongoDB
    print_status "Waiting for MongoDB to be ready..."
    kubectl wait --for=condition=ready pod -l app=mongodb -n guessgame --timeout=300s
    
    # Apply application
    kubectl apply -f k8s/deployment.yaml
    
    # Wait for application
    print_status "Waiting for application to be ready..."
    kubectl wait --for=condition=ready pod -l app=guessgame-app -n guessgame --timeout=300s
    
    print_status "Application deployed successfully!"
    print_status "To access the application, add this to your /etc/hosts:"
    print_status "127.0.0.1 guessgame.local"
    print_status "Then visit: http://guessgame.local"
}

# Deploy with Helm
deploy_helm() {
    print_status "Deploying with Helm..."
    
    # Add bitnami repo for MongoDB
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update
    
    # Install/upgrade the chart
    helm upgrade --install guessgame k8s/helm/ \
        --namespace guessgame \
        --create-namespace \
        --wait
    
    print_status "Application deployed with Helm successfully!"
    print_status "To access the application, add this to your /etc/hosts:"
    print_status "127.0.0.1 guessgame.local"
    print_status "Then visit: http://guessgame.local"
}

# Clean up
cleanup() {
    print_status "Cleaning up..."
    
    case "$1" in
        docker)
            docker-compose down -v
            docker rmi guessgame:latest 2>/dev/null || true
            ;;
        k8s)
            kubectl delete namespace guessgame --ignore-not-found=true
            ;;
        helm)
            helm uninstall guessgame -n guessgame 2>/dev/null || true
            kubectl delete namespace guessgame --ignore-not-found=true
            ;;
        all)
            docker-compose down -v 2>/dev/null || true
            docker rmi guessgame:latest 2>/dev/null || true
            helm uninstall guessgame -n guessgame 2>/dev/null || true
            kubectl delete namespace guessgame --ignore-not-found=true 2>/dev/null || true
            ;;
    esac
    
    print_status "Cleanup completed!"
}

# Show help
show_help() {
    echo "Guess Game Build and Deploy Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  build         Build the Java application only"
    echo "  docker        Build and run with Docker Compose"
    echo "  k8s           Deploy to Kubernetes"
    echo "  helm          Deploy with Helm"
    echo "  cleanup       Clean up resources"
    echo "    docker        Clean up Docker resources"
    echo "    k8s           Clean up Kubernetes resources"
    echo "    helm          Clean up Helm resources"
    echo "    all           Clean up all resources"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build                 # Build the application"
    echo "  $0 docker                # Run with Docker Compose"
    echo "  $0 k8s                   # Deploy to Kubernetes"
    echo "  $0 helm                  # Deploy with Helm"
    echo "  $0 cleanup docker        # Clean up Docker resources"
    echo "  $0 cleanup all           # Clean up everything"
}

# Main script
main() {
    case "${1:-help}" in
        build)
            build_app
            ;;
        docker)
            check_docker
            build_app
            build_docker
            run_compose
            ;;
        k8s)
            build_app
            build_docker
            deploy_k8s
            ;;
        helm)
            build_app
            build_docker
            deploy_helm
            ;;
        cleanup)
            cleanup "${2:-all}"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
