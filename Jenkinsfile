pipeline {
    agent {
        docker {
            image 'maven:3.8-openjdk-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Build Docker Images') {
            // For this stage we need Docker, which isn't in the Maven image
            // So we use the host Jenkins agent
            agent {
                any
            }
            steps {
                script {
                    sh 'docker-compose build'
                }
            }
        }

        stage('Deploy') {
            // Similarly, we need Docker for deployment
            agent {
                any
            }
            steps {
                script {
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
