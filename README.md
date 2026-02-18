# Kalender Nusantara - Android

Aplikasi kalender **multi-tahun** bertema Merah Putih untuk pengguna Indonesia, menampilkan hari libur nasional dengan highlight warna merah beserta keterangannya. Data hari libur dapat diperbarui **tanpa update APK** — cukup edit file CSV di GitHub, perubahan akan otomatis terunduh ke perangkat pengguna.

> Versi saat ini: **1.1** · Min Android: **13 (API 33)** · Bahasa: **Kotlin**

---

## Fitur

### 🗓️ Kalender
- **Multi-Tahun Dinamis** — Navigasi antar tahun menggunakan tombol **◀ Tahun ▶** di bawah toolbar. Mendukung tahun 2025–2030, dapat diperluas kapan saja
- **Scroll Otomatis ke Bulan Ini** — Saat aplikasi dibuka, kalender langsung scroll ke bulan berjalan
- **Tombol "Ke Hari Ini"** — Kembali ke bulan berjalan kapan saja, termasuk saat sedang melihat tahun lain
- **Toggle Mode Tampilan** — Beralih antara *Mode Accordion* (buka/tutup per bulan) dan *Mode Semua Terbuka* (12 bulan sekaligus)
- **Highlight Hari Ini** — Tanggal hari ini ditandai lingkaran merah Indonesia dengan angka putih (prioritas tertinggi)
- **Keterangan Hari Libur** — Nama hari libur ditampilkan sebagai legenda di bawah setiap bulan

### 🎨 Indikator Warna Tanggal

| Kondisi | Tampilan |
|---------|----------|
| Hari Ini | 🔴 Lingkaran merah dengan angka putih |
| Hari Libur Nasional | 🔴 Lingkaran merah muda dengan angka merah |
| Hari Minggu | Angka merah |
| Hari Sabtu | Angka biru |
| Hari Biasa | Angka hitam |

### 📡 Pembaruan Data Hari Libur (Tanpa Update APK)
- **Unduh Otomatis dari GitHub** — Saat app dibuka, data hari libur terbaru diunduh dari GitHub Raw secara diam-diam di background
- **Cache Lokal** — Data tersimpan di internal storage perangkat, tetap berfungsi saat offline
- **Interval Unduh** — Pembaruan dilakukan maksimal sekali per 24 jam agar hemat data
- **Prioritas Sumber Data:**
  1. Cache internal (unduhan GitHub terbaru)
  2. Assets bawaan APK
  3. Kalender kosong jika keduanya tidak ada
- **Banner Status Sumber Data:**
  - ✅ Hijau — data berhasil diunduh dari GitHub (tampil tanggal pembaruan)
  - 📦 Biru — data bawaan APK, belum pernah diunduh
  - ⚠ Oranye — tidak ada data untuk tahun tersebut
- **Perbarui Manual** — Menu ⋮ → *Perbarui Data Hari Libur* untuk force refresh kapan saja

### 🔔 Update Aplikasi
- **Cek Update Otomatis** — Saat app dibuka, mengecek versi terbaru via GitHub Releases API secara diam-diam
- **Notifikasi Update** — Jika ada versi baru, muncul dialog berisi catatan rilis dan tombol download langsung
- **Cek Update Manual** — Menu ⋮ → *Cek Update Aplikasi*

### 🎨 Tema Kalender Nusantara
- Toolbar **merah Indonesia** `#CC0001` selaras warna bendera RI
- Year selector bar **merah gelap** `#A80000`
- Background app **putih hangat** `#FFFAFA`
- Ikon aplikasi: kalender bertema merah putih dengan **bintang Pancasila** emas dan **angka 17** — simbol Hari Kemerdekaan RI

### ℹ️ Dialog Tentang
- Link website, email, dan GitHub yang bisa diklik langsung
- Gambar QRIS donasi
- Daftar rekening bank yang bisa di-copy

---

## Pembaruan Data Hari Libur Tanpa Update APK

### Cara Kerja

```
App dibuka
    │
    ▼
Fase 1 — Tampil seketika (dari data lokal)
    ├── Cache ada?  → Tampilkan dari cache    Banner: ✅ Hijau
    ├── Assets ada? → Tampilkan dari APK      Banner: 📦 Biru
    └── Keduanya kosong → Kalender kosong     Banner: ⚠ Oranye
    │
    ▼ (background, tidak menghalangi UI)
Fase 2 — Unduh diam-diam dari GitHub
    ├── Sudah < 24 jam?    → Skip
    ├── Tidak ada internet? → Skip
    └── Ada internet?
            ↓
        Unduh holidays_<tahun>.csv dari GitHub Raw
            ↓
        Konten valid? → Simpan ke cache
                     → Rebuild kalender otomatis
                     → Banner berubah ke ✅ Hijau
```

### Cara Memperbarui Data (Tanpa Build APK)

Cukup edit langsung di GitHub — tidak perlu buka Android Studio:

1. Buka `github.com/ryanbekabe/Kalender-2026`
2. Navigasi ke `app/src/main/assets/`
3. Edit atau buat file `holidays_<tahun>.csv`
4. Commit perubahan

