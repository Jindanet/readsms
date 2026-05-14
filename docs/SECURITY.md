# Security and Privacy / ความปลอดภัยและความเป็นส่วนตัว

This project handles SMS, which may include OTPs, banking messages, personal names, phone numbers, and private content. Treat the server, APK, logs, and backups as sensitive.

โปรเจกต์นี้จัดการ SMS ซึ่งอาจมี OTP, ข้อความธนาคาร, ชื่อ, เบอร์โทร และข้อมูลส่วนตัว ให้ถือว่า server, APK, log และ backup เป็นข้อมูลอ่อนไหว

## Hardcoded Secret Check / การตรวจ hardcoded secret

Current intended state:

- No real API token is hardcoded in Android source.
- No real API token is hardcoded in Python source.
- No real phone number, private IP, or local user path is required in README.
- `server\.env` contains the real token and is ignored by `.gitignore`.
- `server\sms.json` contains SMS content and is ignored by `.gitignore`.
- `server\.env.example` contains only placeholder values.

สถานะที่ตั้งใจไว้:

- ไม่มี API token จริงฝังใน source Android
- ไม่มี API token จริงฝังใน source Python
- README ไม่ต้องมีเบอร์จริง, IP จริง หรือ path user จริง
- `server\.env` เป็นไฟล์ local ที่มี token จริงและถูก ignore
- `server\sms.json` เป็นไฟล์ local ที่มี SMS จริงและถูก ignore
- `server\.env.example` มีแค่ placeholder

Recommended scan:

```powershell
rg -n --hidden "token|secret|password|api_key|authorization|bearer" -g "!android/app/build/**" -g "!android/build/**" -g "!server/sms.json" -g "!server/.env"
```

The scan will still show safe code paths that handle tokens, such as `Authorization: Bearer` parsing. That is expected. What should not appear is a real token value.

ผล scan จะยังเจอโค้ดที่จัดการ token เช่นการอ่าน `Authorization: Bearer` ซึ่งเป็นเรื่องปกติ สิ่งที่ไม่ควรเจอคือ token จริง

## Local Files That Must Stay Private / ไฟล์ local ที่ต้องเก็บเป็นส่วนตัว

Do not publish:

- `server\.env`
- `server\sms.json`
- `server\*.log`
- APKs already configured with a real token
- Android Studio local files such as `android\local.properties`

ห้ามเผยแพร่:

- `server\.env`
- `server\sms.json`
- `server\*.log`
- APK ที่ตั้งค่าด้วย token จริงแล้ว
- ไฟล์ local ของ Android Studio เช่น `android\local.properties`

## API Token / การใช้ API token

The server uses one shared API token from `READSMS_API_TOKEN`.

Server behavior:

- If `server\.env` is missing, the server creates it with a random token.
- Android clients must enter the same token.
- The token is accepted through `Authorization: Bearer <token>` or `X-API-Token`.
- The WebSocket viewer currently accepts `?token=<token>` because Android WebSocket headers can be awkward in some clients.

พฤติกรรมฝั่ง server:

- ถ้าไม่มี `server\.env` ระบบสร้าง token สุ่มให้
- Android ทุกเครื่องต้องใช้ token เดียวกัน
- API รับ token ผ่าน `Authorization: Bearer <token>` หรือ `X-API-Token`
- WebSocket viewer ตอนนี้รับ `?token=<token>` เพื่อให้ใช้งานกับ Android client ง่าย

For internet-facing production, avoid token in query strings. Put the server behind HTTPS and pass auth through headers or a short-lived session.

ถ้าจะเปิดออก internet จริง ควรหลีกเลี่ยง token ใน query string ใช้ HTTPS และ auth ผ่าน header หรือ session อายุสั้นแทน

## SMS Permission / สิทธิ์ SMS

Android permissions used:

- `READ_SMS`: read existing SMS for backfill
- `RECEIVE_SMS`: receive new SMS events
- `RECEIVE_BOOT_COMPLETED`: restart sync after reboot
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_DATA_SYNC`: keep collector sync alive
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: let the user allow background sync
- `POST_NOTIFICATIONS`: show collector/owner notifications

These permissions should be requested openly in the UI. Do not hide the app icon, hide notifications, or sync from a phone without consent.

สิทธิ์เหล่านี้ต้องขอแบบโปร่งใสใน UI ห้ามซ่อน icon, ซ่อน notification หรือ sync จากเครื่องที่ไม่ได้ยินยอม

## Data Retention / อายุข้อมูล

Default:

```text
READSMS_RETENTION_DAYS=1
```

The server keeps messages only for the configured window and purges older messages during normal operations.

ค่า default เก็บข้อความ 1 วัน และลบข้อความเก่าระหว่างการทำงานปกติ

## Storage / การเก็บข้อมูล

Current storage is plain JSON:

```text
server\sms.json
```

This is simple and good for a private MVP, but it is not enough for sensitive production use.

Recommended upgrades before real production:

- Encrypt the disk or the JSON content.
- Add per-device revoke/rotate tokens.
- Add HTTPS.
- Add audit logs that do not include SMS body.
- Add backup rules that exclude SMS data unless encrypted.
- Add a clear data deletion screen.

## Network Exposure / การเปิด network

Local Wi-Fi use is the intended default.

If exposing the server beyond a trusted LAN:

- Use HTTPS.
- Use a reverse proxy.
- Restrict firewall rules.
- Rotate tokens.
- Avoid WebSocket token in URL.
- Consider per-device credentials instead of one shared token.
- Add rate limiting.

## Android Vendor Notes / หมายเหตุมือถือบางยี่ห้อ

Some phones aggressively kill background apps. Collector reliability may need manual settings:

- Allow SMS permission.
- Allow notification permission.
- Set battery to unrestricted.
- Enable autostart if the ROM has it.
- Do not force stop the app.
- Keep the collector notification visible.

มือถือบางรุ่น kill background หนัก ต้องตั้งค่าเพิ่ม เช่น เปิด SMS permission, notification, unrestricted battery, autostart และอย่ากด force stop

## Public Release Checklist / Checklist ก่อนเผยแพร่ repo

Before publishing:

- Remove real `server\.env`.
- Remove real `server\sms.json`.
- Remove logs.
- Remove local Android Studio files.
- Confirm README uses placeholders only.
- Run the sensitive-term scan.
- Confirm the MIT license is intended for public release.
- Decide whether debug APKs should be published at all.

ก่อนเผยแพร่:

- ลบ `server\.env` จริง
- ลบ `server\sms.json` จริง
- ลบ log
- ลบไฟล์ local ของ Android Studio
- ตรวจ README ว่าใช้ placeholder เท่านั้น
- รัน scan คำอ่อนไหว
- ยืนยันว่าต้องการเผยแพร่ด้วย MIT license
- ตัดสินใจว่าจะเผยแพร่ debug APK หรือไม่
