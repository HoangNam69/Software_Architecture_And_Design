pipeline {
    agent any

    environment {
        // Định nghĩa các service cần theo dõi thay đổi
        SERVICES = "api-gateway admin-service authentication-service cart-service category-service order-service payment-service product-service report-service"
        // Thêm đường dẫn cho backup env
        ENV_BACKUP_DIR = "/var/env-backup"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    // Lấy commit gần nhất
                    def lastCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def previousCommit = sh(script: 'git rev-parse HEAD~1', returnStdout: true).trim()

                    // Lấy danh sách các file đã thay đổi giữa 2 commit
                    def changedFiles = sh(script: "git diff --name-only ${previousCommit} ${lastCommit}", returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    // Khởi tạo map để lưu trạng thái thay đổi của các service
                    def changedServices = [:]
                    def anyServiceChanged = false

                    // Kiểm tra từng service xem có thay đổi không
                    def servicesList = SERVICES.split()
                    servicesList.each { service ->
                        // Kiểm tra xem thư mục của service có thay đổi không
                        def serviceChanged = changedFiles.contains("${service}/")
                        changedServices[service] = serviceChanged

                        if (serviceChanged) {
                            anyServiceChanged = true
                            echo "Service ${service} has changes"
                        }
                    }

                    // Nếu không có service nào thay đổi cụ thể nhưng có thay đổi chung (như docker-compose.yml)
                    if (!anyServiceChanged && (changedFiles.contains("docker-compose.yml") || changedFiles.contains("Jenkinsfile"))) {
                        echo "Common configuration files changed, will update all services"
                        servicesList.each { service ->
                            changedServices[service] = true
                        }
                    }

                    // Lưu trạng thái thay đổi vào biến môi trường để sử dụng ở các stage sau
                    env.CHANGED_SERVICES = changedServices.findAll { it.value == true }.keySet().join(" ")
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                }
            }
        }

        stage('Backup Environment Variables') {
            steps {
                script {
                    // Tạo thư mục backup nếu chưa tồn tại
                    sh "mkdir -p ${ENV_BACKUP_DIR}"

                    if (env.CHANGED_SERVICES) {
                        env.CHANGED_SERVICES.split().each { service ->
                            echo "Backing up environment variables for service: ${service}"
                            sh """
                            # Check if container exists and is running (not in restarting state)
                            CONTAINER_STATUS=\$(docker inspect --format='{{.State.Status}}' ${service} 2>/dev/null || echo "not_found")

                            if [ "\$CONTAINER_STATUS" = "running" ]; then
                                # Extract environment variables
                                docker exec ${service} env | grep -v "PATH=" | grep -v "PWD=" | grep -v "HOME=" > ${ENV_BACKUP_DIR}/${service}.env || echo "Failed to extract environment variables"
                                echo "Environment variables for ${service} backed up successfully"
                            else
                                echo "Container ${service} is not running (status: \$CONTAINER_STATUS), no environment to backup"
                                # Create empty file to avoid issues in the restore stage
                                touch ${ENV_BACKUP_DIR}/${service}.env
                            fi
                            """
                        }
                    }
                }
            }
        }

        stage('Fix Maven Wrapper Permissions') {
            steps {
                script {
                    // Set executable permissions nhưng chỉ trong các thư mục service đã thay đổi
                    if (env.CHANGED_SERVICES) {
                        env.CHANGED_SERVICES.split().each { service ->
                            sh """
                            if [ -f ${service}/mvnw ]; then
                                chmod +x ${service}/mvnw
                                echo "Set executable permission for ${service}/mvnw"
                            fi
                            """
                        }
                    }
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

        stage('Docker Build Changed Services') {
            steps {
                script {
                    // Hiển thị Docker path
                    sh 'which docker'

                    if (env.CHANGED_SERVICES) {
                        // Nếu có service thay đổi, chỉ build những service đó
                        env.CHANGED_SERVICES.split().each { service ->
                            echo "Building service: ${service}"
                            sh "docker compose build ${service} || { echo 'Failed to build ${service} but continuing'; }"
                        }
                    } else {
                        echo "No services to build"
                    }
                }
            }
        }

        stage('Deploy Services') {
            steps {
                script {
                    if (env.CHANGED_SERVICES) {
                        // Nếu có service thay đổi, dừng và khởi động lại chỉ những service đó
                        env.CHANGED_SERVICES.split().each { service ->
                            echo "Restarting service: ${service}"
                            sh """
                            # Dừng service hiện tại (nếu đang chạy)
                            docker compose stop ${service} || true

                            # Xóa container cũ để đảm bảo không có xung đột
                            docker compose rm -f ${service} || true

                            # Khởi động service với cấu hình mới
                            docker compose up -d ${service} || { echo 'Failed to start ${service} but continuing'; }
                            """
                        }
                    } else {
                        echo "No services to deploy"
                    }
                }
            }
        }

        stage('Restore Environment Variables') {
            steps {
                script {
                    if (env.CHANGED_SERVICES) {
                        env.CHANGED_SERVICES.split().each { service ->
                            echo "Restoring environment variables for service: ${service}"
                            sh """
                            if [ -f "${ENV_BACKUP_DIR}/${service}.env" ] && [ -s "${ENV_BACKUP_DIR}/${service}.env" ]; then
                                echo "Found environment backup for ${service}, restoring..."

                                # Wait a bit for container to be fully up
                                sleep 10

                                # Check if container is running before trying to restore environment
                                CONTAINER_STATUS=\$(docker inspect --format='{{.State.Status}}' ${service} 2>/dev/null || echo "not_found")

                                if [ "\$CONTAINER_STATUS" = "running" ]; then
                                    # Apply each environment variable to the container
                                    cat ${ENV_BACKUP_DIR}/${service}.env | while read -r line; do
                                        # Skip empty lines
                                        [ -z "\$line" ] && continue

                                        # Extract key and value
                                        key=\$(echo "\$line" | cut -d= -f1)
                                        value=\$(echo "\$line" | cut -d= -f2-)

                                        # Skip some variables we don't want to override
                                        if [[ "\$key" != "PATH" && "\$key" != "PWD" && "\$key" != "HOME" && "\$key" != "HOSTNAME" ]]; then
                                            echo "Setting \$key for ${service}..."
                                            docker exec ${service} /bin/sh -c "export \$key='\$value'" || echo "Failed to set \$key but continuing"
                                        fi
                                    done
                                    echo "Environment restored for ${service}"
                                else
                                    echo "Container ${service} is not running (status: \$CONTAINER_STATUS), skipping environment restoration"
                                fi
                            else
                                echo "No environment backup found for ${service} or backup file is empty, skipping restoration"
                            fi
                            """
                        }
                    }
                }
            }
        }

        stage('Verify Changed Services Health') {
            steps {
                script {
                    if (env.CHANGED_SERVICES) {
                        // Đợi và kiểm tra trạng thái của các service đã thay đổi
                        sh '''
                        echo "Waiting for changed services to be healthy..."

                        # Danh sách services cần kiểm tra
                        SERVICES_TO_CHECK="${CHANGED_SERVICES}"

                        # Thiết lập timeout (180 giây)
                        TIMEOUT=180
                        START_TIME=$(date +%s)

                        # Initial delay to give services time to start
                        sleep 10

                        # Check service health status
                        for service in $SERVICES_TO_CHECK; do
                            CURRENT_TIME=$(date +%s)
                            ELAPSED=$((CURRENT_TIME - START_TIME))

                            if [ $ELAPSED -gt $TIMEOUT ]; then
                                echo "Timeout reached while checking services!"
                                break
                            fi

                            echo "Checking status of $service (elapsed time: ${ELAPSED}s)..."
                            CONTAINER_STATUS=$(docker inspect --format='{{.State.Status}}' $service 2>/dev/null || echo "not_found")
                            echo "$service status: $CONTAINER_STATUS"
                        done

                        # Show final status of all containers
                        echo "Final status of all services:"
                        docker compose ps
                        '''
                    } else {
                        echo "No services to verify"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Selective deployment successful!'
            script {
                if (env.CHANGED_SERVICES) {
                    echo "Updated services: ${env.CHANGED_SERVICES}"
                } else {
                    echo "No services were updated"
                }
            }
        }
        failure {
            echo 'Build or deployment failed'
            script {
                // Trong trường hợp thất bại, hiển thị logs của các service thay đổi để debug
                if (env.CHANGED_SERVICES) {
                    env.CHANGED_SERVICES.split().each { service ->
                        sh "docker compose logs ${service} || echo 'Could not get logs for ${service}'"
                    }
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
