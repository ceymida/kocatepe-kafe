# Kocatepe Kafe – Personel Vardiya ve Yoğunluk Planlama Paneli

Kafe personeli için haftalık vardiya planlama, müsaitlik, takas talebi, yoğunluk analizi ve
personel ihtiyacı önerisi sunan Spring Boot uygulaması.

> **ÖNEMLİ – ÖRNEK VERİ:** Projede gerçek POS/operasyon verisi yoktur. Personel, müsaitlik,
> vardiya ve yoğunluk verileri tamamen **sentetik (uydurma) örnek veridir**. Arayüzde de
> "ÖRNEK VERİ" etiketiyle belirtilir. Gerçek veriye geçiş adımları aşağıdadır.

## Teknoloji

- Java 21
- Spring Boot 3.3
- Spring Data JPA / Hibernate
- Thymeleaf (sunucu taraflı arayüz, responsive)
- Spring Security (form login, BCrypt)
- H2 (dosya tabanlı, geliştirme)
- PostgreSQL (geçiş için hazır bağımlılık)
- Maven

Ekstra frontend kütüphanesi kullanılmamıştır; arayüz sade HTML + CSS'tir.

## Hızlı Başlangıç

Gereksinimler: Java 21 ve Maven 3.9+.

```bash
cd kocatepe-kafe
mvn spring-boot:run
```

Uygulama: <http://localhost:8080>

İlk açılışta:

1. 12 örnek personel + kullanıcı hesapları oluşturulur.
2. Son 12 hafta için sentetik yoğunluk verisi üretilir (yaklaşık 1.176 kayıt).
3. Veriler `./data/kocatepe.mv.db` H2 dosyasında tutulur.

H2 konsolu: <http://localhost:8080/h2-console>  
JDBC URL: `jdbc:h2:file:./data/kocatepe` – kullanıcı `sa`, şifre boş.

Sıfırdan başlamak için uygulamayı durdurup `data/` klasörünü silin.

## Hesaplar

| Rol | Kullanıcı adı | Şifre |
|---|---|---|
| Yönetici | `admin` | `admin` |
| Çalışan | `emre.kaya@kocatepe.com` | `1234` |
| Çalışan | `selin.aydin@kocatepe.com` | `1234` |
| Çalışan | `burak.demir@kocatepe.com` | `1234` |
| Çalışan | `zeynep.yilmaz@kocatepe.com` | `1234` |
| Çalışan | `can.ozturk@kocatepe.com` | `1234` |
| Çalışan | `elif.arslan@kocatepe.com` | `1234` |
| Çalışan | `mert.celik@kocatepe.com` | `1234` |
| Çalışan | `derya.sahin@kocatepe.com` | `1234` |
| Çalışan | `ahmet.koc@kocatepe.com` | `1234` |
| Çalışan | `ceren.polat@kocatepe.com` | `1234` |
| Çalışan | `oguz.tekin@kocatepe.com` | `1234` |
| Çalışan | `melis.erdem@kocatepe.com` | `1234` |

Yönetici panelinden yeni personel eklenirse kullanıcı adı otomatik üretilir
(`ad.soyad@kocatepe.com`, çakışmada `ad.soyad2@kocatepe.com`) ve varsayılan şifre `1234` olur.
Tüm şifreler BCrypt ile saklanır. Bu hesaplar yalnızca geliştirme içindir.

## Özellikler

### Rol bazlı erişim

- **YÖNETİCİ:** personel yönetimi, tüm vardiyalar, takas onayı, yoğunluk/tahmin, CSV import.
- **ÇALIŞAN:** kendi vardiyaları, kendi müsaitliği, kendi takas talepleri ve kendine gelen talepler.

### Personel

- Listeleme, ekleme, düzenleme, pasife alma.
- Tam zamanlı / part-time, pozisyon (BARISTA, STEWARD, MUTFAK, KASIYER), haftalık izin günü,
  haftalık maksimum saat.
- Yeni personel için otomatik kullanıcı hesabı.

### Müsaitlik

- Çalışan haftanın 7 günü için "müsaitim / müsait değilim" işaretler.
- Part-time personel yalnızca müsait olduğu günlerde vardiya alabilir.
- Tam zamanlı personelin izin günü her durumda kapalıdır.

### Haftalık vardiya tablosu

- Satırlar personel, sütunlar Pazartesi–Pazar.
- Hücrelerde SABAH (09:00–17:00), AKSAM (15:00–23:00) veya boş/OFF.
- Yönetici hücreleri açılır liste ile düzenler; çalışan yalnızca kendi satırını görür.
- Mobilde yatay kaydırma desteklenir; ilk sütun sabittir.
- "Haftayı Yayınla" tüm haftayı `YAYINDA` yapar.
- Kurallar:
  - Tam zamanlının izin gününe atama engellenir.
  - Müsait olunmayan güne atama engellenir.
  - Aynı gün ikinci vardiya engellenir.
  - Haftalık limit aşımı (45 / 20 saat) **uyarı** olarak gösterilir (atamayı engellemez).
- Günlük SABAH/AKSAM için "gerekli / atanan personel" ve EKSİK/FAZLA/UYGUN etiketi gösterilir.

