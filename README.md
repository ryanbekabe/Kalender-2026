# Kalender Indonesia - Android

Aplikasi kalender Gregorian **multi-tahun** untuk pengguna Indonesia, menampilkan hari libur nasional dengan warna merah beserta keterangannya. Tidak perlu rilis APK baru setiap tahun — cukup upload file CSV baru ke folder `assets`.

## Fitur

- **Multi-Tahun Dinamis** — Ganti tampilan tahun kapan saja menggunakan tombol **◀ Tahun ▶** di bawah toolbar. Data hari libur dibaca dari file `holidays_<tahun>.csv` secara otomatis
- **Scroll Otomatis ke Bulan Ini** — Saat aplikasi dibuka, kalender langsung scroll ke bulan berjalan. Tombol **"Ke Hari Ini"** di toolbar untuk kembali ke bulan ini kapan saja, termasuk saat sedang melihat tahun lain
- **Tampilan 12 Bulan Sekaligus** — Semua bulan ditampilkan dalam satu halaman scroll vertikal
- **Toggle Mode Tampilan** — Tombol di toolbar untuk beralih antara:
  - *Mode Accordion* — buka/tutup satu bulan sekaligus dengan klik nama bulan
  - *Mode Semua Terbuka* — semua 12 bulan terbuka sekaligus
- **Hari Libur dari File CSV** — Data hari libur dimuat dari `assets/holidays_<tahun>.csv`, tidak perlu update kode untuk mengubah data libur
- **Peringatan CSV Tidak Ada** — Jika file CSV untuk tahun tertentu belum tersedia, muncul banner kuning sebagai pengingat
- **Keterangan Hari Libur** — Daftar tanggal merah beserta nama hari liburnya ditampilkan di bawah setiap bulan
- **Highlight Hari Ini** — Tanggal hari ini ditandai dengan lingkaran biru tua dan angka putih agar mudah ditemukan
- **Cek Update Otomatis** — Saat aplikasi dibuka, mengecek versi terbaru dari GitHub. Jika ada versi baru, muncul dialog notifikasi beserta catatan rilis dan tombol download langsung
- **Cek Update Manual** — Tersedia di menu overflow (⋮) untuk memeriksa update kapan saja
- **Indikator Warna Tanggal:**
  - Hari Ini — Lingkaran biru tua dengan angka putih (prioritas tertinggi)
  - Hari Libur Nasional — Lingkaran merah muda dengan angka merah
  - Hari Minggu — Merah
  - Hari Sabtu — Biru
  - Hari Biasa — Hitam

---

## Multi-Tahun Dinamis

### Cara Kerja

```
Buka aplikasi
      │
      ▼
Baca tahun sistem (misal: 2026)
      │
      ▼
Cari assets/holidays_2026.csv
      │
   Ada file?
   ┌───┴───┐
  Ya      Tidak
   │        │
Muat      Tampilkan kalender
libur     kosong + banner ⚠
   │
Scroll otomatis ke bulan ini
```

Saat user menekan tombol **◀** atau **▶**:
```
User tekan ◀ (atau ▶)
      │
      ▼
activeYear-- (atau ++)
      │
      ▼
Cari assets/holidays_<tahun>.csv
      │
Rebuild 12 bulan → refresh adapter
      │
Scroll ke Januari tahun baru
```

### Cara Menambah Tahun Baru

Cukup **tiga langkah** — tanpa update kode, tanpa rilis APK baru:

**Langkah 1 — Buat file CSV baru:**
```
assets/holidays_2029.csv
```

**Langkah 2 — Isi dengan format standar:**
```csv
tanggal,keterangan
2029-01-01,Tahun Baru 2029
2029-01-24,Tahun Baru Imlek 2580
...
```

**Langkah 3 — Commit & push ke GitHub.**

File CSV akan ikut terbundle saat build APK berikutnya. Pengguna mendapat tahun baru setelah update APK.

> **Catatan:** Rentang tahun yang didukung saat ini: **2025 – 2030**.
> Untuk memperluas rentang, ubah konstanta `yearMin` dan `yearMax` di `MainActivity.kt`.

### File CSV yang Tersedia

| File | Status |
|------|--------|
| `holidays_2025.csv` | ⬜ Belum ada — perlu dibuat |
| `holidays_2026.csv` | ✅ Tersedia |
| `holidays_2027.csv` | ✅ Tersedia |
| `holidays_2028.csv` | ✅ Tersedia |
| `holidays_2029.csv` | ⬜ Belum ada — perlu dibuat |
| `holidays_2030.csv` | ⬜ Belum ada — perlu dibuat |

