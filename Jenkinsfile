pipeline {
    agent any

    environment {
        // Định nghĩa các service cần theo dõi thay đổi
        SERVICES = "api-gateway admin-service authentication-service cart-service category-service order-service payment-service product-service report-service"
        // Thêm đường dẫn cho backup env và temporary env files
        ENV_BACKUP_DIR = "/var/env-backup"
        ENV_CONFIG_DIR = "/var/env-config"  // Changed from /var/jenkins_home/env-config
        // Thêm flag để theo dõi lần build đầu tiên
        IS_FIRST_BUILD = "false"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Environment Setup') {
            steps {
                script {
                    // Đảm bảo thư mục backup env và env config tồn tại
                    sh "mkdir -p ${ENV_BACKUP_DIR}"
                    sh "mkdir -p ${ENV_CONFIG_DIR}"

                    // Kiểm tra xem volume env-config đã tồn tại chưa
                    def volumeExists = sh(script: "docker volume ls | grep env-config || echo 'NOT_FOUND'", returnStdout: true).trim()
                    if (volumeExists == 'NOT_FOUND' || !volumeExists.contains("env-config")) {
                        echo "Creating env-config volume..."
                        sh "docker volume create env-config"
                    }

                    // Kiểm tra xem network microservices-network đã tồn tại chưa
                    def networkExists = sh(script: "docker network ls | grep microservices-network || echo 'NOT_FOUND'", returnStdout: true).trim()
                    if (networkExists == 'NOT_FOUND' || !networkExists.contains("microservices-network")) {
                        echo "Creating microservices-network..."
                        sh "docker network create microservices-network || true"
                    }

                    // Đảm bảo tệp env cho các service tồn tại
                    def servicesList = SERVICES.split()
                    servicesList.each { service ->
                        sh """
                        if [ ! -f "${ENV_CONFIG_DIR}/${service}.env" ]; then
                            echo "Creating empty env file for ${service}..."
                            touch "${ENV_CONFIG_DIR}/${service}.env"
                        fi
                        """
                    }

                    // Copy env files from config dir to Docker volume
                    sh """
                    docker run --rm -v env-config:/data -v ${ENV_CONFIG_DIR}:/source alpine sh -c '
                        mkdir -p /data
                        cp -f /source/*.env /data/ || true
                        chmod 666 /data/*.env || true
                    '
                    """
                }
            }
        }

        stage('Detect Changed and Missing Services') {
            steps {
                script {
                    // Khởi tạo biến để kiểm tra xem dự án có phải lần đầu khởi tạo không
                    def isFirstTimeInit = false

                    // Kiểm tra xem có bất kỳ service nào đã được tạo chưa
                    def existingServices = sh(script: "docker compose ps -a --format '{{.Name}}' || echo 'NO_SERVICES'", returnStdout: true).trim()
                    echo "Existing services: ${existingServices}"

                    // Nếu không tìm thấy bất kỳ service nào, đây có thể là lần đầu khởi tạo
                    if (existingServices == 'NO_SERVICES' || existingServices.isEmpty()) {
                        echo "No services found. This appears to be the first time initialization."
                        isFirstTimeInit = true
                    } else {
                        // Đếm số lượng service hiện có và so sánh với số lượng service trong định nghĩa
                        def serviceCount = existingServices.split('\n').size()
                        def definedServicesList = SERVICES.split()

                        if (serviceCount < definedServicesList.size()) {
                            echo "Found ${serviceCount} services, but ${definedServicesList.size()} are defined. This might be a partial initialization."
                            isFirstTimeInit = true
                        }
                    }

                    // Nếu là lần đầu khởi tạo, build tất cả các service
                    if (isFirstTimeInit) {
                        echo "First time initialization detected. Will build all services."
                        env.CHANGED_SERVICES = SERVICES
                        env.IS_FIRST_BUILD = "true"
                        return
                    }

                    // Nếu không phải lần đầu khởi tạo, tiếp tục với logic phát hiện thay đổi như trước
                    try {
                        // Kiểm tra xem có commit trước đó không
                        def hasAnyCommit = sh(script: 'git rev-parse --verify HEAD', returnStatus: true) == 0

                        if (!hasAnyCommit) {
                            echo "No previous commits found. Will build all services."
                            env.CHANGED_SERVICES = SERVICES
                            env.IS_FIRST_BUILD = "true"
                            return
                        }

                        // Lấy commit gần nhất
                        def lastCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()

                        // Kiểm tra xem có commit trước đó không
                        def hasPreviousCommit = sh(script: 'git rev-parse HEAD~1', returnStatus: true) == 0

                        // Nếu không có commit trước đó, build tất cả
                        if (!hasPreviousCommit) {
                            echo "This is the first commit. Will build all services."
                            env.CHANGED_SERVICES = SERVICES
                            env.IS_FIRST_BUILD = "true"
                            return
                        }

                        def previousCommit = sh(script: 'git rev-parse HEAD~1', returnStdout: true).trim()

                        // Lấy danh sách các file đã thay đổi giữa 2 commit
                        def changedFiles = sh(script: "git diff --name-only ${previousCommit} ${lastCommit}", returnStdout: true).trim()
                        echo "Changed files: ${changedFiles}"

                        // Khởi tạo map để lưu trạng thái thay đổi của các service
                        def changedServices = [:]
                        def anyServiceChanged = false
                        def servicesList = SERVICES.split()

                        // Kiểm tra từng service xem có thay đổi không
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

                        // Kiểm tra các service nào chưa được build (không tồn tại container)
                        servicesList.each { service ->
                            def containerExists = sh(script: "docker ps -a | grep ${service} || echo 'NOT_FOUND'", returnStdout: true).trim()

                            if (containerExists == 'NOT_FOUND' || !containerExists.contains(service)) {
                                echo "Service ${service} does not exist, will build it"
                                changedServices[service] = true
                            } else {
                                // Kiểm tra xem container có đang chạy không
                                def containerRunning = sh(script: "docker ps | grep ${service} || echo 'NOT_RUNNING'", returnStdout: true).trim()
                                if (containerRunning == 'NOT_RUNNING' || !containerRunning.contains(service)) {
                                    echo "Service ${service} exists but is not running, will build it"
                                    changedServices[service] = true
                                }
                            }
                        }

                        // Lưu trạng thái thay đổi vào biến môi trường để sử dụng ở các stage sau
                        env.CHANGED_SERVICES = changedServices.findAll { it.value == true }.keySet().join(" ")
                        echo "Services to build: ${env.CHANGED_SERVICES}"

                        // Nếu không có service nào cần build, gán lại là tất cả để đảm bảo các service thiếu sẽ được xây dựng
                        if (!env.CHANGED_SERVICES) {
                            echo "No services to build based on changes. Will build all services."
                            env.CHANGED_SERVICES = SERVICES
                        }
                    } catch (Exception e) {
                        echo "Error during service detection: ${e.getMessage()}"
                        echo "As a precaution, will build all services."
                        env.CHANGED_SERVICES = SERVICES
                        env.IS_FIRST_BUILD = "true"
                    }
                }
            }
        }

        stage('Backup Environment Variables') {
            when {
                expression { env.IS_FIRST_BUILD != "true" }
            }
            steps {
                script {
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
                        // Kiểm tra xem có phải build đầu tiên không
                        if (env.IS_FIRST_BUILD == "true") {
                            echo "First time build detected. Building with --no-cache to ensure fresh builds."
                            env.CHANGED_SERVICES.split().each { service ->
                                echo "Building service: ${service}"
                                sh "docker compose build --no-cache ${service} || { echo 'Failed to build ${service} but continuing'; }"
                            }
                        } else {
                            // Nếu không phải build đầu tiên, build bình thường
                            env.CHANGED_SERVICES.split().each { service ->
                                echo "Building service: ${service}"
                                sh "docker compose build ${service} || { echo 'Failed to build ${service} but continuing'; }"
                            }
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
                        // Nếu là lần build đầu tiên, hãy dừng tất cả các container hiện có trước
                        if (env.IS_FIRST_BUILD == "true") {
                            echo "First time deployment. Stopping all existing services before redeploying..."
                            sh "docker compose down || true"
                        }

                        // Khởi động lại các service đã thay đổi
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

                        // Nếu là lần build đầu tiên, kiểm tra xem tất cả các service đã được khởi động chưa
                        if (env.IS_FIRST_BUILD == "true") {
                            echo "Checking if all services have been started successfully..."
                            def servicesList = SERVICES.split()
                            servicesList.each { service ->
                                def isRunning = sh(script: "docker ps | grep ${service} || echo 'NOT_RUNNING'", returnStdout: true).trim()
                                if (isRunning == 'NOT_RUNNING' || !isRunning.contains(service)) {
                                    echo "Service ${service} is not running, attempting to start it..."
                                    sh "docker compose up -d ${service} || { echo 'Failed to start ${service} but continuing'; }"
                                }
                            }
                        }
                    } else {
                        echo "No services to deploy"
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "Verifying all services are running..."
                    def servicesList = SERVICES.split()
                    def failedServices = []

                    servicesList.each { service ->
                        def isRunning = sh(script: "docker ps | grep ${service} || echo 'NOT_RUNNING'", returnStdout: true).trim()
                        if (isRunning == 'NOT_RUNNING' || !isRunning.contains(service)) {
                            echo "WARNING: Service ${service} is not running!"
                            failedServices.add(service)
                        } else {
                            echo "Service ${service} is running."
                        }
                    }

                    if (failedServices.size() > 0) {
                        echo "The following services failed to start: ${failedServices.join(', ')}"
                        echo "Checking logs for failed services..."

                        failedServices.each { service ->
                            echo "Logs for ${service}:"
                            sh "docker compose logs ${service} | tail -n 50"
                        }

                        // Không thất bại pipeline, nhưng ghi nhận lỗi
                        echo "WARNING: Some services failed to start, but continuing pipeline."
                    } else {
                        echo "All services are running correctly."
                    }
                }
            }
        }

        stage('Restore Environment Variables') {
            when {
                expression { env.IS_FIRST_BUILD != "true" }
            }
            steps {
                script {
                    if (env.CHANGED_SERVICES) {
                        env.CHANGED_SERVICES.split().each { service ->
                            echo "Restoring environment variables for service: ${service}"
                            sh """
                            if [ -f "${ENV_BACKUP_DIR}/${service}.env" ] && [ -s "${ENV_BACKUP_DIR}/${service}.env" ]; then
                                echo "Found environment backup for ${service}, restoring..."

                                # Wait a bit for container to be fully up
                                sleep 20

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
    }

    post {
        success {
            echo 'Deployment successful!'
            script {
                if (env.IS_FIRST_BUILD == "true") {
                    echo "Initial build and deployment completed successfully!"
                } else if (env.CHANGED_SERVICES) {
                    echo "Updated services: ${env.CHANGED_SERVICES}"
                } else {
                    echo "No services were updated"
                }

                // Hiển thị URL truy cập API Gateway
                echo "API Gateway is accessible at: http://localhost:45678"
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