### Takas talebi

Akış:

1. Çalışan kendi vardiyasını ve hedef personeli seçer.
2. Sistem hedefin aynı gün vardiyasını bulur:
   - Varsa **takas** (iki vardiyanın personeli yer değiştirir),
   - Yoksa **devir** (talep edenin vardiyası hedefe geçer).
3. Hedef personel kabul veya red eder.
4. Yönetici not girerek onaylar/reddeder.
5. Onayda tüm kurallar yeniden kontrol edilir; hata varsa onay yapılmaz.
6. Başarılı onayda vardiyalar otomatik güncellenir.

### Yoğunluk ve tahmin

- **Yoğunluk sayfası:** seçilen hafta için gün × saat ısı haritası; sipariş sayısı ve dolu masa.
- **Tahmin sayfası:** gelecek hafta için gün/saat bazlı tahmini sipariş ve önerilen personel.
- Yöntem: son 8 haftanın aynı gün-saat ortalaması, yeni haftalara daha yüksek ağırlık veren
  hareketli ortalama. `ForecastService` arayüzü sayesinde yöntem değiştirilebilir.
- Personel ihtiyacı: `ceil(tahmini sipariş / siparis-basina-personel)`, en az `min-personel-saat`.
- Vardiya ihtiyacı, vardiya aralığındaki **en yüksek saatlik ihtiyaç** olarak alınır.

### Otomatik vardiya taslağı

Yönetici tek tıkla haftalık `TASLAK` vardiya üretir:

1. Tahmini yoğunluktan gün/vardiya başına gerekli kişi sayısı hesaplanır.
2. Mevcut `YAYINDA` vardiyalar sabit tutulur; yalnızca eski `TASLAK`lar silinir.
3. Greedy sezgisel:
   - izin günü, müsaitlik, aynı gün tek vardiya kuralları uygulanır,
   - pozisyon dengesi için önce BARISTA, KASIYER, MUTFAK, STEWARD denenir,
   - en az yüklenmiş ve limit içindeki personel tercih edilir,
   - part-time haftalık 20 saat limiti korunmaya çalışılır,
   - karşılanamayan ihtiyaçlar raporlanır.
4. Yönetici tabloyu düzenleyip "Haftayı Yayınla" yapar.

## Sentetik (örnek) veri

`SyntheticDataGenerator` sınıfı son 12 hafta için her gün ve 09–22 saatleri arasında kayıt üretir:

- Hafta içi: sabah hafif, 12–14 öğle yoğunluğu, 15–16 sakin, 17–20 akşam yoğunluğu.
- Hafta sonu: genel olarak hafta içinden %40–60 daha yoğun, tepe 14–20.
- Cuma akşamı ek yoğunluk.
- ±%18 rastgele gürültü; %6 "sakin gün", %6 "çok yoğun gün" sapması.
- Sipariş 5–60 aralığına, dolu masa 0–30 aralığına sınırlandırılır.
- Tarihe bağlı sabit random seed kullanılır (`kafe.sentetik-seed`), böylece sonuç tekrar üretilebilir.

Sentetik üretim yalnızca ilgili dönemde kayıt yoksa çalışır; uygulama yeniden başlatıldığında
veriyi çoğaltmaz.

## CSV Import (gerçek veriye geçiş)

Yönetici menüsünden **CSV** sayfasına gidin. Format:

```csv
tarih,saat,siparisSayisi,doluMasa
2026-11-20,14,42,18
2026-11-20,15,30,12
```

- Başlık satırı opsiyoneldir.
- Ayırıcı: virgül, noktalı virgül veya sekme.
- Tarih formatları: `yyyy-MM-dd`, `dd.MM.yyyy`, `dd/MM/yyyy`, `yyyy/MM/dd`.
- Saat 09–22 aralığında olmalıdır.
- Aynı tarih + saat kaydı varsa **güncellenir**, yoksa eklenir (upsert).
- Örnek dosya: [`docs/ornek-yogunluk.csv`](docs/ornek-yogunluk.csv)

Gerçek veriye tam geçiş için önerilen adımlar:

1. `kafe.seed-enabled: false` yapın (sentetik + örnek personel yüklenmesin).
2. Geliştirme verisini temizlemek için uygulamayı durdurup `data/` klasörünü silin.
3. Uygulamayı başlatın, personeli ve hesapları panelden oluşturun.
4. CSV dosyanızı yükleyin.
5. Haftalık vardiyaları oluşturup yayınlayın.

> Not: CSV import mevcut yoğunluk tablosuna ekleme/güncelleme yapar. Veritabanını tamamen
> temizlemek istiyorsanız `data/` klasörünü silmek en kolay yoldur.

## PostgreSQL'e Geçiş

`postgresql` sürücüsü `pom.xml` içinde hazırdır.

`application.yml` içinde:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kocatepe
    username: kocatepe
    password: gizli
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update   # üretimde validate + Flyway/Liquibase önerilir
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

