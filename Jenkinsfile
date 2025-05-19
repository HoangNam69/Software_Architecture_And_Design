pipeline {
    agent none

    stages {
        stage('Maven Build & Test') {
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
            }
        }

        stage('Docker Operations') {
            agent any
            stages {
                stage('Build Docker Images') {
                    steps {
                        script {
                            sh 'docker-compose build'
                        }
                    }
                }
                stage('Deploy') {
                    steps {
                        script {
                            sh 'docker-compose down || true'
                            sh 'docker-compose up -d'
                        }
                    }
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
