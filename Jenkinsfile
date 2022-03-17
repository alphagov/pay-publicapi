#!/usr/bin/env groovy

pipeline {
  agent any

  parameters {
    booleanParam(defaultValue: false, description: '', name: 'runEndToEndTestsOnPR')
    string(defaultValue: 'card,products,zap', description: 'The tests to run', name: 'E2E_TESTS')
  }

  options {
    ansiColor('xterm')
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
    RUN_END_TO_END_ON_PR = "${params.runEndToEndTestsOnPR}"
    JAVA_HOME="/usr/lib/jvm/java-1.11.0-openjdk-amd64"
  }

  stages {
    stage('Maven Build') {
      when {
        branch 'master'
      }
      steps {
        script {
          def stepBuildTime = System.currentTimeMillis()
          def commit = gitCommit()
          def branchName = 'master'

          sh 'docker pull redis:latest'
          sh 'mvn -version'
          sh "mvn clean verify"
          postSuccessfulMetrics("publicapi.maven-build", stepBuildTime)
        }
      }
      post {
        failure {
          postMetric("publicapi.maven-build.failure", 1)
        }
      }
    }
    stage('Docker Build') {
      steps {
        script {
          buildAppWithMetrics {
            app = "publicapi"
          }
        }
      }
      post {
        failure {
          postMetric("publicapi.docker-build.failure", 1)
        }
      }
    }
    stage('Docker Tag') {
      steps {
        script {
          dockerTagWithMetrics {
            app = "publicapi"
          }
        }
      }
      post {
        failure {
          postMetric("publicapi.docker-tag.failure", 1)
        }
      }
    }
     stage('Complete') {
       failFast true
       parallel {
         stage('Tag Build') {
           when {
             branch 'master'
           }
           steps {
             tagDeployment("publicapi")
           }
         }
       }
     }
  }
  post {
    failure {
      postMetric(appendBranchSuffix("publicapi") + ".failure", 1)
    }
    success {
      postSuccessfulMetrics(appendBranchSuffix("publicapi"))
    }
    always {
        junit "**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml"
    }
  }
}
