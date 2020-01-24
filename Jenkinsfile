def mvnProfile        = 'dev'
def galasaSignJarSkip = 'true'

pipeline {
// Initially run on any agent
   agent {
      label 'codesigning'
   }
   options { timestamps () }
   environment {
//Configure Maven from the maven tooling in Jenkins
      def mvnHome = tool 'Default'
      PATH = "${mvnHome}/bin:${env.PATH}"
      
//Set some defaults
      def workspace = pwd()
      def mvnGoal    = 'install'
   }
   stages {
// If it is the master branch, version 0.3.0 and master on all the other branches
      stage('set-dev') {
         when {
           environment name: 'GIT_BRANCH', value: 'origin/master'
         }
         steps {
            script {
               mvnProfile        = 'dev'
               mvnGoal           = 'deploy sonar:sonar'
               galasaSignJarSkip = 'false'
            }
         }
      }
// If the staging branch,  then set as appropriate
      stage('set-staging') {
         when {
           environment name: 'GIT_BRANCH', value: 'origin/staging'
         }
         steps {
            script {
               mvnGoal           = 'deploy'
               mvnProfile        = 'staging'
               galasaSignJarSkip = 'false'
            }
         }
     }
// for debugging purposes
      stage('report') {
         steps {
            echo "Branch/Tag         : ${env.GIT_BRANCH}"
            echo "Commit Hash        : ${env.GIT_COMMIT}"
            echo "Workspace directory: ${workspace}"
            echo "Maven Goal         : ${mvnGoal}"
            echo "Maven profile      : ${mvnProfile}"
            echo "Skip Signing JARs  : ${galasaSignJarSkip}"
         }
      }
   
// Set up the workspace, clear the git directories and setup the manve settings.xml files
      stage('prep-workspace') { 
         steps {
            configFileProvider([configFile(fileId: '86dde059-684b-4300-b595-64e83c2dd217', targetLocation: 'settings.xml')]) {
            }
            dir('repository/dev.galasa') {
               deleteDir()
            }
            dir('repository/dev/galasa') {
               deleteDir()
            }
         }
      }
      
      stage('Managers Parent') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                  }
               }
            }
         }
      }
      stage('Managers Core Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-core-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.core.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.core.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.artifact.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.artifact.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Managers Comms Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-comms-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.ipnetwork.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.http.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.http.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Managers zOS Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-zos-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.zos.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zosmf.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zosbatch.zosmf.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zosconsole.zosmf.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zosfile.zosmf.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos3270.common') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Djarsigner.skip=${galasaSignJarSkip} -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos3270.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos3270.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos3270.devtools') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos3270.ui') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Djarsigner.skip=${galasaSignJarSkip} -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.zos.feature') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Djarsigner.skip=${galasaSignJarSkip} -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Managers Unix Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-unix-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.linux.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.linux.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Managers Cloud Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-cloud-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.openstack.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.docker.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.docker.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                     
                     dir('dev.galasa.kubernetes.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.kubernetes.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Managers Other Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent/galasa-managers-other-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"

                     dir('dev.galasa.galasaecosystem.manager') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.galasaecosystem.manager.ivt') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
      stage('Remaining Maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-managers-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
   
                     dir('galasa-bom') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
   
                     dir('dev.galasa.managers.obr') {
                       sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.uber.obr') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.uber.karaffeature') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('dev.galasa.karaf.master.feature') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('galasa-master-api-server') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }

                     dir('galasa-uber-javadoc') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=false -Dgpg.passphrase=$GPG  -P ${mvnProfile} -B -e -fae --non-recursive ${mvnGoal}"
                     }
                  }
               }
            }
         }
      }
   }
   post {
       // triggered when red sign
       failure {
           slackSend (channel: '#project-galasa-devs', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
       }
    }
}
