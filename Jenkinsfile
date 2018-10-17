pipeline {
    agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /var/services/homes/pfs/maven/repos:/root/.m2'
        }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
    }
}
