def call(String imageName, String imageTag, String dockerRepoUrl) {
    sh "docker tag ${imageName}:${imageTag} ${dockerRepoUrl}/${imageName}:${imageTag}"
    sh "docker push ${dockerRepoUrl}/${imageName}:${imageTag}"
}