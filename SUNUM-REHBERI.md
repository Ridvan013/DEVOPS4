# SUNUM — Komutlar

Tek bir PowerShell aç. Aşağıdaki blokları sırayla, olduğu gibi yapıştır.

---

## Hızlı: tüm komutlar tek blokta

```powershell
# 1) Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
while ($true) { docker ps *> $null; if ($?) { break }; Start-Sleep 5 }

# 2) Jenkins (kendi penceresinde)
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd

# 3) Jenkins'i tarayicida ac
while ($true) { try { Invoke-WebRequest http://localhost:8081/login -UseBasicParsing -TimeoutSec 5 | Out-Null; break } catch { Start-Sleep 5 } }
Start-Process "http://localhost:8081"

# 4) Minikube
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
& "C:\Users\RIDVAN\tools\minikube.exe" status
kubectl get nodes -o wide

# 5) Pipeline'i push ile tetikle
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git commit --allow-empty -m "sunum: pipeline tetikle"
git push origin main

# 6) Build sayfasini ac
Start-Process "http://localhost:8081/job/devops4/"

# 7) K8s dogrula
kubectl get all
kubectl get pods -o wide
kubectl get svc devops4-service

# 8) Servis tunelini ac (kendi penceresinde, tarayici otomatik acilir)
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'

# 9) 2 pod'a scale
kubectl scale deployment devops4-deployment --replicas=2
kubectl rollout status deployment/devops4-deployment
kubectl get pods -o wide

# 10) Load-balance kaniti
kubectl run lbtest --image=curlimages/curl:8.11.0 --restart=Never --command -- sh -c "for i in 1 2 3 4 5 6 7 8 9 10 11 12; do curl -s http://devops4-service/; echo; done"
kubectl wait --for=jsonpath='{.status.phase}'=Succeeded pod/lbtest --timeout=120s
kubectl logs lbtest
kubectl delete pod lbtest
```

---

## Adım adım

### 1) Docker Desktop

```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
while ($true) { docker ps *> $null; if ($?) { break }; Start-Sleep 5 }
```

### 2) Jenkins

```powershell
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd
```

### 3) Jenkins tarayıcı

```powershell
while ($true) { try { Invoke-WebRequest http://localhost:8081/login -UseBasicParsing -TimeoutSec 5 | Out-Null; break } catch { Start-Sleep 5 } }
Start-Process "http://localhost:8081"
```

### 4) Minikube

```powershell
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
& "C:\Users\RIDVAN\tools\minikube.exe" status
kubectl get nodes -o wide
```

### 5) Pipeline tetikle

```powershell
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git commit --allow-empty -m "sunum: pipeline tetikle"
git push origin main
```

### 6) Build sayfası

```powershell
Start-Process "http://localhost:8081/job/devops4/"
```

### 7) K8s

```powershell
kubectl get all
kubectl get pods -o wide
kubectl get svc devops4-service
```

### 8) Servis tüneli

```powershell
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'
```

### 9) Scale

```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl rollout status deployment/devops4-deployment
kubectl get pods -o wide
```

### 10) Kanıt

```powershell
kubectl run lbtest --image=curlimages/curl:8.11.0 --restart=Never --command -- sh -c "for i in 1 2 3 4 5 6 7 8 9 10 11 12; do curl -s http://devops4-service/; echo; done"
kubectl wait --for=jsonpath='{.status.phase}'=Succeeded pod/lbtest --timeout=120s
kubectl logs lbtest
kubectl delete pod lbtest
```

---

## Sorun olursa

```powershell
# Jenkins yeniden baslat
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd
```

```powershell
# Build'i elle tetikle (sayfada Build Now)
Start-Process "http://localhost:8081/job/devops4/"
```

```powershell
# Minikube sifirla
& "C:\Users\RIDVAN\tools\minikube.exe" delete
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
```

```powershell
# Pod loglari
kubectl logs deployment/devops4-deployment
```

```powershell
# Servis URL
& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service --url
```

---

## Kapanış

```powershell
kubectl scale deployment devops4-deployment --replicas=1
& "C:\Users\RIDVAN\tools\minikube.exe" stop
```

---

## Notlar

- Jenkins giriş: `ridvan` / `ridvan`
- 6 stage: 1 Clone · 2 Build JAR · 3 Docker image · 4 DockerHub login · 5 Push · 6 K8s deploy
- Build tetikleyici: "Started by an SCM change" (PS1)
- Jenkins penceresi ve servis tüneli penceresi demo bitene kadar açık kalır
