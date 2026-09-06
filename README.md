# 💬 ChatControl by Leftycraft

[![Paper Version](https://img.shields.io/badge/Paper-1.21.x-blue.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**ChatControl** adalah plugin chat management modern untuk Minecraft server yang berjalan di **Paper 1.21+** (Paper 1.21, 1.21.4, 1.21.8+). Dibuat khusus oleh **Leftycraft** untuk memberikan kendali penuh kepada pemain dan staff atas tampilan chat dan moderasi chat server.

---

## ✨ Fitur Utama

1. **Per-Player Chat Toggle (Filter Chat Pribadi)**
   - Pemain dapat mematikan pesan publik dari pemain lain kapan saja (`/chat toggle` atau `/chat on`/`/chat off`).
   - Saat chat dimatikan, pemain **hanya melihat pesannya sendiri**, tanpa terganggu spam atau obrolan pemain lain di layar.
   - **Sound Integration**: Suara notifikasi chat (termasuk kompatibilitas dengan PlayMoreSounds) hanya akan berbunyi jika fitur chat sedang aktif pada pemain tersebut. Jika pemain mematikan chat, tidak akan ada suara chat yang mengganggu.

2. **Server-Wide Chat Mute (Admin Control)**
   - Admin/Staff dapat mengheningkan chat 1 server secara instan (`/chat muteall [on|off]`).
   - Sangat berguna saat event, pengumuman penting, atau penanganan spam massal.
   - Status mute dapat disimpan secara persisten melewati restart/reload server.

3. **Whitelist Chat Bypass System (Admin Control)**
   - Admin dapat menambahkan pemain tertentu ke dalam daftar whitelist (`/chat whitelist add <player>`).
   - Pemain dalam whitelist tetap dapat mengirim pesan meskipun 1 server sedang di-mute.
   - Dilengkapi perintah lengkap: `add`, `remove`, `list`, dan `clear`.

4. **Separate Permissions (Izin Terpisah untuk Player & Admin)**
   - Hak akses pemain biasa dan admin dibedakan secara terstruktur dan aman.

5. **Clear Chat Utility**
   - Membersihkan tampilan layar chat publik untuk semua pemain dengan sekali perintah (`/chat clear`).

6. **Modern Format & Adventure / MiniMessage Support**
   - Mendukung format warna legacy (`&a`, `&c`, `&l`) dan gradient / hex modern (`<gradient:#00d2ff:#3a7bd5>`, `&#3a7bd5`).
   - Pesan dan konfigurasi 100% dapat disesuaikan di `config.yml`.

---

## 📜 Perintah (Commands) & Izin (Permissions)

### Perintah Pemain (Player Commands)

| Perintah | Alias | Izin (Permission) | Default | Deskripsi |
| :--- | :--- | :--- | :--- | :--- |
| `/chat` / `/chat toggle` | `/chattoggle`, `/cc` | `chatcontrol.toggle` | `Semua Player` | Mengaktifkan/menonaktifkan chat pemain lain |
| `/chat on` | - | `chatcontrol.toggle` | `Semua Player` | Mengaktifkan chat publik |
| `/chat off` | - | `chatcontrol.toggle` | `Semua Player` | Menonaktifkan chat publik (hanya lihat pesan sendiri) |
| `/chat status` | - | `chatcontrol.status` | `Semua Player` | Melihat status chat pribadi, status server mute, & whitelist |
| `/chat help` | - | `chatcontrol.use` | `Semua Player` | Menampilkan menu bantuan perintah |

---

### Perintah Admin / Staff (Admin Commands)

| Perintah | Izin (Permission) | Default | Deskripsi |
| :--- | :--- | :--- | :--- |
| `/chat muteall [on\|off]` | `chatcontrol.admin.muteall` | `OP` | Mengaktifkan/menonaktifkan mute chat 1 server |
| `/chat whitelist add <player>` | `chatcontrol.admin.whitelist` | `OP` | Menambahkan pemain ke whitelist mute bypass |
| `/chat whitelist remove <player>` | `chatcontrol.admin.whitelist` | `OP` | Menghapus pemain dari whitelist mute bypass |
| `/chat whitelist list` | `chatcontrol.admin.whitelist` | `OP` | Menampilkan semua pemain yang di-whitelist |
| `/chat whitelist clear` | `chatcontrol.admin.whitelist` | `OP` | Menghapus seluruh daftar whitelist |
| `/chat clear` | `chatcontrol.admin.clear` | `OP` | Membersihkan layar chat publik |
| `/chat reload` | `chatcontrol.admin.reload` | `OP` | Memuat ulang `config.yml` dan `data.yml` |

---

### Daftar Permission Lengkap

```yaml
permissions:
  chatcontrol.use:
    description: Mengizinkan akses dasar command /chat
    default: true
  chatcontrol.toggle:
    description: Mengizinkan pemain untuk /chat toggle, on, off
    default: true
  chatcontrol.status:
    description: Mengizinkan pemain mengecek /chat status
    default: true
  chatcontrol.bypass.muteall:
    description: Bebas mengirim pesan saat 1 server sedang di-mute
    default: op
  chatcontrol.bypass.toggle:
    description: Pesan dari pengirim ini tetap muncul meskipun penerima mematikan chat
    default: op
  chatcontrol.bypass.clear:
    description: Layar chat tidak ikut terhapus saat admin menjalankan /chat clear
    default: op
  chatcontrol.admin:
    description: Izin penuh ke seluruh fitur admin ChatControl
    default: op
    children:
      chatcontrol.admin.muteall: true
      chatcontrol.admin.whitelist: true
      chatcontrol.admin.clear: true
      chatcontrol.admin.reload: true
      chatcontrol.bypass.muteall: true
```

---

## ⚙️ Konfigurasi (`config.yml`)

```yaml
# Prefix pesan plugin
prefix: "<gradient:#00d2ff:#3a7bd5>[LeftyChat]</gradient> &8» &r"

# Pengaturan Suara Chat (Sound & PlayMoreSounds Integration)
sound:
  enabled: true
  name: "minecraft:entity.experience_orb.pickup"
  volume: 1.0
  pitch: 1.0
  # Suara hanya berbunyi untuk pemain yang chat-nya AKTIF
  only-for-enabled-chat: true
  playmoresounds-integration: true

# Pengaturan Mute Server
server-mute:
  persist-state: true
  allow-permission-bypass: true

# Pengaturan Bersihkan Chat
clear-chat:
  lines: 100

# Kustomisasi Pesan (Mendukung legacy & dan MiniMessage)
messages:
  chat-toggled-on: "&aChat dari pemain lain berhasil &lDIAKTIFKAN&a. Kamu sekarang dapat melihat pesan dan mendengar suara chat."
  chat-toggled-off: "&cChat dari pemain lain berhasil &lDINONAKTIFKAN&c. Kamu hanya dapat melihat pesanmu sendiri &7(suara chat dinonaktifkan)&c."
  server-muted-broadcast: "&c&l[SERVER MUTE] &fChat server telah &c&lDINONAKTIFKAN &foleh &e%admin%&f!"
  server-unmuted-broadcast: "&a&l[SERVER MUTE] &fChat server telah &a&lDIAKTIFKAN &fkembali oleh &e%admin%&f!"
  server-is-muted: "&cChat server saat ini sedang dinonaktifkan oleh staff! Hanya pemain yang di-whitelist yang dapat mengirim pesan."
  whitelist-added: "&aPemain &e%player% &aberhasil ditambahkan ke daftar whitelist chat mute."
  whitelist-removed: "&cPemain &e%player% &adihapus dari daftar whitelist chat mute."
  ...
```

---

## 🔨 Cara Build & Install

### Persyaratan
- **Java 21** atau yang lebih baru
- **Paper / Purpur / Folia 1.21+** (1.21.x, 1.21.4, 1.21.8+)
- **Maven 3.8+**

### Kompilasi dari Source Code
```bash
# Clone repository
git clone https://github.com/DarkIgnite/ChatControl.git

# Masuk ke direktori
cd ChatControl

# Build file .jar
mvn clean package
```
File JAR yang sudah siap pakai akan berada di direktori `target/ChatControl-1.0.0.jar`.

### Instalasi di Server
1. Salin `ChatControl-1.0.0.jar` ke folder `plugins/` server Paper Anda.
2. Restart server atau muat plugin.
3. Sesuaikan konfigurasi di `plugins/ChatControl/config.yml` sesuai kebutuhan Anda.

---

## 👨‍💻 Developer & Credits
- **Author**: Leftycraft
- **Repository**: [DarkIgnite/ChatControl](https://github.com/DarkIgnite/ChatControl)