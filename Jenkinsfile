pipeline {
    agent any

    parameters {
        booleanParam(name: 'BUILD_DOCKER_IMAGES', defaultValue: false, description: 'Construye las imagenes Docker del backend y frontend')
    }

    environment {
        JAVA_HOME = "${WORKSPACE}/tools/jdk-21"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
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
                    junit 'Backend/target/surefire-reports/*.xml'
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
                sh 'docker build -t hotelrm-backend:${BUILD_NUMBER} Backend'
                sh 'docker build -t hotelrm-frontend:${BUILD_NUMBER} Frontend'
            }
        }
    }
}
