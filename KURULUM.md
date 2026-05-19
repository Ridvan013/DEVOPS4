# SWE304 Project 4 — Kurulum ve Sunum Rehberi (Windows 11)

Bu rehber projeyi sifirdan calistirip hocaya sunum yapacagin sirayla yazildi.
Komutlar **PowerShell** icindir. Her adimi sirayla uygula.

> Ozet hedef: GitHub'a push -> Jenkins pipeline tetiklenir -> jar + Docker imaji
> -> DockerHub -> Minikube'e deploy -> uygulama K8s'te calisir -> 2 pod'a scale.

---

## ✅ ASISTAN TARAFINDAN ZATEN YAPILANLAR (durum)

Bu adimlar senin icin bir kez yapildi, dogrulandi:

| Is | Durum | Detay |
|----|-------|-------|
| Minikube kuruldu | ✅ | `C:\Users\RIDVAN\tools\minikube.exe` + kullanici PATH'ine eklendi |
| Minikube cluster | ✅ Running | `minikube status` -> host/kubelet/apiserver Running |
| JAR build | ✅ | `gradlew bootJar` -> `build/libs/devops4-0.0.1-SNAPSHOT.jar` |
| Docker imaji | ✅ | `ridvandursun/devops4:latest` ve `:1` |
| DockerHub push | ✅ | hub.docker.com/r/ridvandursun/devops4 (public) |
| K8s deploy | ✅ | `kubectl apply` -> deployment + service uygulandi |
| Uygulama K8s'te | ✅ | HTTP 200, "Served by pod: devops4-..." |
| 2 pod'a scale | ✅ | 2 pod Running, yuk iki pod'a dagiliyor (kanitlandi) |
| 2 pod'a scale | ✅ | 2 pod Running, yuk iki pod'a dagiliyor (kanitlandi) |
| Jenkins kuruldu | ✅ | `C:\Users\RIDVAN\tools\jenkins.war`, giris `ridvan/ridvan` |
| Jenkins pluginleri | ✅ | git, github, pipeline, docker-workflow, credentials-binding |
| DockerHub credential | ✅ | Jenkins id: `dockerhub` (ridvandursun + token) |
| Kod GitHub'da | ✅ | https://github.com/Ridvan013/DEVOPS4 (main) |
| `devops4` pipeline job | ✅ | Pipeline from SCM -> Jenkinsfile, pollSCM tetikleyici |
| **Pipeline 6 stage** | ✅ **SUCCESS** | Build #3 -> imaj `ridvandursun/devops4:3` K8s'e deploy edildi |
| Jenkins calisiyor | ⚠️ | http://localhost:8081 — surec asistan oturumuna bagli, kapaninca durur (bkz. asagi) |

**Her sey kurulu ve pipeline calisiyor.** Senin yapman gerekenler sadece:
sunum oncesi Jenkins'i kendi terminalinde baslatmak (asagi), sunumda push'la
tetikleyip 6 stage'i + K8s'i gostermek, ve 2 pod'a scale demosu (Adim 7).

### ⚠️ ÖNEMLİ — Jenkins'i kendin baslat

Asistanin baslattigi Jenkins, oturum kapaninca durur. Sunumdan once **kendi
terminalinde** sunu calistir ve pencereyi acik birak:

```powershell
$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"
java -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081
```

Ayni `JENKINS_HOME` kullanildigi icin kurdugun her sey (kullanici, plugin,
job) korunur.

**Jenkins giris bilgileri:** kullanici **`ridvan`** / sifre **`ridvan`**

