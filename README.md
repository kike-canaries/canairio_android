# Pollution Reporter (hpma115s0)

**W A R N I N G :** Project in development

Android client and reporter for [esp32-hpma115s0](https://github.com/kike-canaries/esp32-hpma115s0) pollution sensor.

---
<a href="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/main.png"><img src="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/main.png" align="right" width="220" ></a>
<a href="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/scan.png"><img src="https://github.com/kike-canaries/android-hpma115s0/blob/master/screenshots/scan.png" align="right" width="220" ></a>
---

## TODO

- [X] BLE scanning and connecting 
- [X] BLE auto connect and reconnect
- [X] Receive data via BLE notification
- [X] Basic chart for PM 2.5 data
- [ ] BLE persist connection on background service
- [ ] MQTT subscription and publishing
- [ ] Google Maps pollution data


## Dependencies

- Android SDK and NDK
- Tested with gradle 4.4, SDK tools 26 and NDK 15

## Compiling

```bash
git clone https://github.com/kike-canaries/android-hpma115s0.git
cd android-hpma115s0.git && ./gradlew assembleDebug
```
