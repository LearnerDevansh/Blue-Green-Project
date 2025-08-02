pipeline {
    agent any

    environment {
        IMAGE_NAME = 'bankapp-image'
        NEXUS_REGISTRY = 'localhost:5000'
        SONAR_PROJECT_KEY = 'bankapp'
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_LOGIN = credentials('sonar-token')
        GITHUB_TOKEN = credentials('github-creds')
        K8S_MONITORING_ENABLED = 'true'
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
                sh 'docker build -t $NEXUS_REGISTRY/$IMAGE_NAME:latest .'
            }
        }

        stage('Trivy Image Scan') {
            steps {
                echo "🔬 Scanning Docker image..."
                sh 'trivy image --exit-code 1 --severity HIGH,CRITICAL $NEXUS_REGISTRY/$IMAGE_NAME || echo "🔴 Vulnerabilities found!"'
            }
        }

        stage('Push to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD' )]) {
                   sh '''
                       echo "$PASSWORD" | docker login $NEXUS_REGISTRY -u "$USERNAME" --password-stdin
                       
                       echo "📦 Pushing Docker image to Nexus..."
                       docker push $NEXUS_REGISTRY/$IMAGE_NAME:latest || {
                           echo "⏳ Push failed, retrying in 10s..."
                           sleep 10
                           docker push $NEXUS_REGISTRY/$IMAGE_NAME:latest
                       }
                    '''
                }
            }
        }
    }

        stage('Prometheus Monitoring (EKS)') {
            when {
                expression {return env.K8S_MONITORING_ENABLED == 'true'}
            }
            steps {
                echo "🔍 Validating Prometheus target status from inside the cluster..."

                // port-forward Prometheus UI to check scrape targets (optional)
                sh '''
                    POD=$(kubectl get pod -n monitoring -l "app.kubernetes.io/name=prometheus" -o jsonpath="{.items[0].metadata.name}")
                    kubectl port-forward -n monitoring $POD 9090:9090 &

                    sleep 10
                    curl -s http://localhost:9090/api/v1/targets | tee prometheus-targets.json | jq '.data.activeTargets[] | {instance, health, scrapeUrl}'
                '''
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