Setup sihirbazi atlandi (init script ile). Giris yapinca direkt panele
duser; ilk acilis token'ina / "Install suggested plugins" adimina gerek yok.
Bu hesap `C:\Users\RIDVAN\.jenkins\init.groovy.d\basic-security.groovy`
tarafindan her aciliista garanti edilir. (Repodaki kopya:
[jenkins/basic-security.groovy](jenkins/basic-security.groovy) — baska
makinede `%JENKINS_HOME%\init.groovy.d\` altina koyman yeterli.)

> Minikube cluster bilgisayar yeniden baslayinca durur; tekrar `minikube start`
> de. Java 25 ile Jenkins calisiyor (admin panelinde "unsupported Java" uyarisi
> cikabilir, gormezden gelinebilir).

---

## 0. On Gereksinimler

| Arac | Durum | Not |
|------|-------|-----|
| Java 25 (JDK) | Kurulu olmali | `java -version` |
| Docker Desktop | Kurulu + calisiyor olmali | Minikube driver'i da bu olacak |
| Git | Kurulu | `git --version` |
| kubectl | Kurulu | `kubectl version --client` |
| Minikube | ✅ Kuruldu (Adim 1 referans) | `C:\Users\RIDVAN\tools` |
| Jenkins | ✅ Indirildi (Adim 3 referans) | `C:\Users\RIDVAN\tools\jenkins.war` |

Docker Desktop **acik** ve calisir durumda olmali (sag alt simge yesil).

---

## 1. Minikube Kurulumu

> ℹ️ Bu adim **zaten yapildi** (binary `C:\Users\RIDVAN\tools`, PATH'e eklendi,
> cluster Running). Asagisi yeniden kurulum / baska makine / bilgi icindir.
> Yeniden baslatmak yeterli: `minikube start`

### 1.1 Kurulum (Chocolatey ile — en kolay)

Yonetici (Administrator) PowerShell ac:

```powershell
choco install minikube -y
```

Chocolatey yoksa, manuel:

```powershell
New-Item -ItemType Directory -Force "C:\minikube"
Invoke-WebRequest -OutFile "C:\minikube\minikube.exe" `
  -Uri "https://github.com/kubernetes/minikube/releases/latest/download/minikube-windows-amd64.exe"
# C:\minikube'u PATH'e ekle (Sistem Ortam Degiskenleri) ve PowerShell'i yeniden ac
```

### 1.2 Baslat

```powershell
minikube start --driver=docker
minikube status
kubectl get nodes
```

`kubectl get nodes` ciktisinda node `Ready` gorunmeli. (Proje gereksinimi:
"`$ minikube start` ile uygulamayi calistirmaya hazir olmali".)

> Minikube'i her bilgisayar acildiginda yeniden baslatman gerekir:
> `minikube start`

---

## 2. Uygulamayi Yerelde Test Et (opsiyonel ama onerilir)

```powershell
.\gradlew.bat bootRun
```

Tarayicidan **http://localhost:8080/** -> "Hello from SWE304 DEVOPS4..."
gormelisin. `Ctrl+C` ile durdur.

---

## 3. Jenkins Kurulumu

> ℹ️ `jenkins.war` **zaten indirildi** (`C:\Users\RIDVAN\tools\jenkins.war`) ve
> bir kez calistirildi. Yeniden baslatmak icin yukaridaki "Jenkins'i kendin
> baslat" kutusuna bak. Asagidaki 3.2–3.4 (plugin, credential) **senin UI'da
> yapman gereken** adimlar.

### 3.1 Jenkins'i kur

En basit yontem — `jenkins.war` (kendi kullanicinla calisir, kubectl/minikube
erisimi otomatik dogru olur):

1. https://www.jenkins.io/download/ adresinden **Generic Java package (.war)**
   indir -> `jenkins.war`.
2. PowerShell'de (kendi kullanicinla, Administrator DEGIL):

```powershell
java -jar jenkins.war --httpPort=8081
```

> Port 8081 sectik cunku 8080 Spring Boot icin. Jenkins acikken bu pencere
> kapanmamali.

3. Tarayici: **http://localhost:8081** -> giris: **`ridvan` / `ridvan`**.
   (Setup sihirbazi init script ile atlandigi icin token/plugin sihirbazi
   cikmaz; dogrudan panele duser.)
4. Plugin sihirbazi cikmadigi icin gerekli 5 plugin'i **3.2'deki gibi elle**
   kurman gerekir (asagi).

> Windows Installer (.msi) ile de kurabilirsin ama o zaman Jenkins "SYSTEM"
> servis kullanicisiyla calisir ve kubectl/minikube/docker'i goremeyebilir.
> Bu durumda servisi kendi kullanicinla calistir (Adim 6'daki nota bak).
> Sunum icin **`jenkins.war` yontemi en sorunsuzudur.**

### 3.2 Gerekli Plugin'ler

Sihirbaz atlandigi icin **hicbir plugin onceden kurulu degil**. **Manage
Jenkins -> Plugins -> Available plugins** kisminda sunlarin hepsini ara ve kur:

- **Git plugin**
- **GitHub plugin**
- **Pipeline** (veya "Pipeline: Groovy" / Pipeline: Aggregator)
- **Docker Pipeline**
- **Credentials Binding plugin**

"Install after restart" / "Restart Jenkins when installation is complete" sec.
Jenkins kapaninca **kendi terminalinde** tekrar baslat (yukaridaki komut) —
ayni `JENKINS_HOME` oldugu icin pluginler korunur.

### 3.3 DockerHub Kimligi Ekle

**Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials:**

- Kind: **Username with password**
- Username: `ridvandursun`  (DockerHub kullanici adin)
- Password: DockerHub **Access Token** (Hub -> Account Settings -> Security ->
  New Access Token) ya da sifren
- **ID: `dockerhub`**  ← Jenkinsfile bu ID'yi kullaniyor, AYNEN yaz
- Description: DockerHub

### 3.4 Araclarin Jenkins Tarafindan Gorulebildigini Dogrula

**Manage Jenkins -> Tools** gerekmiyor; ama `docker`, `kubectl`, `git`
PATH'te olmali. Test icin yeni bir Pipeline yerine, su an actigin PowerShell'de
sunlarin calistigini gor (zaten calisiyor):

```powershell
docker --version
kubectl version --client
git --version
```

Jenkins `jenkins.war`'i bu kullaniciyla calistirdigin icin ayni PATH'i gorur.

---

## 4. GitHub Reposunu Hazirla ve Push Et

Repo: **https://github.com/Ridvan013/DEVOPS4** (bos olusturulmus olmali —
README/gitignore ekleme, bos ac).

Bu klasorde (`c:\Users\RIDVAN\Desktop\DEVOPS4`):

```powershell
git init
git branch -M main
git add .
git commit -m "SWE304 Project 4: Spring Boot + Jenkins CI/CD + K8s"
git remote add origin https://github.com/Ridvan013/DEVOPS4.git
git push -u origin main
```

> GitHub kullanici/parola sorarsa parola yerine **Personal Access Token**
> kullan (GitHub -> Settings -> Developer settings -> Tokens).

> `_devops3_ref/` ve `pro4.pdf` `.gitignore` ile haric tutuldu (Adim sonunda
> kontrol et: `git status`).

---

## 5. Jenkins Pipeline Job'u Olustur

1. Jenkins ana sayfa -> **New Item** -> isim: `devops4` -> **Pipeline** -> OK.
2. **Build Triggers** bolumu:
   - ☑ **GitHub hook trigger for GITScm polling**  (webhook ile tetikleme — PS1)
3. **Pipeline** bolumu:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/Ridvan013/DEVOPS4.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. **Save**.

İlk calistirmayi elle test et: **Build Now**. Tum 6 stage yesil olmali.

---

## 6. GitHub Push/Merge ile Otomatik Tetikleme (PS1)

Jenkins **yerelde** (localhost) calistigi icin GitHub onun adresine direkt
ulasamaz. Iki yol var:

### Yol A — Webhook + ngrok (gercek "push ile tetikleme", onerilen)

1. https://ngrok.com'dan ngrok indir, kaydol, authtoken'i ayarla.
2. Yeni PowerShell penceresinde:

```powershell
ngrok http 8081
```

3. Cikan `https://xxxx.ngrok-free.app` adresini kopyala.
4. GitHub repo -> **Settings -> Webhooks -> Add webhook**:
   - Payload URL: `https://xxxx.ngrok-free.app/github-webhook/`
   - Content type: `application/json`
   - Event: **Just the push event**
   - Add webhook (yesil tik gelmeli).
5. Artik koda degisiklik yapip `git push` yapinca pipeline kendiliginden
   calisir. Merge (PR kapanisi) de push uretir, ayni sekilde tetikler.

### Yol B — Polling (webhook olmadan, yedek)

ngrok kullanamiyorsan `Jenkinsfile` icindeki su satirlari degistir:

```groovy
triggers {
    // githubPush()
    pollSCM('* * * * *')   // her dakika GitHub'i kontrol eder
}
```

Push'tan sonra ~1 dk icinde pipeline calisir. Bu da "GitHub guncellemesi ile
tetikleniyor" kapsamindadir.

> Sunumda Yol A'yi gostermek tam puan icin daha iyidir.

---

## 7. Sunum (Demo) Adimlari — Hocaya Goster

### 7.1 Pipeline calisiyor (50 puan)

1. `minikube start` (calisiyor olsun), `jenkins.war` calisiyor olsun.
2. Kucuk bir degisiklik yap (orn. `HelloController.java` icindeki mesaja
   `v2` ekle), kaydet:

```powershell
git add .
git commit -m "demo: trigger pipeline"
git push
```

3. Jenkins -> `devops4` job -> build kendiliginden basliyor. **Stage View**'da
   6 stage'in de yesil oldugunu goster:
   1) Clone 2) Build JAR 3) Docker image 4) DockerHub login
   5) Push image 6) Deploy to Kubernetes
