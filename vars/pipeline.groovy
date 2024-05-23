def call(Map config=[:]) {
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
                        buildCode(config.buildCommand)
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
