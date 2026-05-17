package com.readsms.app

class AppText private constructor(private val th: Boolean) {
    val languageCode = if (th) "th" else "en"
    val languageTitle = if (th) "ภาษา" else "Language"
    val languageEnglish = "English"
    val languageThai = "ไทย"

    val ready = if (th) "พร้อมใช้งาน" else "Ready"
    val serverTokenRequired = if (th) "ต้องกรอก Server URL และ API token ก่อนใช้งาน" else "Server URL and API token are required."
    val completeSetup = if (th) "ตั้งค่าการเชื่อมต่อก่อน" else "Complete setup first"
    val checkingServer = if (th) "กำลังตรวจสอบเซิร์ฟเวอร์" else "Checking server"
    val connected = if (th) "เชื่อมต่อสำเร็จ" else "Connected"
    val cannotConnectServer = if (th) "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้" else "Cannot connect to server."
    val fixConnectionSetup = if (th) "ต้องแก้ค่าการเชื่อมต่อ" else "Fix connection setup"
    val saveToVerify = if (th) "บันทึกเพื่อตรวจสอบค่าชุดนี้" else "Save to verify these settings."
    val addServerToken = if (th) "กรอก Server URL และ API token ก่อน" else "Add Server URL and API token"
    val syncingInbox = if (th) "กำลังดึงข้อความ" else "Syncing messages"
    val refreshFailed = if (th) "รีเฟรชไม่สำเร็จ" else "Refresh failed"
    val fixServerFirst = if (th) "ตรวจสอบเซิร์ฟเวอร์ก่อน" else "Fix server setup first"
    val allowSmsFirst = if (th) "อนุญาต SMS ก่อน" else "Allow SMS permission first"
    val readingLocalSms = if (th) "กำลังอ่านข้อความในเครื่องนี้" else "Reading SMS from this phone"
    val cannotReadLocalSms = if (th) "อ่านข้อความในเครื่องไม่ได้" else "Cannot read local SMS"
    val smsPermissionGranted = if (th) "อนุญาต SMS แล้ว" else "SMS permission granted"
    val smsPermissionMissing = if (th) "ยังไม่ได้อนุญาต SMS" else "SMS permission missing"
    val notificationPermissionMissing = if (th) "ยังไม่ได้อนุญาตแจ้งเตือน" else "Notification permission missing"
    val connectingRealtime = if (th) "กำลังเชื่อมต่อเรียลไทม์" else "Connecting realtime"
    val realtimeReady = if (th) "เรียลไทม์พร้อมใช้งาน" else "Realtime connected"
    val newMessageReceived = if (th) "มีข้อความใหม่" else "New message received"
    val realtimeDisconnectedRetry = if (th) "เรียลไทม์หลุด กำลังต่อใหม่" else "Realtime disconnected, reconnecting"
    val realtimeClosed = if (th) "เรียลไทม์ปิดอยู่" else "Realtime closed"

    val messagesTitle = if (th) "ข้อความ" else "Messages"
    val collectorTitle = if (th) "ตัวเก็บ SMS" else "Collector"
    val pollingOnline = if (th) "ออนไลน์แบบ polling" else "Online via polling"
    val checkingSetup = if (th) "กำลังตรวจสอบค่า" else "Checking setup"
    val setupRequired = if (th) "ต้องตั้งค่าก่อน" else "Setup required"
    val backgroundSmsSync = if (th) "ซิงก์ SMS เบื้องหลัง" else "Background SMS sync"
    val syncQueued = if (th) "สั่งซิงก์แล้ว" else "Sync queued"

    val setupCheckingConnection = if (th) "กำลังตรวจสอบการเชื่อมต่อ" else "Checking connection"
    val setupServerFirst = if (th) "ตั้งค่าเซิร์ฟเวอร์ก่อน" else "Set up server first"
    val setupConnectionInvalid = if (th) "ค่าการเชื่อมต่อยังไม่ถูก" else "Connection needs fixing"
    val setupConnected = if (th) "เชื่อมต่อแล้ว" else "Connected"
    val setupBody = if (th) "แอปจะตรวจ Server URL และ API token ทุกครั้งก่อนเริ่มซิงก์" else "The app checks the Server URL and API token before syncing."
    val editSetup = if (th) "แก้ไขค่า" else "Edit setup"

