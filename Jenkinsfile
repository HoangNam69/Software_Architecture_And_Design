pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build & Test') {
            steps {
                script {
                    // Kiểm tra Docker có tồn tại và có quyền truy cập
                    sh 'which docker || echo "Docker not found in PATH"'
                    sh 'docker --version || echo "Cannot execute Docker"'

                    // Hiển thị thông tin workspace hiện tại
                    sh 'pwd'
                    sh 'ls -la'

                    // Sử dụng Maven trực tiếp nếu có sẵn, thay vì dùng Docker
                    sh '''
                        if command -v mvn &> /dev/null; then
                            echo "Using system Maven"
                            mvn clean install package -DskipTests
                        else
                            echo "Maven not found, cannot proceed"
                            exit 1
                        fi
                    '''

                    sh '''
                        if command -v mvn &> /dev/null; then
                            echo "Running tests with system Maven"
                            mvn test
                        fi
                    '''
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

                    // Kiểm tra docker-compose
                    sh '''
                    if command -v docker-compose &> /dev/null; then
                        echo "docker-compose found"
                        docker-compose --version
                    elif command -v docker &> /dev/null && docker compose version &> /dev/null; then
                        echo "docker compose plugin found"
                        docker compose version
                    else
                        echo "Neither docker-compose nor docker compose plugin found"
                        exit 1
                    fi
                    '''

                    // Build và deploy với Docker Compose
                    sh '''
                    if command -v docker-compose &> /dev/null; then
                        docker-compose down || true
                        docker-compose build
                        docker-compose up -d
                    else
                        docker compose down || true
                        docker compose build
                        docker compose up -d
                    fi
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
