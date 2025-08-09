pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "baljeet389/website:latest"
        DOCKER_REGISTRY = "docker.io"
    }

    stages {
	
		stage('Checkout with Submodules') {
            steps {
                checkout scm
                sh 'git submodule update --init --recursive'
            }
		}	
			
        stage('Build React Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Copy React Build to Spring Boot') {
            steps {
                sh 'rm -rf backend/src/main/resources/static/*'
                sh 'cp -r frontend/build/* backend/src/main/resources/static/'
            }
        }

        stage('Build Spring Boot App') {
            steps {
                dir('backend') {
                    sh './mvnw clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh "echo $PASSWORD | docker login $DOCKER_REGISTRY -u $USERNAME --password-stdin"
                    sh "docker tag $DOCKER_IMAGE $DOCKER_REGISTRY/$DOCKER_IMAGE"
                    sh "docker push $DOCKER_REGISTRY/$DOCKER_IMAGE"
                }
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}
