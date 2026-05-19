# 🎤 SUNUM REHBERİ — Bilgisayarı Yeni Açtın, Baştan Sona

Bu dosya: **PC'yi yeni açtın**, hiçbir şey çalışmıyor. Sunuma kadar tek tek ne
yapacağın, **hangi terminalde**, ne zaman **yeni terminal** açacağın yazılı.
Komutlar **PowerShell** içindir.

---

## 🧠 Genel mantık: 1 program + 3 ayrı terminal

| # | Ne | Açıklama | Kapatma |
|---|----|----------|---------|
| 🐳 | **Docker Desktop** | Program (terminal değil), Başlat menüsünden | Açık kalmalı |
| 🟦 **Terminal 1** | **JENKINS** | Jenkins sunucusu burada çalışır | **KAPATMA**, küçült |
| 🟩 **Terminal 2** | **KOMUT** | minikube, kubectl, git — ana çalışma terminali | İş bitince kapanabilir |
| 🟨 **Terminal 3** | **SERVİS TÜNELİ** | `minikube service` tüneli (demo anında) | Demo boyunca **KAPATMA** |

**PowerShell nasıl açılır:** Başlat (Windows tuşu) → `powershell` yaz → Enter.
**"Yeni terminal aç" =** yeni bir PowerShell penceresi aç.

> Sırayla git. Her adımın başında köşeli parantez hangi terminal olduğunu söyler.

---

## ADIM 0 — 🐳 Docker Desktop'ı başlat  (program, terminal DEĞİL)

1. Başlat menüsü → **Docker Desktop** → tıkla, aç.
2. Sağ alttaki balina simgesi **sabitlenene** kadar bekle (~1–2 dk, animasyon durmalı).
3. **Kontrol:** Yeni bir PowerShell aç, şunu yaz:
   ```powershell
   docker ps
   ```
   Hatasız bir liste (boş olabilir) dönerse Docker hazır. Hata verirse biraz
   daha bekle. Bu kontrol terminalini kapatabilirsin.

> Docker çalışmadan Minikube **başlamaz**. Bu adımı atlama.

---

## ADIM 1 — 🟦 [Terminal 1: JENKINS] Jenkins'i başlat

1. **Yeni PowerShell penceresi aç** → bu **Terminal 1 (JENKINS)**.
2. Şu iki satırı yapıştır, Enter:
   ```powershell
   $env:JENKINS_HOME="C:\Users\RIDVAN\.jenkins"
   java -jar "C:\Users\RIDVAN\tools\jenkins.war" --httpPort=8081
   ```
3. Akan logda **`Jenkins is fully up and running`** yazısını görünce hazırdır.
4. ⚠️ **Bu pencereyi KAPATMA.** Sadece küçült (minimize). Kapatırsan Jenkins durur.
5. Tarayıcı aç → **http://localhost:8081** → giriş:
   **kullanıcı `ridvan` / şifre `ridvan`**

> Pluginler, `dockerhub` credential ve `devops4` job'u zaten kurulu — sihirbaz çıkmaz.

---

## ADIM 2 — 🟩 [Terminal 2: KOMUT] Minikube'i başlat

1. **Yeni PowerShell penceresi aç** → bu **Terminal 2 (KOMUT)**. Bundan sonraki
   tüm `kubectl` / `git` / `minikube` komutları **hep burada**.
2. ```powershell
   minikube start --driver=docker
   ```
   "Done! kubectl is now configured..." görünmeli (ilk açılışta ~1 dk).
3. Doğrula:
   ```powershell
   minikube status
   kubectl get nodes -o wide
   ```
   Node **Ready** olmalı (`192.168.49.2`, control-plane).

---

## ADIM 3 — 🟩 [Terminal 2] Pipeline'ı GitHub push ile TETİKLE  (PS1)

Terminal 2'de:
```powershell
cd C:\Users\RIDVAN\Desktop\DEVOPS4
git commit --allow-empty -m "sunum: pipeline tetikle"
git push origin main
```
- Token sormaz (kayıtlı). ~1 dakika içinde Jenkins **kendiliğinden** başlar.
- Jenkins'te build'in tetikleyicisi: **"Started by an SCM change"** = push tetikledi.

> Beklemek istemezsen: tarayıcıda Jenkins → `devops4` → **Build Now**.
> Ama "push otomatik tetikledi" demek tam puan için daha iyi (PS1).

---

## ADIM 4 — 🌐 [Tarayıcı] 6 STAGE'i göster  (Jenkins'in 50 puanı)