    val noMessages = if (th) "ยังไม่มีข้อความ" else "No messages yet"
    val noMatchingMessages = if (th) "ไม่เจอข้อความที่ค้นหา" else "No matching messages"
    val noMessagesBody = if (th) "เมื่อเครื่อง B หรือ C ซิงก์สำเร็จ บทสนทนาจะขึ้นที่นี่อัตโนมัติ" else "When B or C syncs, conversations appear here automatically."
    val noMatchingBody = if (th) "ลองค้นหาด้วยชื่อผู้ส่ง เครื่อง หรือข้อความอื่น" else "Try another sender, device, or message text."
    val refresh = if (th) "รีเฟรช" else "Refresh"
    val settings = if (th) "ตั้งค่า" else "Settings"
    val back = if (th) "กลับ" else "Back"
    val searchMessages = if (th) "ค้นหาข้อความ" else "Search messages"
    val realtime = "Realtime"
    val polling = "Polling"
    val offline = if (th) "ออฟไลน์" else "Offline"

    val allRead = if (th) "อ่านแล้วทั้งหมด" else "All read"
    val unread = if (th) "ยังไม่อ่าน" else "unread"
    val newLabel = if (th) "ใหม่" else "New"
    val unknownSender = if (th) "ไม่ทราบผู้ส่ง" else "Unknown sender"

    val syncStatus = if (th) "สถานะซิงก์" else "Sync status"
    val server = if (th) "เซิร์ฟเวอร์" else "Server"
    val readyValue = if (th) "พร้อม" else "Ready"
    val missingValue = if (th) "ยังไม่ครบ" else "Missing"
    val smsAccess = if (th) "สิทธิ์ SMS" else "SMS access"
    val allowed = if (th) "อนุญาตแล้ว" else "Allowed"
    val needsPermission = if (th) "ต้องอนุญาต" else "Needs permission"
    val battery = if (th) "แบตเตอรี่" else "Battery"
    val unrestricted = if (th) "ไม่จำกัด" else "Unrestricted"
    val restricted = if (th) "ถูกจำกัด" else "Restricted"
    val keepAlive = if (th) "ซิงก์ตลอดเวลา" else "Keep alive"
    val on = if (th) "เปิด" else "On"
    val off = if (th) "ปิด" else "Off"
    val queue = if (th) "คิวส่ง" else "Queue"
    val allowSmsAccess = if (th) "อนุญาตอ่าน SMS" else "Allow SMS access"
    val openAppPermissions = if (th) "เปิดหน้าสิทธิ์แอป" else "Open app permissions"
    val openSmsPermissionPath = if (th) "เปิด Permissions > SMS > Allow" else "Open Permissions > SMS > Allow"
    val allowBackgroundSync = if (th) "อนุญาตซิงก์เบื้องหลัง" else "Allow background sync"
    val startKeepAliveSync = if (th) "เปิดซิงก์ตลอดเวลา" else "Start keep-alive sync"
    val pullLastDay = if (th) "ดึง SMS 1 วันล่าสุด" else "Pull last 1 day"
    val syncNow = if (th) "ซิงก์ตอนนี้" else "Sync now"

    val connectionSetup = if (th) "ตั้งค่าการเชื่อมต่อ" else "Connection setup"
    val apiToken = "API token"
    val serverUrl = "Server URL"
    val deviceName = if (th) "ชื่อเครื่อง" else "Device name"
    val deviceId = "Device ID"
    val hideApiToken = if (th) "ซ่อน API token" else "Hide API token"
    val showApiToken = if (th) "แสดง API token" else "Show API token"
    val checking = if (th) "กำลังตรวจสอบ..." else "Checking..."
    val checkAndSave = if (th) "ตรวจสอบและบันทึก" else "Check and save"

