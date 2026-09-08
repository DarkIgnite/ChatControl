# 💬 ChatControl by Leftycraft

[![Paper Version](https://img.shields.io/badge/Paper-1.21.x-blue.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**ChatControl** adalah plugin chat management modern untuk Minecraft server yang berjalan di **Paper 1.21+** (Paper 1.21, 1.21.4, 1.21.8+). Dibuat khusus oleh **Leftycraft** untuk memberikan kendali penuh kepada pemain dan staff atas tampilan chat dan moderasi chat server.

---

## ✨ Fitur Utama

1. **Per-Player Chat Toggle (Filter Chat Pribadi)**
   - Pemain dapat mematikan chat kapan saja (`/chat toggle` atau `/chat on`/`/chat off`).
   - **Self-Chat Prevention saat Off:** Saat chat dimatikan (`/chat off`), pemain tidak dapat mengirim pesan chat dan akan muncul pesan peringatan untuk mengaktifkan chat terlebih dahulu menggunakan `/chat on`.
   - **PlayMoreSounds Integrated**: Terintegrasi otomatis dengan recipient filtering pada PlayMoreSounds sehingga saat chat dimatikan, tidak ada suara chat yang masuk.

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
| `/chat` / `/chat toggle` | `/chattoggle`, `/cc` | `chatcontrol.toggle` | `Semua Player` | Mengaktifkan/menonaktifkan chat |
| `/chat on` | - | `chatcontrol.toggle` | `Semua Player` | Mengaktifkan chat publik |
| `/chat off` | - | `chatcontrol.toggle` | `Semua Player` | Menonaktifkan chat publik |
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

## 🔨 Cara Build & Install

```bash
git clone https://github.com/DarkIgnite/ChatControl.git
cd ChatControl
mvn clean package
```
File JAR yang sudah siap pakai akan berada di direktori `target/ChatControl-1.0.0.jar`.

---

## 👨‍💻 Developer & Credits
- **Author**: Leftycraft
- **Repository**: [DarkIgnite/ChatControl](https://github.com/DarkIgnite/ChatControl)