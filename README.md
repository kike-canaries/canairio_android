# Pollution Reporter (hpma115s0)

**W A R N I N G :** Project in development

Android client and reporter for [esp32-hpma115s0](https://github.com/kike-canaries/esp32-hpma115s0) pollution sensor.

---
<a href="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/main.jpg"><img src="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/main.jpg" align="right" width="512" ></a>
---

## TODO

- [X] BLE scanning and connecting 
- [X] BLE auto connect and reconnect
- [X] Receive data via BLE notification
- [X] Basic chart for PM 2.5 data
- [X] BLE persist connection on background service
- [X] Recoding data in the phone
- [X] List recorded tracks fragment
- [X] Open Street map fragment
- [ ] Connect list records to record track
- [ ] Connect Open Street Maps to pollution data
- [ ] MQTT subscription and publishing

## Dependencies

- Android SDK and NDK
- Tested with gradle 4.4, SDK tools 26 and NDK 15

## Compiling

```bash
git clone https://github.com/kike-canaries/android-hpma115s0.git
cd android-hpma115s0.git && ./gradlew assembleDebug
```
