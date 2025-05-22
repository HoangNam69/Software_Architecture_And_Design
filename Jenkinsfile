pipeline {
    agent any

    environment {
        // Define services to monitor for changes
        SERVICES = "api-gateway admin-service authentication-service cart-service category-service order-service payment-service product-service report-service"
        // Change path to a directory with write permissions
        ENV_BACKUP_DIR = "/tmp/env-backup"
        ENV_CONFIG_DIR = "/tmp/env-backup/config"
        // Flag to track first build
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
                    // Ensure backup and config directories exist
                    sh "mkdir -p ${ENV_BACKUP_DIR}"
                    sh "mkdir -p ${ENV_CONFIG_DIR}"

                    // Check if env-config volume exists
                    def volumeExists = sh(script: "docker volume ls | grep env-config || echo 'NOT_FOUND'", returnStdout: true).trim()
                    if (volumeExists == 'NOT_FOUND' || !volumeExists.contains("env-config")) {
                        echo "Creating env-config volume..."
                        sh "docker volume create env-config"
                    }

                    // Ensure the microservices-network is properly created or recreated
                    echo "Setting up microservices-network..."
                    sh """
                    # Remove existing network if it exists
                    docker network rm microservices-network || true
                    # Create a fresh network
                    docker network create microservices-network || true
                    """

                    // Ensure env files for services exist
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
                    // Initialize first-time initialization flag
                    def isFirstTimeInit = false

                    // Check if any services have already been created
                    def existingServices = sh(script: "docker compose ps -a --format '{{.Name}}' || echo 'NO_SERVICES'", returnStdout: true).trim()
                    echo "Existing services: ${existingServices}"

                    // If no services found, this may be the first initialization
                    if (existingServices == 'NO_SERVICES' || existingServices.isEmpty()) {
                        echo "No services found. This appears to be the first time initialization."
                        isFirstTimeInit = true
                    } else {
                        // Count existing services and compare with defined services
                        def serviceCount = existingServices.split('\n').size()
                        def definedServicesList = SERVICES.split()

                        if (serviceCount < definedServicesList.size()) {
                            echo "Found ${serviceCount} services, but ${definedServicesList.size()} are defined. This might be a partial initialization."
                            isFirstTimeInit = true
                        }
                    }

                    // If first initialization, build all services
                    if (isFirstTimeInit) {
                        echo "First time initialization detected. Will build all services."
                        env.CHANGED_SERVICES = SERVICES
                        env.IS_FIRST_BUILD = "true"
                        return
                    }

                    // If not first initialization, proceed with change detection logic
                    try {
                        // Check if there's a previous commit
                        def hasAnyCommit = sh(script: 'git rev-parse --verify HEAD', returnStatus: true) == 0

                        if (!hasAnyCommit) {
                            echo "No previous commits found. Will build all services."
                            env.CHANGED_SERVICES = SERVICES
                            env.IS_FIRST_BUILD = "true"
                            return
                        }

                        // Get latest commit
                        def lastCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()

                        // Check if there's a previous commit
                        def hasPreviousCommit = sh(script: 'git rev-parse HEAD~1', returnStatus: true) == 0

                        // If no previous commit, build all
                        if (!hasPreviousCommit) {
                            echo "This is the first commit. Will build all services."
                            env.CHANGED_SERVICES = SERVICES
                            env.IS_FIRST_BUILD = "true"
                            return
                        }

                        def previousCommit = sh(script: 'git rev-parse HEAD~1', returnStdout: true).trim()

                        // Get list of changed files between commits
                        def changedFiles = sh(script: "git diff --name-only ${previousCommit} ${lastCommit}", returnStdout: true).trim()
                        echo "Changed files: ${changedFiles}"

                        // Initialize map to track service changes
                        def changedServices = [:]
                        def anyServiceChanged = false
                        def servicesList = SERVICES.split()

                        // Check each service for changes
                        servicesList.each { service ->
                            // Check if service directory has changes
                            def serviceChanged = changedFiles.contains("${service}/")
                            changedServices[service] = serviceChanged

                            if (serviceChanged) {
                                anyServiceChanged = true
                                echo "Service ${service} has changes"
                            }
                        }

                        // If no specific service changed but common files changed (like docker-compose.yml)
                        if (!anyServiceChanged && (changedFiles.contains("docker-compose.yml") || changedFiles.contains("Jenkinsfile"))) {
                            echo "Common configuration files changed, will update all services"
                            servicesList.each { service ->
                                changedServices[service] = true
                            }
                        }

                        // Check for services that don't exist or aren't running
                        servicesList.each { service ->
                            def containerExists = sh(script: "docker ps -a | grep ${service} || echo 'NOT_FOUND'", returnStdout: true).trim()

                            if (containerExists == 'NOT_FOUND' || !containerExists.contains(service)) {
                                echo "Service ${service} does not exist, will build it"
                                changedServices[service] = true
                            } else {
                                // Check if container is running
                                def containerRunning = sh(script: "docker ps | grep ${service} || echo 'NOT_RUNNING'", returnStdout: true).trim()
                                if (containerRunning == 'NOT_RUNNING' || !containerRunning.contains(service)) {
                                    echo "Service ${service} exists but is not running, will build it"
                                    changedServices[service] = true
                                }
                            }
                        }

                        // Save change status to environment variable for later stages
                        env.CHANGED_SERVICES = changedServices.findAll { it.value == true }.keySet().join(" ")
                        echo "Services to build: ${env.CHANGED_SERVICES}"

                        // If no services need building, set to all to ensure missing services are built
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
                    // Set executable permissions for changed services
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

        stage('Docker Build Changed Services') {
            steps {
                script {
                    // Display Docker path
                    sh 'which docker'

                    if (env.CHANGED_SERVICES) {
                        // Check if it's the first build
                        if (env.IS_FIRST_BUILD == "true") {
                            echo "First time build detected. Building with --no-cache to ensure fresh builds."
                            env.CHANGED_SERVICES.split().each { service ->
                                echo "Building service: ${service}"
                                sh "docker compose build --no-cache ${service} || { echo 'Failed to build ${service} but continuing'; }"
                            }
                        } else {
                            // If not first build, build normally
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
                        // Stop and remove ALL containers first to avoid conflicts
                        echo "Stopping and removing all existing services before redeploying..."
                        sh """
                        # Stop all existing containers first
                        docker stop api-gateway admin-service authentication-service cart-service category-service order-service payment-service product-service report-service || true

                        # Remove all existing containers
                        docker rm -f api-gateway admin-service authentication-service cart-service category-service order-service payment-service product-service report-service || true

                        # Stop docker compose if needed
                        docker compose down || true

                        # Ensure network exists and is fresh
                        docker network rm microservices-network || true
                        docker network create microservices-network || true
                        """

                        // Start with the api-gateway first
                        echo "Starting api-gateway first..."
                        sh """
                        docker compose up -d api-gateway || echo 'Failed to start api-gateway but continuing'
                        # Wait for the gateway to be ready
                        sleep 30
                        """

                        // Start other services one by one
                        env.CHANGED_SERVICES.split().each { service ->
                            if (service != "api-gateway") {
                                echo "Starting service: ${service}"
                                sh """
                                docker compose up -d ${service} || echo 'Failed to start ${service} but continuing'
                                # Brief pause between service starts
                                sleep 10
                                """
                            }
                        }

                        // Check if all services are running
                        echo "Checking if all services have been started successfully..."
                        def servicesList = SERVICES.split()
                        servicesList.each { service ->
                            def isRunning = sh(script: "docker ps | grep ${service} || echo 'NOT_RUNNING'", returnStdout: true).trim()
                            if (isRunning == 'NOT_RUNNING' || !isRunning.contains(service)) {
                                echo "Service ${service} is not running, attempting to start it again..."
                                sh "docker compose up -d ${service} || echo 'Failed to start ${service} but continuing'"
                                sh "sleep 10"
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

                    // Allow more time for startup
                    sh "echo 'Waiting 60 seconds for services to start...'"
                    sh "sleep 60"

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

                        // Don't fail pipeline, but log error
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

                // Display API Gateway access URL
                echo "API Gateway is accessible at: http://localhost:45678"
            }
        }
        failure {
            echo 'Build or deployment failed'
            script {
                // In case of failure, display logs of changed services for debugging
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
