def call(String dockerfilePath, String imageName, String imageTag) {
    sh "DOCKER_BUILDKIT=1 sudo docker build -t ${imageName}:${imageTag} -f ${dockerfilePath} ."
}
