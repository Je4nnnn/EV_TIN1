pipeline {
    agent any

    parameters {
        booleanParam(name: 'BUILD_DOCKER_IMAGES', defaultValue: true, description: 'Construye las imagenes Docker del backend y frontend')
        booleanParam(name: 'PUSH_DOCKER_IMAGES', defaultValue: true, description: 'Sube las imagenes Docker a DockerHub')
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: 'je4nn', description: 'Namespace o usuario de DockerHub')
        string(name: 'DOCKERHUB_CREDENTIALS_ID', defaultValue: 'dockerhub-credentials', description: 'ID de credenciales DockerHub en Jenkins')
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Tag de las imagenes Docker')
    }

    environment {
        JAVA_HOME = "${WORKSPACE}/tools/jdk-21"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        BACKEND_IMAGE = "${params.DOCKERHUB_NAMESPACE}/travelagency-backend:${params.IMAGE_TAG}"
        FRONTEND_IMAGE = "${params.DOCKERHUB_NAMESPACE}/travelagency-frontend:${params.IMAGE_TAG}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Java 21') {
            steps {
                sh 'chmod +x scripts/setup-java21.sh Backend/mvnw'
                sh './scripts/setup-java21.sh'
                sh 'java -version'
            }
        }

        stage('Build Backend') {
            steps {
                dir('Backend') {
                    sh './mvnw -B clean verify'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'Backend/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'Backend/target/*.jar', onlyIfSuccessful: true
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('Frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'Frontend/dist/**', onlyIfSuccessful: true
                }
            }
        }

        stage('Build Docker Images') {
            when {
                expression { return params.BUILD_DOCKER_IMAGES }
            }
            steps {
                sh 'docker build -t ${BACKEND_IMAGE} Backend'
                sh 'docker build --build-arg VITE_API_BASE_URL=/ --build-arg VITE_PAYROLL_BACKEND_SERVER=/ -t ${FRONTEND_IMAGE} Frontend'
            }
        }

        stage('Push Docker Images') {
            when {
                expression { return params.BUILD_DOCKER_IMAGES && params.PUSH_DOCKER_IMAGES }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: params.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh 'echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin'
                    sh 'docker push ${BACKEND_IMAGE}'
                    sh 'docker push ${FRONTEND_IMAGE}'
                }
            }
        }
    }
}
