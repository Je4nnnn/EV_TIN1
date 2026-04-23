def runCommand(String unixCommand, String windowsCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}

def withProjectJava(Closure body) {
    def javaHome = isUnix() ? "${env.WORKSPACE}/tools/jdk-21" : "${env.WORKSPACE}\\tools\\jdk-21"
    def separator = isUnix() ? ':' : ';'
    def javaBin = isUnix() ? "${javaHome}/bin" : "${javaHome}\\bin"

    withEnv([
        "JAVA_HOME=${javaHome}",
        "PATH=${javaBin}${separator}${env.PATH}"
    ]) {
        body()
    }
}

def imageTag(String namespace, String imageName, String versionTag) {
    return namespace?.trim()
        ? "${namespace.trim()}/${imageName}:${versionTag}"
        : "${imageName}:${versionTag}"
}

pipeline {
    agent any

    parameters {
        booleanParam(name: 'BUILD_DOCKER_IMAGES', defaultValue: false, description: 'Construye imagenes Docker del backend y frontend')
        booleanParam(name: 'PUSH_DOCKER_IMAGES', defaultValue: false, description: 'Publica las imagenes en Docker Hub')
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: 'mtisw', description: 'Namespace o usuario de Docker Hub')
        string(name: 'DOCKERHUB_CREDENTIALS_ID', defaultValue: 'dockerhub', description: 'Credentials ID de Jenkins para Docker Hub')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Java 21') {
            steps {
                script {
                    withProjectJava {
                        if (isUnix()) {
                            runCommand(
                                'chmod +x scripts/setup-java21.sh Backend/mvnw && ./scripts/setup-java21.sh && java -version',
                                ''
                            )
                        } else {
                            runCommand(
                                '',
                                'powershell -ExecutionPolicy Bypass -File scripts\\setup-java21.ps1 && java -version'
                            )
                        }
                    }
                }
            }
        }

        stage('Verify Tooling') {
            steps {
                script {
                    withProjectJava {
                        runCommand('node --version && npm --version', 'node --version && npm --version')
                        if (params.BUILD_DOCKER_IMAGES || params.PUSH_DOCKER_IMAGES) {
                            runCommand('docker --version', 'docker --version')
                        }
                    }
                }
            }
        }

        stage('Build Backend') {
            steps {
                script {
                    withProjectJava {
                        dir('Backend') {
                            runCommand('./mvnw -B clean verify', 'mvnw.cmd -B clean verify')
                        }
                    }
                }
            }
            post {
                always {
                    junit 'Backend/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'Backend/target/*.jar,Backend/target/site/jacoco/**/*', allowEmptyArchive: true, fingerprint: true
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('Frontend') {
                    script {
                        runCommand('npm ci && npm run build', 'npm ci && npm run build')
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'Frontend/dist/**', allowEmptyArchive: true, fingerprint: true
                }
            }
        }

        stage('Build Docker Images') {
            when {
                expression { params.BUILD_DOCKER_IMAGES || params.PUSH_DOCKER_IMAGES }
            }
            steps {
                script {
                    env.BACKEND_IMAGE = imageTag(params.DOCKERHUB_NAMESPACE, 'hotelrm-backend', env.BUILD_NUMBER)
                    env.FRONTEND_IMAGE = imageTag(params.DOCKERHUB_NAMESPACE, 'hotelrm-frontend', env.BUILD_NUMBER)

                    runCommand(
                        "docker build -t ${env.BACKEND_IMAGE} Backend && docker build -t ${env.FRONTEND_IMAGE} Frontend",
                        "docker build -t ${env.BACKEND_IMAGE} Backend && docker build -t ${env.FRONTEND_IMAGE} Frontend"
                    )
                }
            }
        }

        stage('Validate Compose Files') {
            when {
                expression { params.BUILD_DOCKER_IMAGES || params.PUSH_DOCKER_IMAGES }
            }
            steps {
                script {
                    runCommand(
                        'docker compose config && docker compose -f docker/compose-ha.yml config',
                        'docker compose config && docker compose -f docker\\compose-ha.yml config'
                    )
                }
            }
        }

        stage('Push Docker Images') {
            when {
                expression { params.PUSH_DOCKER_IMAGES }
            }
            steps {
                script {
                    if (!params.DOCKERHUB_CREDENTIALS_ID?.trim()) {
                        error('Debes indicar DOCKERHUB_CREDENTIALS_ID para publicar imagenes.')
                    }

                    withCredentials([usernamePassword(
                        credentialsId: params.DOCKERHUB_CREDENTIALS_ID,
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )]) {
                        runCommand(
                            'echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin',
                            'echo %DOCKERHUB_PASSWORD%| docker login -u %DOCKERHUB_USERNAME% --password-stdin'
                        )
                        runCommand(
                            "docker push ${env.BACKEND_IMAGE} && docker push ${env.FRONTEND_IMAGE}",
                            "docker push ${env.BACKEND_IMAGE} && docker push ${env.FRONTEND_IMAGE}"
                        )
                    }
                }
            }
            post {
                always {
                    script {
                        runCommand('docker logout || true', 'docker logout')
                    }
                }
            }
        }
    }
}
