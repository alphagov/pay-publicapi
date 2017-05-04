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
        sh 'mvn clean package'
      }
    }
    stage('Docker Build') {
      steps {
        script {
          buildApp{
            app = "publicapi"
          }
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
          dockerTag {
            app = "publicapi"
          }
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deploy("publicapi", "test", null, true)
      }
    }
  }
}
