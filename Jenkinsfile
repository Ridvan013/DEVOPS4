pipeline {
    agent any

    // PS1: Pipeline GitHub push/merge ile tetiklenir.
    // Jenkins yerelde calistigi icin GitHub webhook'unun Jenkins'e ulasmasi
    // gerekir (KURULUM.md -> ngrok adimina bakin). Webhook kullanamiyorsaniz
    // asagidaki pollSCM satirini aktif edip githubPush'u yorum yapin.
    triggers {
        // githubPush(): webhook (ngrok ile). pollSCM: webhook'suz da calisir,
        // her dakika GitHub'i kontrol eder -> push/merge'de otomatik tetikler (PS1).
        githubPush()
        pollSCM('* * * * *')
    }

    environment {
        IMAGE       = 'ridvandursun/devops4'
        GIT_URL     = 'https://github.com/Ridvan013/DEVOPS4.git'
        GIT_BRANCH  = 'main'
        DOCKERHUB   = credentials('dockerhub') // Jenkins -> Credentials (Username + Password/Token)
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('1) Clone from GitHub') {
            steps {
                echo "GitHub'dan proje klonlaniyor: ${env.GIT_URL}"
                git branch: "${env.GIT_BRANCH}", url: "${env.GIT_URL}"
            }
        }

        stage('2) Build JAR') {
            steps {
                echo 'Gradle ile jar olusturuluyor...'
                bat 'gradlew.bat clean bootJar --no-daemon'
                bat 'dir build\\libs'
            }
        }

        stage('3) Build Docker image') {
            steps {
                echo 'Docker imaji olusturuluyor...'
                bat "docker build -t %IMAGE%:%BUILD_NUMBER% -t %IMAGE%:latest ."
            }
        }

        stage('4) Login to DockerHub') {
            steps {
                echo 'DockerHub login...'
                bat 'echo %DOCKERHUB_PSW%| docker login -u %DOCKERHUB_USR% --password-stdin'
            }
        }

        stage('5) Push image to DockerHub') {
            steps {
                echo 'Imaj DockerHub a gonderiliyor...'
                bat "docker push %IMAGE%:%BUILD_NUMBER%"
                bat "docker push %IMAGE%:latest"
            }
        }

        stage('6) Deploy to Kubernetes (Minikube)') {
            steps {
                echo 'K8s deployment ve service uygulaniyor...'
                bat 'kubectl apply -f k8s/deployment.yaml'
                bat 'kubectl apply -f k8s/service.yaml'
                // Her build te yeni imaji cekmesi icin imaj etiketini guncelle
                bat "kubectl set image deployment/devops4-deployment devops4=%IMAGE%:%BUILD_NUMBER% --record"
                bat 'kubectl rollout status deployment/devops4-deployment --timeout=180s'
                bat 'kubectl get pods -o wide'
                bat 'kubectl get svc devops4-service'
            }
        }
    }

    post {
        always {
            echo 'Pipeline tamamlandi, docker logout yapiliyor.'
            bat 'docker logout'
        }
        success {
            echo 'BASARILI: Uygulama Kubernetes uzerinde calisiyor.'
        }
        failure {
            echo 'HATA: Pipeline basarisiz. Konsol ciktisini kontrol edin.'
        }
    }
}
