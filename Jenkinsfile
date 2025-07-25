node {
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    def mvn = tool 'Default Maven';
    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=iainblack-sonar_jenkins-java_d3b28e7f-6e34-4507-ab02-7354d876fd1a -Dsonar.projectName='jenkins-java'"
    }
  }
}