### Format File CSV

```csv
tanggal,keterangan
YYYY-MM-DD,Nama Hari Libur
```

Aturan format:
- Baris pertama adalah header — wajib ada, tidak boleh dihapus
- Kolom `tanggal` harus format `YYYY-MM-DD`
- Kolom `keterangan` boleh mengandung spasi dan karakter khusus
- Baris kosong dan baris diawali `#` diabaikan (bisa dipakai sebagai komentar)
- Tidak perlu urut tanggal, tapi disarankan urut untuk kemudahan baca

---

## Hari Libur Nasional

### 2026

| Bulan | Tanggal | Keterangan |
|-------|---------|------------|
| Januari | 1 | Tahun Baru 2026 |
| Januari | 16 | Isra Mikraj Nabi Muhammad SAW |
| Februari | 17 | Tahun Baru Imlek 2577 |
| Maret | 19 | Hari Suci Nyepi |
| Maret | 20 | Idul Fitri 1447 H |
| Maret | 21 | Idul Fitri 1447 H |
| April | 3 | Wafat Yesus Kristus |
| April | 5 | Hari Paskah |
| Mei | 1 | Hari Buruh Internasional |
| Mei | 12 | Hari Raya Waisak 2570 |
| Mei | 14 | Kenaikan Yesus Kristus |
| Mei | 27 | Idul Adha 1447 H |
| Juni | 1 | Hari Lahir Pancasila |
| Juni | 16 | Tahun Baru Islam 1448 H |
| Agustus | 17 | Hari Kemerdekaan RI |
| Agustus | 25 | Maulid Nabi Muhammad SAW |
| Desember | 25 | Hari Raya Natal |

### 2027

| Bulan | Tanggal | Keterangan |
|-------|---------|------------|
| Januari | 1 | Tahun Baru 2027 |
| Januari | 6 | Isra Mikraj Nabi Muhammad SAW |
| Januari | 29 | Tahun Baru Imlek 2578 |
| Maret | 8 | Hari Suci Nyepi |
| Maret | 10 | Idul Fitri 1448 H |
| Maret | 11 | Idul Fitri 1448 H |
| Maret | 26 | Wafat Yesus Kristus |
| Maret | 28 | Hari Paskah |
| Mei | 1 | Hari Buruh Internasional |
| Mei | 6 | Kenaikan Yesus Kristus |
| Mei | 17 | Idul Adha 1448 H |
| Mei | 22 | Hari Raya Waisak 2571 |
| Juni | 1 | Hari Lahir Pancasila |
| Juni | 6 | Tahun Baru Islam 1449 H |
| Agustus | 14 | Maulid Nabi Muhammad SAW |
| Agustus | 17 | Hari Kemerdekaan RI |
| Desember | 25 | Hari Raya Natal |

### 2028

| Bulan | Tanggal | Keterangan |
|-------|---------|------------|
| Januari | 1 | Tahun Baru 2028 |
| Januari | 17 | Tahun Baru Imlek 2579 |
| Februari | 26 | Isra Mikraj Nabi Muhammad SAW |
| Maret | 14 | Hari Raya Nyepi |
| Maret | 21 | Wafat Yesus Kristus |
| Maret | 23 | Hari Paskah |
| Maret | 28 | Idul Fitri 1449 H |
| Maret | 29 | Idul Fitri 1449 H |
| Mei | 1 | Hari Buruh Internasional |
| Mei | 5 | Idul Adha 1449 H |
| Mei | 11 | Hari Raya Waisak 2572 |
| Juni | 1 | Kenaikan Yesus Kristus & Hari Lahir Pancasila |
| Juni | 25 | Tahun Baru Islam 1450 H |
| Agustus | 3 | Maulid Nabi Muhammad SAW |
| Agustus | 17 | Hari Kemerdekaan RI |
| Desember | 25 | Hari Raya Natal |

---

## Sistem Auto-Update

Aplikasi mengecek versi terbaru secara otomatis melalui **GitHub Releases API** setiap kali dibuka. Tidak perlu server — cukup manfaatkan fitur GitHub Releases yang gratis.

### Cara Kerja

