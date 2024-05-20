def call(String dockerfilePath, String imageName, String imageTag) {
    sh "sudo docker build -t ${imageName}:${imageTag} -f ${dockerfilePath} ."
}
