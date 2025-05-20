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

        stage('Fix Maven Wrapper Permissions') {
            steps {
                script {
                    // Set executable permissions on Maven wrapper scripts in all services
                    sh '''
                    find ./ -name "mvnw" -exec chmod +x {} \\;
                    echo "Setting executable permissions on Maven wrapper scripts"
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

//         stage('Verify Services Health') {
//             steps {
//                 script {
//                     // Đợi tất cả các service khởi động
//                     sh '''
//                     echo "Waiting for all services to be healthy..."
//
//                     # Danh sách các services cần kiểm tra
//                     SERVICES=("api-gateway" "admin-service" "authentication-service" "cart-service" "category-service" "order-service" "payment-service" "product-service" "report-service")
//
//                     # Thiết lập timeout (180 giây = 3 phút)
//                     TIMEOUT=180
//                     START_TIME=$(date +%s)
//
//                     # Hàm kiểm tra service đã chạy và healthy
//                     check_services_health() {
//                         ALL_HEALTHY=true
//
//                         for SERVICE in "${SERVICES[@]}"; do
//                             # Kiểm tra service có hoạt động không
//                             STATUS=$(docker inspect --format='{{.State.Status}}' $SERVICE 2>/dev/null || echo "not_found")
//                             HEALTH=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no_health_check{{end}}' $SERVICE 2>/dev/null || echo "not_found")
//
//                             if [ "$STATUS" != "running" ]; then
//                                 echo "Service $SERVICE is not running yet (status: $STATUS)"
//                                 ALL_HEALTHY=false
//                                 break
//                             fi
//
//                             if [ "$HEALTH" == "starting" ] || [ "$HEALTH" == "unhealthy" ]; then
//                                 echo "Service $SERVICE is not healthy yet (health: $HEALTH)"
//                                 ALL_HEALTHY=false
//                                 break
//                             fi
//                         done
//
//                         echo $ALL_HEALTHY
//                     }
//
//                     # Kiểm tra liên tục cho đến khi timeout
//                     while true; do
//                         CURRENT_TIME=$(date +%s)
//                         ELAPSED=$((CURRENT_TIME - START_TIME))
//
//                         if [ $ELAPSED -gt $TIMEOUT ]; then
//                             echo "Timeout reached while waiting for services to be healthy!"
//                             docker compose ps
//                             docker compose logs
//                             exit 1
//                         fi
//
//                         # Hiển thị trạng thái các container
//                         echo "Checking services status (elapsed time: ${ELAPSED}s)..."
//                         docker compose ps
//
//                         # Kiểm tra health status
//                         HEALTH_CHECK=$(check_services_health)
//
//                         if [ "$HEALTH_CHECK" == "true" ]; then
//                             echo "All services are up and running!"
//                             break
//                         fi
//
//                         # Đợi 10 giây trước khi kiểm tra lại
//                         echo "Waiting 10 seconds before next health check..."
//                         sleep 10
//                     done
//                     '''
//                 }
//             }
//         }
//
//         stage('Verify API Connectivity') {
//             steps {
//                 script {
//                     // Kiểm tra API Gateway có thể truy cập được hay không
//                     sh '''
//                     echo "Testing API Gateway connectivity..."
//                     # Đợi 10 giây để các service hoàn tất kết nối
//                     sleep 10
//
//                     # Thử kết nối tới API Gateway và kiểm tra health status
//                     # Chú ý rằng chúng ta sử dụng port 45678 theo cấu hình đã được thay đổi
//                     ATTEMPTS=0
//                     MAX_ATTEMPTS=10
//
//                     while [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
//                         HTTP_CODE=$(curl -s -o /tmp/health_response -w "%{http_code}" http://localhost:45678/actuator/health)
//
//                         if echo $HTTP_CODE | grep -q "200"; then
//                             # Kiểm tra nội dung phản hồi để đảm bảo tất cả các service đều UP
//                             if grep -q "\"status\":\"UP\"" /tmp/health_response; then
//                                 echo "API Gateway is healthy and all services are UP!"
//                                 cat /tmp/health_response
//                                 break
//                             else
//                                 echo "API Gateway responded with status 200 but some services might be down:"
//                                 cat /tmp/health_response
//                                 echo "Attempt $((ATTEMPTS+1))/$MAX_ATTEMPTS: Waiting for all services to be UP..."
//                             fi
//                         else
//                             echo "Attempt $((ATTEMPTS+1))/$MAX_ATTEMPTS: API Gateway returned code $HTTP_CODE. Waiting..."
//                         fi
//
//                         ATTEMPTS=$((ATTEMPTS+1))
//                         sleep 10
//                     done
//
//                     if [ $ATTEMPTS -eq $MAX_ATTEMPTS ]; then
//                         echo "Failed to connect to API Gateway after multiple attempts."
//                         exit 1
//                     fi
//                     '''
//                 }
//             }
//         }
//     }

    post {
        success {
            echo 'Deployment successful! Application is running on port 45678'
        }
        failure {
            echo 'Build or deployment failed'
            script {
                // In trường hợp thất bại, hiển thị logs của tất cả các containers để giúp debug
                sh '''
                echo "Dumping logs from all containers for debugging..."
                docker compose logs
                '''
            }
        }
        always {
            cleanWs()
        }
    }
}