4. (Madde 5) "K8s build stages run successfully" — Stage 6 konsol ciktisinda
   `deployment configured`, `rollout status ... successfully rolled out`
   satirlarini goster.

### 7.2 Uygulama K8s'te calisiyor (50 puan)

```powershell
kubectl get pods
kubectl get svc
minikube service devops4-service --url
```

Cikan URL'yi tarayicida ac -> "Hello from SWE304 DEVOPS4 ... Served by pod:
devops4-deployment-xxxx" goruyorsun. (Madde 6 — uygulama beklendigi gibi
calisiyor.)

### 7.3 2 Pod'a Scale Et (Madde 7)

```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl get pods -o wide
```

İki pod da `Running` olmali. URL'yi birkac kez yenile; "Served by pod: ..."
satirinda farkli pod isimleri gorulur (yuk iki pod'a dagiliyor). Hala
calistigini boylece gosterirsin.

> Alternatif: `k8s/deployment.yaml` icinde `replicas: 2` yapip tekrar push
> edersen pipeline 2 pod ile deploy eder.

---

## 8. Faydali Komutlar / Sorun Giderme

```powershell
# Minikube
minikube status
minikube dashboard            # tarayicida K8s panosu
minikube delete; minikube start --driver=docker   # sifirdan

# Kubernetes
kubectl get all
kubectl describe deployment devops4-deployment
kubectl logs deployment/devops4-deployment
kubectl rollout restart deployment/devops4-deployment

# Docker
docker images | findstr devops4
docker login -u ridvandursun
```

**Sik karsilasilan sorunlar:**

- *Stage 6'da `kubectl` bulunamadi:* Jenkins'i `jenkins.war` ile KENDI
  kullanicinla calistir. (.msi servis kullanicisi PATH'i gormez.)
- *Pod `ImagePullBackOff`:* DockerHub reposu **public** olmali, ya da imaj
  henuz pushlanmamis. Stage 5'in basarili bittigini ve
  `hub.docker.com/r/ridvandursun/devops4` altinda imajin gorundugunu kontrol et.
- *Pod `CrashLoopBackOff`:* `kubectl logs <pod>` ile uygulama hatasina bak.
- *Webhook tetiklemiyor:* GitHub -> Settings -> Webhooks -> Recent Deliveries
  bolumunde 200 cevap geldi mi? ngrok penceresi acik mi? Payload URL sonunda
  `/github-webhook/` (sondaki `/` dahil) var mi?
- *`minikube start` Docker hatasi:* Docker Desktop acik mi? `docker ps` calisiyor mu?
- *Port 8080 cakismasi:* Jenkins 8081'de; yerel `bootRun` testini kapat.

---

Hazirsin. Sirasiyla: **1 (Minikube) -> 3 (Jenkins) -> 4 (GitHub push) ->
5 (Job) -> 6 (Webhook) -> 7 (Sunum).**
