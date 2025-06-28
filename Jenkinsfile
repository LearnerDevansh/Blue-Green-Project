pipeline {
    agent any

    environment {
        IMAGE_NAME = 'bankapp-image'
        DOCKER_REGISTRY = 'devanshpandey21'
        SONAR_PROJECT_KEY = 'bankapp'
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_LOGIN = credentials('sonar-token')
    }

    stages {

        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/LearnerDevansh/Blue-Green-Project.git'
            }
        }

        stage('Maven .jar Build') {
            steps {
                sh 'mvn clean package -DskipTests=true'
            }
        }

        stage('Unit and Integration Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Trivy File Scan') {
            steps {
                sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL .'
            }
        }

        stage('SonarQube Scan') {
            environment {
                SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
            }
            steps {
                withSonarQubeEnv('MySonarQube') {
                    sh '''
                        ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.sources=src \
                        -Dsonar.java.binaries=target \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_LOGIN}
                    '''
                }
            }
        }

        stage('Docker Image Build') {
            steps {
                sh 'docker build -t $DOCKER_REGISTRY/$IMAGE_NAME .'
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh 'trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_REGISTRY/$IMAGE_NAME || echo "🔴 Vulnerabilities found!"'
            }
        }

        stage('Push to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
                        echo "$PASSWORD" | docker login -u "$USERNAME" --password-stdin
                        docker push $DOCKER_REGISTRY/$IMAGE_NAME
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline Executed Successfully.'
        }
        failure {
            echo '❌ Pipeline Failed.'
        }
    }
}
