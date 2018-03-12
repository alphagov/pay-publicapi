#!/usr/bin/env groovy

pipeline {
  agent any

  options {
    ansiColor('xterm')
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
  }

  stages {
    stage('Maven Build') {
      steps {
        script {
          def long stepBuildTime = System.currentTimeMillis()

          sh 'mvn clean package'
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
    stage('Test') {
      steps {
        runEndToEnd("publicapi")
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
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployEcs("publicapi", "test", null, true, true)
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
  }
}
