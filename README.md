# Read SMS / readsms

Read SMS is a private Android + Python project for collecting SMS from secondary Android phones and showing them on one owner phone in near realtime.

Read SMS เป็นโปรเจกต์ส่วนตัวสำหรับรวม SMS จากเครื่อง Android รองหลายเครื่อง มาให้เครื่องหลักดูรวมกันแบบใกล้ realtime

> Important: use this only on phones you own or phones where the user clearly agrees. SMS can contain OTPs, bank messages, and private information. This project is not designed for hidden monitoring.

> สำคัญ: ใช้เฉพาะเครื่องของตัวเองหรือเครื่องที่เจ้าของเครื่องยินยอมชัดเจนเท่านั้น SMS อาจมี OTP, ข้อความธนาคาร และข้อมูลส่วนตัว โปรเจกต์นี้ไม่ได้ออกแบบมาเพื่อแอบติดตามใคร

## What It Solves / แก้ปัญหาอะไร

Some people use more than one Android phone. SMS may arrive on phone B or C, but the person usually uses phone A. Read SMS makes B/C act as collectors and A act as the owner viewer.

บางคนมีมือถือ Android หลายเครื่อง SMS อาจเข้าเครื่อง B หรือ C แต่ใช้งานหลักที่เครื่อง A โปรเจกต์นี้ให้ B/C เป็นเครื่องเก็บข้อความ และให้ A เป็นเครื่องหลักที่ดูข้อความรวมกัน

## Current Shape / สถานะปัจจุบัน

- Android app written in Kotlin + Jetpack Compose.
- Python FastAPI server.
- JSON file storage, default retention 1 day.
- Owner mode shows conversations, unread/new state, realtime updates, and silent notifications.
- Collector mode reads local SMS, queues pending messages, and syncs in the background.
- Supports Android light/dark system theme.
- Thai-friendly UI copy and typography.
- No real API token, phone number, IP address, or password is hardcoded in app/server source.

## Quick Start / เริ่มใช้งานเร็ว

### 1. Install server packages once / ติดตั้ง dependency ฝั่ง server ครั้งเดียว

```powershell
pip install -r server\requirements.txt
```

### 2. Run the server / รัน server

Run from the project root:

```powershell
python main.py
```

If `server\.env` does not exist, the server creates it automatically with a random API token.

ถ้ายังไม่มี `server\.env` ระบบจะสร้างให้เองพร้อม API token แบบสุ่ม

The server prints URLs like this:

```text
ReadSMS server
Config: <project>\server\.env
Local:  http://127.0.0.1:9201
Phone:  http://<LAN-IP>:9201
Stop:   Ctrl+C
```

Use the `Phone:` URL in the Android app when the phones are on the same Wi-Fi.

ให้นำ URL บรรทัด `Phone:` ไปใส่ในแอป Android เมื่อมือถืออยู่ Wi-Fi เดียวกัน

### 3. Build the Android APK / build APK

```powershell
.\build-apk.ps1
```

APK output:

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

Install it on:

- Phone A: choose `เครื่องหลัก` / Owner.
- Phone B and C: choose `เครื่องรอง` / Collector.

### 4. Configure the Android app / ตั้งค่าในแอป

Open `server\.env` and copy:

```text
READSMS_API_TOKEN=<YOUR_API_TOKEN>
```

In the app:

```text
Server URL: http://<LAN-IP>:9201
API token:  <YOUR_API_TOKEN>
```

The app validates the server and token every time it opens. If the values are wrong, it shows the setup screen before syncing.

แอปจะตรวจ Server URL และ API token ทุกครั้งที่เปิด ถ้าค่าผิดจะขึ้นหน้า setup ให้แก้ก่อน sync

## Documentation / เอกสารละเอียด

- [Thai guide](docs/TH.md)
- [English guide](docs/EN.md)
- [Security and privacy](docs/SECURITY.md)

## Project Structure / โครงสร้างโปรเจกต์

```text
readsms/
  main.py                  run server from the project root
  server/
    app/                   FastAPI backend
    .env                   local config and generated API token, do not commit
    .env.example           safe example config
    sms.json               local JSON storage, generated at runtime
  android/
    app/                   Kotlin Android app
  docs/                    detailed documentation
```

## Security Note / หมายเหตุเรื่องความปลอดภัย

- `server\.env` contains the real API token and is ignored by `.gitignore`.
- `server\sms.json` contains SMS data and is ignored by `.gitignore`.
- Do not share APKs configured with your real token.
- Do not commit local logs, build outputs, APKs, or Android Studio local files.
- Current storage is plain JSON. Use disk encryption or add encryption-at-rest before storing sensitive production data.

## License / ใบอนุญาต

MIT License. See [LICENSE](LICENSE).

ใช้สัญญาอนุญาต MIT ดูรายละเอียดที่ [LICENSE](LICENSE)
