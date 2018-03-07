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
    HOSTED_GRAPHITE_ACCOUNT_ID = credentials('graphite_account_id')
    HOSTED_GRAPHITE_API_KEY = credentials('graphite_api_key')
  }

  stages {
    stage('Maven Build') {
      steps {
        sh 'mvn clean package'
      }
      post {
        failure {
          postMetric("publicapi.maven-build.failure", 1, "new")
        }
        success {
          postSuccessfulMetrics("publicapi.maven-build")
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
          postMetric("publicapi.docker-build.failure", 1, "new")
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
          postMetric("publicapi.docker-tag.failure", 1, "new")
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
      postMetric("publicapi.failure", 1, "new")
    }
    success {
      postSuccessfulMetrics("publicapi")
    }
  }
}
