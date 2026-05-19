# 🎤 SUNUM — Sadece Komutlar (PC yeni açıldı)

Kural: **Tek bir PowerShell penceresi aç** (Başlat → `powershell` → Enter).
Aşağıdaki blokları **sırayla, olduğu gibi kopyala-yapıştır**. Jenkins ve
servis için yeni pencereleri **komutlar kendisi açar** — elle bir şey açmıyorsun,
GUI'ye dokunmuyorsun.

---

### 1) Docker Desktop'ı başlat ve hazır olmasını bekle
```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
while ($true) { docker ps *> $null; if ($?) { break }; Start-Sleep 5 }
Write-Output "Docker hazir."
```

### 2) Jenkins'i KENDİ penceresinde başlat (komut yeni pencere açar)
```powershell
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd
```

### 3) Jenkins ayağa kalkınca tarayıcıda aç
```powershell
while ($true) { try { Invoke-WebRequest http://localhost:8081/login -UseBasicParsing -TimeoutSec 5 | Out-Null; break } catch { Start-Sleep 5 } }
Start-Process "http://localhost:8081"
Write-Output "Jenkins acildi. Giris: ridvan / ridvan"
```

### 4) Minikube'i başlat ve doğrula
```powershell
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
& "C:\Users\RIDVAN\tools\minikube.exe" status
kubectl get nodes -o wide
```

### 5) Pipeline'ı GitHub push ile tetikle (PS1)
```powershell
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git commit --allow-empty -m "sunum: pipeline tetikle"
git push origin main
Write-Output "Push yapildi. ~1 dk icinde Jenkins/devops4 otomatik baslar (Started by an SCM change)."
```

### 6) Jenkins'te build'i aç (6 stage'i Console Output'ta göster)
```powershell
Start-Process "http://localhost:8081/job/devops4/"
```
Stage'ler: 1 Clone · 2 Build JAR · 3 Docker image · 4 DockerHub login · 5 Push · 6 K8s deploy → hepsi yeşil.

### 7) Uygulama K8s'te çalışıyor mu (K8s 50 puan)
```powershell
kubectl get all
kubectl get pods -o wide
kubectl get svc devops4-service
```

### 8) Uygulamayı tarayıcıda aç — servis tüneli KENDİ penceresinde
```powershell
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'
```
Açılan pencere tüneli tutar ve tarayıcıyı otomatik açar:
"Hello from SWE304 DEVOPS4! ... Served by pod: ...". Bu pencereyi kapatma.

### 9) 2 POD'a scale et + hâlâ çalışıyor (madde 7)
```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl rollout status deployment/devops4-deployment
kubectl get pods -o wide
```

### 10) Yükün iki pod'a dağıldığını kanıtla
```powershell
kubectl run lbtest --image=curlimages/curl:8.11.0 --restart=Never --command -- sh -c "for i in 1 2 3 4 5 6 7 8 9 10 11 12; do curl -s http://devops4-service/; echo; done"
kubectl wait --for=jsonpath='{.status.phase}'=Succeeded pod/lbtest --timeout=120s
kubectl logs lbtest
kubectl delete pod lbtest
```
"Served by pod:" satırında **iki farklı pod adı** → tamam.

---

## Sorun olursa (komutlar)

```powershell
# Jenkins penceresi kapandiysa tekrar baslat:
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd

# Pipeline tetiklenmediyse elle calistir (Build Now):
Start-Process "http://localhost:8081/job/devops4/"   # sayfada "Build Now"

# Minikube bozulduysa sifirla:
& "C:\Users\RIDVAN\tools\minikube.exe" delete
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker

# Pod loglari:
kubectl logs deployment/devops4-deployment

# Servis tarayicida acilmadiysa URL'yi al:
& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service --url
```

## Kapanış (opsiyonel)
```powershell
kubectl scale deployment devops4-deployment --replicas=1
# Jenkins ve servis pencerelerini kapat. Istersen: & "C:\Users\RIDVAN\tools\minikube.exe" stop
```

---

### Sıra (özet)
`1 Docker` → `2 Jenkins` → `3 tarayıcı` → `4 minikube start` → `5 git push`
→ `6 build aç` → `7 kubectl get all` → `8 servis aç` → `9 scale` → `10 kanıt`
