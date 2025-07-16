pipeline {
    agent any

    environment {
        IMAGE_NAME = 'bankapp-image'
        DOCKER_REGISTRY = 'devanshpandey21'
        SONAR_PROJECT_KEY = 'bankapp'
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_LOGIN = credentials('sonar-token')
        GITHUB_TOKEN = credentials('github-creds')
    }

    stages {

        stage('Checkout Code') {
            steps {
                echo "📥 Checking out source code..."
                git(
                    url: 'https://github.com/LearnerDevansh/Blue-Green-Project.git',
                    credentialsId: 'github-creds',
                    branch: 'main'
                )
            }
        }

        stage('Maven Build') {
            steps {
                echo "🛠️ Building application..."
                sh 'mvn clean package -DskipTests=true'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo "🧪 Running unit tests..."
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
                echo "🔍 Scanning source files..."
                sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL .'
            }
        }

        stage('SonarQube Scan') {
            steps {
                echo "📡 Running SonarQube analysis..."
                withSonarQubeEnv('MySonarQube') {
                    sh """
                        mvn verify sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_LOGIN}
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "🐳 Building Docker image..."
                sh 'docker build -t $DOCKER_REGISTRY/$IMAGE_NAME:latest .'
            }
        }

        stage('Trivy Image Scan') {
            steps {
                echo "🔬 Scanning Docker image..."
                sh 'trivy image --exit-code 1 --severity HIGH,CRITICAL $DOCKER_REGISTRY/$IMAGE_NAME || echo "🔴 Vulnerabilities found!"'
            }
        }

        stage('Push Docker Image') {
            steps {
                echo "📤 Pushing Docker image..."
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh '''
                        echo "$PASSWORD" | docker login -u "$USERNAME" --password-stdin
                        docker push "$DOCKER_REGISTRY/$IMAGE_NAME:latest" || {
                            echo "⏳ First push failed. Retrying..."
                            sleep 10
                            docker push "$DOCKER_REGISTRY/$IMAGE_NAME:latest"
                        }
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