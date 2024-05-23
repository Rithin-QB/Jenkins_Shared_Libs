// vars/pipeline.groovy

def call(Map config) {
    pipeline {
        agent any

        environment {
            DOCKER_HUB_CREDENTIALS = credentials(config.dockerCredentialsId)
        }

        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkoutGitRepo(config.repoUrl, config.branch)
                    }
                }
            }
            stage('Build') {
                steps {
                    script {
                        dir(config.buildDir) {
                            buildCode(config.buildCommand)
                        }
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        dir(config.dockerDir) {
                            buildDockerImage(config.dockerfile, config.imageName, config.imageTag)
                        }
                    }
                }
            }
            stage('Push Docker Image') {
                steps {
                    script {
                        sh 'echo $DOCKER_HUB_CREDENTIALS_PSW | docker login -u $DOCKER_HUB_CREDENTIALS_USR --password-stdin'
                        pushDockerImage(config.imageName, config.imageTag, config.dockerRepoUrl)
                    }
                }
            }
        }
    }
}

def checkoutGitRepo(String repoUrl, String branch) {
    checkout([$class: 'GitSCM', branches: [[name: branch]], userRemoteConfigs: [[url: repoUrl]]])
}

def buildCode(String buildCommand) {
    sh buildCommand
}

def buildDockerImage(String dockerfile, String imageName, String imageTag) {
    sh "docker build -t ${imageName}:${imageTag} -f ${dockerfile} ."
}

def pushDockerImage(String imageName, String imageTag, String dockerRepoUrl) {
    sh "docker tag ${imageName}:${imageTag} ${dockerRepoUrl}/${imageName}:${imageTag}"
    sh "docker push ${dockerRepoUrl}/${imageName}:${imageTag}"
}
