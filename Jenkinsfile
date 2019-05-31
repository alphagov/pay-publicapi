#!/usr/bin/env groovy

pipeline {
  agent any

  parameters {
    booleanParam(defaultValue: false, description: '', name: 'runEndToEndTestsOnPR')
    booleanParam(defaultValue: false, description: '', name: 'runZapTestsOnPR')
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
    RUN_ZAP_ON_PR = "${params.runZapTestsOnPR}"
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

          withCredentials([
                  string(credentialsId: 'pact_broker_username', variable: 'PACT_BROKER_USERNAME'),
                  string(credentialsId: 'pact_broker_password', variable: 'PACT_BROKER_PASSWORD')]
          ) {
              sh 'mvn -version'
              sh "mvn clean verify pact:publish -DPACT_BROKER_URL=https://pact-broker-test.cloudapps.digital -DPACT_CONSUMER_VERSION=${commit}" +
                      " -DPACT_BROKER_USERNAME=${PACT_BROKER_USERNAME} -DPACT_BROKER_PASSWORD=${PACT_BROKER_PASSWORD} -DPACT_CONSUMER_TAG=${branchName}"
          }
          postSuccessfulMetrics("publicapi.maven-build", stepBuildTime)
        }
      }
      post {
        failure {
          postMetric("publicapi.maven-build.failure", 1)
        }
      }
    }
    stage('Maven Build Branch') {
      when {
        not {
          branch 'master'
        }
      }
      steps {
        script {
          def stepBuildTime = System.currentTimeMillis()
          def commit = gitCommit()
          def branchName = gitBranchName()

          withCredentials([
                  string(credentialsId: 'pact_broker_username', variable: 'PACT_BROKER_USERNAME'),
                  string(credentialsId: 'pact_broker_password', variable: 'PACT_BROKER_PASSWORD')]
          ) {
              sh 'mvn -version'
              sh "mvn clean verify pact:publish -DPACT_BROKER_URL=https://pact-broker-test.cloudapps.digital -DPACT_CONSUMER_VERSION=${commit}" +
                      " -DPACT_BROKER_USERNAME=${PACT_BROKER_USERNAME} -DPACT_BROKER_PASSWORD=${PACT_BROKER_PASSWORD} -DPACT_CONSUMER_TAG=${branchName}"
          }
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
    stage('Contract Tests') {
        steps {
            script {
                env.PACT_TAG = gitBranchName()
            }
            ws('contract-tests-wp') {
                runPactProviderTests("pay-direct-debit-connector", "${env.PACT_TAG}")
                runPactProviderTests("pay-connector", "${env.PACT_TAG}")
            }
        }
        post {
            always {
                ws('contract-tests-wp') {
                    deleteDir()
                }
            }
        }
    }
    stage('Tests') {
      failFast true
      stages {
        stage('Card Payment End-to-End Tests') {
            when {
                anyOf {
                  branch 'master'
                  environment name: 'RUN_END_TO_END_ON_PR', value: 'true'
                }
            }
            steps {
                runCardPaymentsE2E("publicapi")
            }
        }
        stage('Products End-to-End Tests') {
            when {
                anyOf {
                  branch 'master'
                  environment name: 'RUN_END_TO_END_ON_PR', value: 'true'
                }
            }
            steps {
                runProductsE2E("publicapi")
            }
        }
        stage('Direct-Debit End-to-End Tests') {
            when {
                anyOf {
                  branch 'master'
                  environment name: 'RUN_END_TO_END_ON_PR', value: 'true'
                }
            }
            steps {
                runDirectDebitE2E("publicapi")
            }
        }
        stage('ZAP Tests') {
           when {
               anyOf {
                 branch 'master'
                 environment name: 'RUN_ZAP_ON_PR', value: 'true'
               }
           }
           steps {
               runZap("publicapi")
           }
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
     stage('Deploy') {
       when {
         branch 'master'
       }
       steps {
         deployEcs("publicapi")
       }
     }
     stage('Smoke Tests') {
       failFast true
       parallel {
         stage('Card Smoke Test') {
           when { branch 'master' }
           steps { runCardSmokeTest() }
         }
         stage('Direct Debit Smoke Test') {
           when { branch 'master' }
           steps { runDirectDebitSmokeTest() }
         }
       }
     }
     stage('Pact Tag') {
       when {
         branch 'master'
       }
       steps {
         echo 'Tagging consumer pact with "test"'
         tagPact("publicapi", gitCommit(), "test")
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
         stage('Trigger Deploy Notification') {
           when {
             branch 'master'
           }
           steps {
             triggerGraphiteDeployEvent("publicapi")
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
