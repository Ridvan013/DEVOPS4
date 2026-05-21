# Sunum Komutları

## Sunum öncesi hazırlık

```powershell
# 1. Docker Desktop'ı aç (manuel)

# 2. Jenkins'i başlat
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd
# → http://localhost:8081   (giris: ridvan / ridvan)

# 3. Minikube'u başlat
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
```

## Podları ve servisleri göster

```powershell
kubectl get pods
kubectl get services
```

## Uygulamayı aç

```powershell
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'
# Tarayıcıda / sayfası açılır
```

## GitHub push → Jenkins otomatik tetiklenme gösterimi

Önce `HelloController.java` dosyasını şöyle güncelle — mesaja `(v2)` ekle:

```java
return "Hello from SWE304 DEVOPS4 (v2)! Spring Boot running on Kubernetes. Served by pod: " + pod;
```

Sonra push et:

```powershell
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git add src/main/java/com/example/demo/controller/HelloController.java
git commit -m "Add v2 marker to hello message"
git push origin main
```

Jenkins ~1 dakika içinde otomatik build başlatır. Build bittikten sonra
tarayıcıyı yenile — `(v2)` görünür.

## 2 pod'a scale et

```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl get pods
# 2x Running görünmeli
```

```powershell
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'
# / hâlâ çalışıyor
```

---

Sunum öncesi başlatma:

```powershell
# Jenkins'i başlat
$jcmd = '$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"; & "C:\Users\RIDVAN\.jdks\openjdk-25.0.2\bin\java.exe" -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081'
Start-Process powershell -ArgumentList '-NoExit','-Command',$jcmd

# Minikube'u başlat
& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker
```

Sunum sırasında gösterilecek komutlar:

```powershell
# Pod durumunu göster
kubectl get pods

# Servis ve deployment'ı göster
kubectl get deployments
kubectl get services

# 2 pod'a ölçekle (canlı göster)
kubectl scale deployment devops4-deployment --replicas=2
kubectl get pods

# Uygulamayı aç (tünel açar, URL verir)
Start-Process powershell -ArgumentList '-NoExit','-Command','& "C:\Users\RIDVAN\tools\minikube.exe" service devops4-service'
```

Push demo için HelloController'a küçük ekleme (canlı push tetiklemek için):

`src/main/java/com/example/demo/controller/HelloController.java` dosyasında mesaja `(v2)` ekle:

```java
return "Hello from SWE304 DEVOPS4 (v2)! Spring Boot running on Kubernetes. Served by pod: " + pod;
```

Sonra commit + push → Jenkins otomatik tetiklenir → pipeline çalışır.

Test komutu (minikube service'in verdiği URL'ye göre):

```powershell
curl http://127.0.0.1:<PORT>/
```