Ortam değişkenleriyle de verilebilir:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/kocatepe \
SPRING_DATASOURCE_USERNAME=kocatepe \
SPRING_DATASOURCE_PASSWORD=gizli \
mvn spring-boot:run
```

## Konfigürasyon

`src/main/resources/application.yml` içindeki `kafe` bölümü:

| Özellik | Varsayılan | Açıklama |
|---|---|---|
| `opening-hour` | 9 | Açılış saati |
| `closing-hour` | 23 | Kapanış saati |
| `full-time-max-saat` | 45 | Tam zamanlı haftalık limit |
| `part-time-max-saat` | 20 | Part-time haftalık limit |
| `min-personel-saat` | 2 | Saat başına minimum personel |
| `siparis-basina-personel` | 15 | 1 personel başına saatlik sipariş |
| `toplam-masa` | 30 | Toplam masa sayısı |
| `tahmin-hafta-sayisi` | 8 | Tahminde kullanılan geçmiş hafta |
| `sentetik-hafta-sayisi` | 12 | Üretilecek sentetik dönem |
| `sentetik-seed` | 42 | Tekrarlanabilirlik için seed |
| `seed-enabled` | true | Örnek veri yükleme aç/kapat |

## Mimari

```
src/main/java/com/kocatepe/kafe
├── config        AppProperties, SecurityConfig, DataSeeder, TimeConfig, ViewHelpers
├── controller    Auth, Dashboard, Personel, Musaitlik, Vardiya, Takas, Yogunluk, Tahmin, Csv
├── dto           Formlar ve görünüm modelleri (KuralSonucu, VardiyaHaftasi, IhtiyacDurumu...)
├── entity        Personel, Musaitlik, Vardiya, TakasTalebi, YogunlukKaydi, Kullanici
├── repository    Spring Data JPA repository'leri
└── service       Kural, vardiya, takas, tahmin, ihtiyaç, otomatik plan, CSV, sentetik üretim
```

- Katmanlı yapı: controller → service → repository.
- Formlar DTO ile taşınır; entity doğrudan bağlanmaz (personel formu hariç basit DTO kullanır).
- Tarih/saat dilimi: `Europe/Istanbul`.
- Hafta başlangıcı: Pazartesi.

## Testler

```bash
mvn test
```

Kapsanan temel birim testleri:

- `VardiyaKuralServiceTest`: izin günü, müsaitlik, aynı gün ikinci vardiya, haftalık limit uyarısı.
- `TakasServiceTest`: gerçek takas, vardiya devri, müsait olmayan hedef, hedef onayı olmadan yönetici onayı.
- `WeightedMovingAverageForecastServiceTest`: ağırlıklı ortalama ve veri yokken varsayılan.
- `StaffingServiceTest`: katsayı/minimum personel ve vardiya aralığındaki tepe ihtiyaç.

## Varsayımlar

1. **Vardiya süresi:** Her vardiya 8 saattir (1 saat mola dahil) ve haftalık limit hesabında 8 saat
   sayılır. Tam zamanlı 6 gün × 8 = 48 saat olduğundan 45 saat limiti bazen aşılır; şartnamedeki
   gibi bu durum **uyarı** olarak gösterilir, atama engellenmez.
2. **Müsaitlik:** Tam zamanlılarda izin günü yanında, müsaitlik ekranında kapatılan diğer günler de
   atamada dikkate alınır. Part-time için kayıt yoksa gün müsait sayılmaz.
3. **Yayın durumu:** Haftayı yayınlama işlemi o haftadaki tüm vardiyaları `YAYINDA` yapar. Yönetici
   yayınlanmış bir hücreyi değiştirebilir; yeni eklenen hücreler `TASLAK` olur.
4. **Çalışan görünümü:** Çalışan haftalık tabloda yalnızca kendi satırını görür; taslak vardiyalar
   "taslak" etiketiyle görünür. Panelde yalnızca yayınlanmış yaklaşan vardiyalar listelenir.
5. **Takas:** Hedefin aynı gün vardiyası varsa iki vardiya yer değiştirir. Yoksa talep edenin
   vardiyası hedefe devredilir ve talep eden o gün boş kalır. Onay öncesi hedef personelin kabulü
   zorunludur.
6. **Otomatik plan:** Yayındaki vardiyalar korunur, yalnızca taslaklar yeniden üretilir. Part-time
   limiti korunmaya çalışılır; hiç uygun aday yoksa limit aşımına izin verilebilir ve uyarı olarak
   raporlanır.
7. **Yeni personel hesabı:** Otomatik oluşturulur, kullanıcı adı e-posta benzeri üretilir,
   varsayılan şifre `1234`'tür.
8. **Sentetik veri:** Gerçek POS verisi değildir; yalnızca demo ve geliştirme amaçlıdır.
9. **CSV import:** Aynı tarih + saat için upsert yapar; tüm tabloyu silmez.
10. **H2 konsolu ve varsayılan hesaplar** yalnızca geliştirme ortamı içindir; üretimde kapatılmalı
    ve şifreler değiştirilmelidir.

## Kapsam Dışı

- Bahşiş hesabı/dağıtımı yoktur.
- Maaş/bordro yoktur.
- Gerçek POS entegrasyonu yoktur; veri CSV ile içeri alınır.
