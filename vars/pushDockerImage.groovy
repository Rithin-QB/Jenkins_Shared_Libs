def call(String imageName, String imageTag, String dockerRepoUrl) {
    def image = "${dockerRepoUrl}/${imageName}:${imageTag}"
    sh "docker tag ${imageName}:${imageTag} ${image}"
    sh "docker push ${image}"
}
