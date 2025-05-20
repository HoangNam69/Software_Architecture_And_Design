pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Build & Test') {
            steps {
                script {
                    // Kiểm tra Docker có tồn tại và có quyền truy cập
                    sh 'which docker || echo "Docker not found in PATH"'
                    sh 'docker --version || echo "Cannot execute Docker"'

                    // Hiển thị thông tin workspace hiện tại
                    sh 'pwd'
                    sh 'ls -la'
                }
            }
        }

        stage('Update Ports Configuration') {
            steps {
                script {
                    // Cập nhật port trong docker-compose.yml - sử dụng port 45678
                    sh '''
                    sed -i 's/- "8080:8080"/- "45678:8080"/g' docker-compose.yml
                    cat docker-compose.yml | grep -A 1 api-gateway
                    '''
                }
            }
        }

        stage('Docker Build & Deploy') {
            steps {
                script {
                    // Hiển thị Docker path
                    sh 'which docker'
                    sh 'docker ps -a'

                    // Always use docker compose plugin format (with space)
                    sh '''
                    echo "Using docker compose plugin"
                    docker compose version

                    # Stop any running containers
                    docker compose down || true

                    # Build and start the services
                    docker compose build
                    docker compose up -d
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful! Application is running on port 45678'
        }
        failure {
            echo 'Build or deployment failed'
        }
        always {
            cleanWs()
        }
    }
}