Pengguna mendapat data terbaru **otomatis saat membuka app** keesokan harinya, atau **seketika** via menu ⋮ → *Perbarui Data Hari Libur*.

### Status File CSV

| File | Status |
|------|--------|
| `holidays_2025.csv` | ⬜ Belum ada |
| `holidays_2026.csv` | ✅ Tersedia |
| `holidays_2027.csv` | ✅ Tersedia |
| `holidays_2028.csv` | ✅ Tersedia |
| `holidays_2029.csv` | ⬜ Belum ada |
| `holidays_2030.csv` | ⬜ Belum ada |

### Format File CSV

```csv
tanggal,keterangan
YYYY-MM-DD,Nama Hari Libur
```

Aturan:
- Baris pertama adalah header — wajib ada
- Format tanggal harus `YYYY-MM-DD`
- Baris kosong dan baris diawali `#` diabaikan (gunakan sebagai komentar)
- Urutan baris tidak harus berurutan

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

## Sistem Update Aplikasi

### Cara Merilis Versi Baru

**1. Naikkan versi di `app/build.gradle.kts`:**
```kotlin
versionCode = 3       // selalu tambah 1
versionName = "1.2"   // format: MAJOR.MINOR
```

**2. Build APK Release:**
```bash
./gradlew assembleRelease
```

**3. Buat GitHub Release:**
- Repo GitHub → **Releases** → **Draft a new release**
- **Tag:** `v1.2` (awali `v`, cocokkan dengan `versionName`)
- **Deskripsi:** catatan perubahan (muncul di dialog update pengguna)
- **Attach:** upload file APK
- Klik **Publish release**

Pengguna dengan APK lama mendapat notifikasi otomatis saat membuka app.

---

## Teknologi

| | |
|---|---|
| **Bahasa** | Kotlin |
| **Min SDK** | 33 (Android 13) |
| **Target SDK** | 36 |
| **UI** | RecyclerView + GridLayoutManager (7 kolom) |
| **Library** | AndroidX, Material Design 3 |
| **Update APK** | GitHub Releases API |
| **Update Data** | GitHub Raw (HttpURLConnection, tanpa library) |
| **Cache** | Internal Storage (`context.filesDir`) |
| **Preferensi** | SharedPreferences |

---

## Struktur Proyek

```
app/src/main/
├── assets/
│   ├── holidays_2026.csv        # Data hari libur 2026 (bawaan APK)
│   ├── holidays_2027.csv        # Data hari libur 2027 (bawaan APK)
│   ├── holidays_2028.csv        # Data hari libur 2028 (bawaan APK)
│   ├── holidays_<tahun>.csv     # Tambah untuk tahun berikutnya
│   └── QRISHanyaJasaCom.jpg     # Gambar QRIS donasi
├── java/com/hanyajasa/kalender2026/
│   ├── MainActivity.kt          # Aktivitas utama: year selector, scroll, banner status
│   ├── MonthAdapter.kt          # Adapter bulan: accordion, expand-all, legend
│   ├── DayAdapter.kt            # Adapter tanggal: warna, highlight hari ini
│   ├── HolidayUpdater.kt        # Unduh & cache CSV hari libur dari GitHub
│   ├── UpdateChecker.kt         # Cek update versi APK via GitHub Releases API
│   └── AboutDialog.kt           # Dialog Tentang: kontak, QRIS, rekening bank
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Toolbar + Year Selector + Banner + RecyclerView
    │   ├── dialog_about.xml     # Layout dialog Tentang (custom ScrollView)
    │   ├── item_month.xml       # Item bulan: header + grid + legenda libur
    │   └── item_day.xml         # Item sel tanggal
    ├── menu/
    │   └── menu_main.xml        # Toggle, Ke Hari Ini, Perbarui Data, Cek Update, Tentang
    ├── drawable/
    │   ├── ic_launcher_background.xml  # Background ikon: bendera merah putih
    │   ├── ic_launcher_foreground.xml  # Foreground ikon: kalender bertema Nusantara
    │   ├── bg_holiday.xml              # Lingkaran merah muda (hari libur)
    │   └── bg_today.xml                # Lingkaran merah Indonesia (hari ini)
    └── values/
        ├── strings.xml          # Nama app: Kalender Nusantara
        └── colors.xml           # Palet warna Merah Putih Indonesia
```

---

## Instalasi

1. Unduh APK dari halaman [Releases](https://github.com/ryanbekabe/Kalender-2026/releases/latest)
2. Instal pada perangkat Android 13 ke atas
3. Buka aplikasi — kalender langsung scroll ke bulan berjalan

## Build APK

```bash
# Debug
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

---

## Dukungan

Jika aplikasi ini bermanfaat, Anda bisa mendukung pengembangan melalui:

### QRIS

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

### Kontak

- 🌐 [hanyajasa.com](https://hanyajasa.com)
- ✉ hanyajasa@gmail.com
- 💻 [@ryanbekabe](https://github.com/ryanbekabe)

Terima kasih atas dukungan Anda!
