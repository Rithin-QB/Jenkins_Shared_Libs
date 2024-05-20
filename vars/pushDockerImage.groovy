def call(String imageName, String imageTag, String dockerRepoUrl) {
    sh "docker tag ${imageName}:${imageTag} ${dockerRepoUrl}"
    sh "docker push ${dockerRepoUrl}/${imageName}:${imageTag}"
}
