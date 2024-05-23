// vars/pipeline.groovy
def call(Map config) {
    pipeline {
        agent any

        environment {
            DOCKER_CREDENTIALS = credentials("${config.dockerCredentialsId ?: 'dockerhub-halkeye'}")
            DOCKER_REGISTRY = "${config.dockerRepoUrl ?: ''}"
            IMAGE_NAME = "${config.imageName}"
            IMAGE_TAG = "${config.imageTag ?: 'latest'}"
            DOCKERFILE = "${config.dockerfile ?: 'Dockerfile'}"
        }

        options {
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
            timeout(time: 60, unit: "MINUTES")
            ansiColor("xterm")
        }

        stages {
            stage('Checkout') {
                steps {
                    script {
                       checkout([$class: 'GitSCM', branches: [[name: branch]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: repoUrl]]])
                    }
                }
            }
            stage('Build') {
                steps {
                    script {
                        sh(config.buildCommand ?: 'mvn clean install')
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        sh """
                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -f ${DOCKERFILE} .
                        """
                    }
                }
            }
            stage('Push Docker Image') {
                steps {
                    script {
                        withCredentials([usernamePassword(credentialsId: config.dockerCredentialsId, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            sh """
                                docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
                                docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                                docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                            """
                        }
                    }
                }
            }
        }

        post {
            failure {
                emailext(
                    attachLog: true,
                    recipientProviders: [developers()],
                    body: "Build failed (see ${env.BUILD_URL})",
                    subject: "[JENKINS] ${env.JOB_NAME} failed"
                )
            }
        }
    }
}
