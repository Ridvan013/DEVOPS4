# SWE304 Proje 4 — SUNUM KOMUTLARI (en baştan, sırayla)

Hocanın ders akışına (W11 Jenkins, W12-W13 Kubernetes/Minikube) göre, ama
**Windows makinende çalıştığı doğrulanmış** komutlarla. Her adımı sırayla
çalıştır. Komutlar **PowerShell** içindir.

| Bilgi | Değer |
|---|---|
| GitHub repo | https://github.com/Ridvan013/DEVOPS4 |
| DockerHub imaj | `ridvandursun/devops4` |
| Jenkins | http://localhost:8081  (giriş: `ridvan` / `ridvan`) |
| Job adı | `devops4` |
| K8s deployment | `devops4-deployment` · service `devops4-service` (NodePort 30080) |
| Uygulama portu | 8080 (container) |

---

## 0) Sunum öncesi — Docker Desktop açık mı?

```powershell
docker ps
```
Hata verirse Docker Desktop'ı aç, simge yeşil olana kadar bekle.

---

## 1) Minikube'i başlat  (Hoca W12/W13: "minikube start")

```powershell
minikube start --driver=docker
minikube status
kubectl get nodes -o wide
```
Beklenen: node **Ready**, `192.168.49.2`, control-plane.
(Zaten çalışıyorsa `minikube start` "already running" der, sorun değil.)

---

## 2) Jenkins'i başlat  (kendi terminalinde, pencere AÇIK kalsın)

```powershell
$env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"
java -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081
```
Bu pencereyi kapatma. Tarayıcı: **http://localhost:8081** → `ridvan` / `ridvan`.
(Plugin'ler, `dockerhub` credential ve `devops4` job'u zaten kurulu.)

---

## 3) Pipeline'ı GitHub push ile TETİKLE  (PS1: push/merge tetikler)

Küçük bir değişiklik yapıp pushla — pollSCM ~1 dk içinde otomatik tetikler:

```powershell
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git commit --allow-empty -m "sunum: pipeline tetikle"
git push origin main
```

> Beklemeden göstermek istersen: Jenkins → `devops4` → **Build Now**.
> "Push ile otomatik tetiklendi"yi göstermek tam puan için daha iyi (PS1).

---

## 4) Pipeline'ın 6 stage'ini göster  (Jenkins'in 50 puanı)

Jenkins → **devops4** → çalışan build → **Console Output** ve **Stage View**.
Sırayla yeşil olmalı:

1. Clone from GitHub
2. Build JAR  (`BUILD SUCCESSFUL`)
3. Build Docker image
4. Login to DockerHub
5. Push image to DockerHub  (`digest: sha256:...`)
6. Deploy to Kubernetes  (`image updated`, `successfully rolled out`)

İstersen DockerHub'da imajı da göster: https://hub.docker.com/r/ridvandursun/devops4

---

## 5) Uygulama K8s'te çalışıyor mu?  (K8s'in 50 puanı — Hoca W13 akışı)

```powershell
kubectl get all
kubectl get pods -o wide
kubectl get svc devops4-service
kubectl get deployment devops4-deployment
```
Pod **Running 1/1** olmalı.

### Tarayıcıda aç (NodePort servis):

```powershell
minikube service devops4-service
```
Tarayıcı otomatik açılır → **"Hello from SWE304 DEVOPS4! ... Served by pod: ..."**

> Bu komut bir tünel açar ve **pencere açık kalmalı** (Windows+Docker driver).
> Alternatif (URL'yi gösterip curl ile):
> ```powershell
> minikube service devops4-service --url
> ```

---

## 6) 2 POD'a SCALE et ve hâlâ çalıştığını göster  (Proje madde 7)

```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl rollout status deployment/devops4-deployment
kubectl get pods -o wide
```
İki pod da **Running** olmalı.

### Yükün iki pod'a dağıldığını kanıtla (cluster içinden):

```powershell
kubectl run lbtest --image=curlimages/curl:8.11.0 --restart=Never --command -- sh -c "for i in 1 2 3 4 5 6 7 8 9 10 11 12; do curl -s http://devops4-service/; echo; done"
kubectl wait --for=jsonpath='{.status.phase}'=Succeeded pod/lbtest --timeout=120s
kubectl logs lbtest
kubectl delete pod lbtest
```
Çıktıda "Served by pod:" satırında **iki farklı pod adı** görünür → load
balancing çalışıyor, uygulama 2 pod ile hâlâ çalışıyor.

---

## 7) (İsteğe bağlı) Toparlama / tekrar

```powershell
kubectl scale deployment devops4-deployment --replicas=1   # eski hale
# Tamamen silmek istersen:
# kubectl delete -f k8s/service.yaml -f k8s/deployment.yaml
```

---

## Hızlı sorun giderme (sunumda takılırsan)

| Sorun | Komut / çözüm |
|---|---|
| `minikube` tanınmıyor | `& "C:\Users\RIDVAN\tools\minikube.exe" start` (veya yeni terminal aç) |
| Docker hatası | Docker Desktop açık mı? `docker ps` |
| Jenkins açılmıyor | Adım 2 komutu; şifre `ridvan/ridvan` |
| Pod `ImagePullBackOff` | Stage 5 bitti mi? `kubectl describe pod <ad>` |
| Pod `CrashLoopBackOff` | `kubectl logs deployment/devops4-deployment` |
| Servis tarayıcıda açılmıyor | `minikube service devops4-service` penceresi açık mı? |
| Pipeline tetiklenmedi | Jenkins → devops4 → Build Now; veya 1 dk bekle (pollSCM) |

---

### Tek bakışta sıra
`docker ps` → `minikube start` → Jenkins başlat → `git push` → 6 stage yeşil →
`kubectl get all` → `minikube service devops4-service` → `kubectl scale ... --replicas=2`
→ load-balance kanıtı.
