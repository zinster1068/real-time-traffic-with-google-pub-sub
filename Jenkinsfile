pipeline {
    agent {
        docker {
            image 'maven:latest'
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