```
APK (versionName saat ini)
        │
        ▼
GitHub API: api.github.com/repos/ryanbekabe/Kalender-2026/releases/latest
        │
        ▼
Bandingkan tag_name GitHub  vs  versionName APK
        │
   Ada versi baru?
   ┌────┴────┐
  Ya        Tidak
   │          │
Dialog     Diam saja
Update    (silent mode)
   │
Tombol "Download"
→ Buka browser ke halaman release / file APK
```

### Dua Mode Pengecekan

| Mode | Kapan | Perilaku jika sudah terbaru |
|------|-------|-----------------------------|
| **Otomatis** | Setiap buka aplikasi | Diam, tidak mengganggu |
| **Manual** | Menu ⋮ → "Cek Update" | Tampilkan dialog "Sudah Terbaru" |

### Cara Merilis Versi Baru

**1. Naikkan versi di `app/build.gradle.kts`:**
```kotlin
versionCode = 2       // tambah 1 setiap rilis
versionName = "1.1"   // format: MAJOR.MINOR
```

**2. Build APK:**
```bash
./gradlew assembleRelease
```

**3. Buat GitHub Release:**
- Buka repo GitHub → **Releases** → **Draft a new release**
- **Tag:** `v1.1` ← harus diawali huruf `v`, angka harus sama dengan `versionName`
- **Title:** nama rilis (contoh: "Versi 1.1 - Tambah CSV 2027")
- **Deskripsi:** catatan perubahan (akan muncul di dialog update pada APK lama)
- **Attach file:** upload file APK hasil build
- Klik **Publish release**

Seluruh pengguna yang membuka APK versi lama akan otomatis mendapat notifikasi update.

---

## Teknologi

- **Bahasa:** Kotlin
- **Min SDK:** 33 (Android 13)
- **Target SDK:** 36
- **UI:** RecyclerView dengan GridLayoutManager (7 kolom)
- **Library:** AndroidX, Material Design 3
- **Update:** GitHub Releases API (tanpa library tambahan)

## Struktur Proyek

```
app/src/main/
├── assets/
│   ├── holidays_2026.csv       # Data hari libur 2026
│   ├── holidays_2027.csv       # Data hari libur 2027
│   ├── holidays_2028.csv       # Data hari libur 2028
│   └── holidays_<tahun>.csv    # Tambah file baru untuk tahun berikutnya
├── java/com/hanyajasa/kalender2026/
│   ├── MainActivity.kt         # Aktivitas utama: multi-tahun, scroll otomatis, year selector
│   ├── MonthAdapter.kt         # Adapter bulan: accordion, expand-all, legend, replaceData
│   ├── DayAdapter.kt           # Adapter grid tanggal: pewarnaan hari & highlight hari ini
│   └── UpdateChecker.kt        # Cek update otomatis via GitHub Releases API
└── res/
    ├── layout/
    │   ├── activity_main.xml   # Layout utama: Toolbar + Year Selector Bar + RecyclerView
    │   ├── item_month.xml      # Item bulan: header + grid + legend
    │   └── item_day.xml        # Item sel tanggal
    ├── menu/
    │   └── menu_main.xml       # Menu toolbar: Toggle, Ke Hari Ini, Cek Update, Tentang
    └── drawable/
        ├── bg_holiday.xml      # Lingkaran merah muda untuk hari libur
        └── bg_today.xml        # Lingkaran biru tua untuk hari ini
```

## Instalasi

1. Unduh APK dari halaman [Releases](https://github.com/ryanbekabe/Kalender-2026/releases/latest)
2. Instal pada perangkat Android 13 ke atas
3. Buka aplikasi

## Build APK

```bash
./gradlew assembleDebug
```

APK akan dihasilkan di: `app/build/outputs/apk/debug/app-debug.apk`

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

## Dukungan

Jika Anda ingin mendukung pengembangan proyek opensource ini, Anda bisa berdonasi melalui:

### Donasi Pengembangan

Jika ingin mendukung pengembangan aplikasi ini, atau merasa aplikasi ini bermanfaat, Anda dapat melakukan donasi melalui transfer QRIS dengan scan gambar berikut:

![QRIS Donasi](QRISHanyaJasaCom.jpg)

### Rekening Bank

| Bank | Nomor Rekening |
|------|----------------|
| BSI | 9692999170 |
| BCA | 8600432053 |
| BRI | 4543-01-020754-53-0 |
| Mandiri | 159-00-0068323-4 |
| Muamalat | 6310042068 |
| Dana | 082254205110 |
| Jenius | 90110062490 |
| Jago | 101396991206 |
| Seabank | 901899706783 |

Terima kasih atas dukungan Anda!