    val setupPanelMissing = if (th) "ต้องตั้งค่าก่อน" else "Setup required"
    val setupPanelChecking = if (th) "กำลังตรวจสอบการเชื่อมต่อ" else "Checking connection"
    val setupPanelValid = if (th) "เชื่อมต่อได้แล้ว" else "Connection verified"
    val setupPanelInvalid = if (th) "เชื่อมต่อไม่ได้" else "Connection failed"
    val setupPanelMissingBody = if (th) "กรอก Server URL และ API token ก่อนเริ่มใช้งาน" else "Enter the Server URL and API token before syncing."
    val setupPanelCheckingBody = if (th) "กำลังทดสอบ Server URL และ API token ที่บันทึกไว้" else "Testing the saved Server URL and API token."
    val setupPanelValidBody = if (th) "เครื่องนี้ติดต่อ backend ได้เรียบร้อย" else "This device can contact the backend."
    val setupPanelInvalidBody = if (th) "ตรวจ Server URL, port, Wi-Fi และ API token อีกครั้ง" else "Check the Server URL, port, Wi-Fi, and API token."

    val ownerMode = if (th) "เครื่องหลัก" else "Owner"
    val collectorMode = if (th) "เครื่องรอง" else "Collector"

    val ownerSmsChannelName = if (th) "แจ้งเตือน SMS แบบเงียบ" else "Silent SMS notifications"
    val ownerSmsChannelDescription = if (th) "แจ้งเตือน SMS ใหม่แบบไม่มีเสียงสำหรับเครื่อง Owner" else "Silent realtime SMS notifications for owner devices"
    val fromDevicePrefix = if (th) "จาก" else "From"
    val readSmsReceived = if (th) "ReadSMS ได้รับข้อความใหม่" else "ReadSMS received new messages"

    val collectorStarting = if (th) "กำลังเริ่ม keep-alive sync" else "Starting keep-alive sync"
    val collectorSyncFailedPrefix = if (th) "ซิงก์ไม่สำเร็จ" else "Sync failed"
    val collectorWaitingSetup = if (th) "รอการตั้งค่าเซิร์ฟเวอร์" else "Waiting for server setup"
    val collectorWaitingSmsPermission = if (th) "รอสิทธิ์ SMS" else "Waiting for SMS permission"
    val collectorChannelName = if (th) "ซิงก์ Collector ตลอดเวลา" else "Collector keep-alive sync"
    val collectorChannelDescription = if (th) "ช่วยให้ SMS sync ทำงานบนเครื่อง Android ที่จำกัดเบื้องหลัง" else "Keeps SMS sync running on restricted Android devices"
    val collectorNotificationTitle = if (th) "ReadSMS Collector กำลังทำงาน" else "ReadSMS Collector is running"

    fun updated(count: Int, newCount: Int): String {
        return if (newCount > 0) {
            if (th) "อัปเดต $count ข้อความ, ใหม่ $newCount" else "Updated $count messages, $newCount new"
        } else {
            if (th) "อัปเดต $count ข้อความ" else "Updated $count messages"
        }
    }

    fun readQueued(readCount: Int, addedCount: Int): String =
        if (th) "อ่าน $readCount SMS, เข้าคิว $addedCount" else "Read $readCount SMS, queued $addedCount"

    fun messageCount(count: Int): String =
        if (th) "$count ข้อความ" else "$count messages"

    fun pendingCount(count: Int): String =
        if (th) "$count ค้างส่ง" else "$count pending"

    fun threadSubtitle(deviceId: String, unreadCount: Int, count: Int): String =
        if (unreadCount > 0) {
            if (th) "$deviceId • $unreadCount ยังไม่อ่าน • $count SMS" else "$deviceId • $unreadCount unread • $count SMS"
        } else {
            if (th) "$deviceId • อ่านแล้วทั้งหมด • $count SMS" else "$deviceId • all read • $count SMS"
        }

    fun newMessages(count: Int): String =
        if (th) {
            if (count == 1) "1 ข้อความใหม่" else "$count ข้อความใหม่"
        } else {
            if (count == 1) "1 new message" else "$count new messages"
        }

    fun newSmsTitle(count: Int): String =
        if (th) "มี SMS ใหม่ $count ข้อความ" else "$count new SMS"

    fun collectorStatus(read: Int, queued: Int, synced: Int, pending: Int): String =
        if (th) "อ่าน $read, เข้าคิว $queued, ส่งแล้ว $synced, ค้าง $pending" else "Read $read, queued $queued, synced $synced, pending $pending"

    companion object {
        val English = AppText(th = false)
        val Thai = AppText(th = true)

        fun from(code: String): AppText = if (code == "th") Thai else English
    }
}