Tarayıcı: Jenkins → **devops4** → çalışan/biten build → **Console Output**
(veya Stage görünümü). Sırayla ve **yeşil** olmalı:

1. **Clone from GitHub**
2. **Build JAR** — `BUILD SUCCESSFUL`
3. **Build Docker image**
4. **Login to DockerHub**
5. **Push image to DockerHub** — `digest: sha256:...`
6. **Deploy to Kubernetes** — `image updated`, `successfully rolled out`

İstersen DockerHub'da imajı da göster: https://hub.docker.com/r/ridvandursun/devops4

---

## ADIM 5 — 🟩 [Terminal 2] Uygulama K8s'te çalışıyor mu?  (K8s'in 50 puanı)

Terminal 2'de:
```powershell
kubectl get all
kubectl get pods -o wide
kubectl get svc devops4-service
```
Pod **Running 1/1** olmalı.

---

## ADIM 6 — 🟨 [Terminal 3: SERVİS] Tarayıcıda uygulamayı aç

1. **Yeni PowerShell penceresi aç** → bu **Terminal 3 (SERVİS)**.
2. ```powershell
   minikube service devops4-service
   ```
3. Tarayıcı **otomatik açılır** →
   **"Hello from SWE304 DEVOPS4! ... Served by pod: ..."**
4. ⚠️ Bu pencere bir **tünel**; demo bitene kadar **KAPATMA**.

> Alternatif (URL'yi gösterip Terminal 2'den test): `minikube service devops4-service --url`

---

## ADIM 7 — 🟩 [Terminal 2] 2 POD'a SCALE et + hâlâ çalışıyor  (madde 7)

Terminal 2'de:
```powershell
kubectl scale deployment devops4-deployment --replicas=2
kubectl rollout status deployment/devops4-deployment
kubectl get pods -o wide
```
İki pod da **Running** olmalı.

Yükün iki pod'a dağıldığını kanıtla (cluster içinden):
```powershell
kubectl run lbtest --image=curlimages/curl:8.11.0 --restart=Never --command -- sh -c "for i in 1 2 3 4 5 6 7 8 9 10 11 12; do curl -s http://devops4-service/; echo; done"
kubectl wait --for=jsonpath='{.status.phase}'=Succeeded pod/lbtest --timeout=120s
kubectl logs lbtest
kubectl delete pod lbtest
```
Çıktıda "Served by pod:" satırında **iki farklı pod adı** görünür → tamam.

---

## 🗺️ Hangi terminal neydi? (özet)

```
🐳 Docker Desktop  → program, Başlat menüsü, açık kalır
🟦 Terminal 1      → Jenkins   (java -jar jenkins.war ...)  → KAPATMA
🟩 Terminal 2      → Komut     (minikube/kubectl/git)       → ana terminal
🟨 Terminal 3      → Servis    (minikube service ...)       → demo boyu KAPATMA
```

**Akış:** Docker Desktop → T1 Jenkins → T2 `minikube start` → T2 `git push`
→ Tarayıcı 6 stage → T2 `kubectl get all` → T3 `minikube service` → T2 `scale`.

---

## 🆘 Sorun giderme (sunumda takılırsan)

| Sorun | Çözüm |
|---|---|
| `docker ps` hata | Docker Desktop açık mı? Balina simgesi sabit mi? Bekle. |
| `minikube` tanınmıyor | Terminali kapat-yeniden aç. Olmazsa: `& "C:\Users\RIDVAN\tools\minikube.exe" start --driver=docker` |
| `kubectl` tanınmıyor | Yeni PowerShell aç (PATH yenilensin) |
| Jenkins açılmıyor (8081) | Terminal 1 açık mı? Log'da "fully up" var mı? Şifre `ridvan/ridvan` |
| Pipeline tetiklenmedi | 1 dk bekle (pollSCM) ya da Jenkins → devops4 → **Build Now** |
| Pod `ImagePullBackOff` | Stage 5 bitti mi? `kubectl describe pod <ad>` |
| Pod `CrashLoopBackOff` | `kubectl logs deployment/devops4-deployment` |
| Tarayıcı açılmadı (Adım 6) | Terminal 3 penceresi açık mı? `minikube service devops4-service --url` ile URL'yi al, elle aç |
| Her şeyi sıfırla | `minikube delete` → `minikube start --driver=docker` → Adım 3'ten devam |

---

### Kapanışta (opsiyonel)
```powershell
kubectl scale deployment devops4-deployment --replicas=1   # T2: eski hale
```
Terminal 1, 3'ü kapat (Jenkins/tünel durur). Minikube'i durdurmak istersen: `minikube stop`.
