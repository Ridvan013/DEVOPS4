# SWE304 Project 4 - Jenkins CI/CD + Kubernetes (Minikube)

Basit bir Spring Boot uygulamasini Jenkins pipeline ile build edip Docker imaji
olusturan, DockerHub'a pushlayan ve yerel Minikube Kubernetes kumesine deploy
eden proje.

- **Uygulama:** Spring Boot (Java 25, Gradle), tek endpoint `GET /`, veritabani yok.
- **Imaj:** `ridvandursun/devops4`
- **GitHub:** https://github.com/Ridvan013/DEVOPS4
- **CI/CD:** `Jenkinsfile` (6 stage) — GitHub push/merge ile tetiklenir.
- **K8s:** `k8s/deployment.yaml` + `k8s/service.yaml` (NodePort 30080)

## Dizin yapisi

```
.
├── Jenkinsfile              # 6 stage'li CI/CD pipeline
├── Dockerfile               # Hazir jar'i jre imajina paketler
├── build.gradle             # Sadece spring-boot-starter-webmvc
├── k8s/
│   ├── deployment.yaml      # 1 replica (sunumda 2'ye scale edilir)
│   └── service.yaml         # NodePort 30080
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   └── controller/HelloController.java   # GET /  (tek endpoint)
└── KURULUM.md               # ADIM ADIM kurulum + sunum rehberi
```

## Hizli baslangic

Tum kurulum ve sunum adimlari **[KURULUM.md](KURULUM.md)** dosyasinda.

```bash
# yerel test
gradlew.bat bootRun
# tarayici: http://localhost:8080/
```

## Pipeline stage'leri (Jenkinsfile)

1. GitHub'dan klonla
2. JAR build et (Gradle)
3. Docker imaji olustur
4. DockerHub login
5. Imaji DockerHub'a pushla
6. K8s deployment + service uygula (Minikube)
