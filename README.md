# CanAir.io Air quality Reporter 


<a href="https://play.google.com/store/apps/details?id=hpsaturn.pollutionreporter" target="_blank"><img src="https://github.com/kike-canaries/android-hpma115s0/blob/master/assets/googleplay/gplayicon.png" align="right" width="128" ></a>


[CanAirIO](http://canair.io) is a citizen science initiative for air quality tracking, visualization and dissemination by using PM2.5 particulate material sensors paired with your smartphone via bluetooth.

This code is for [CanAir.io](http://canair.io) Android client and reporter for [esp32-hpma115s0](https://github.com/kike-canaries/esp32-hpma115s0) pollution sensor.


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
- [X] Connect list records to record track
- [X] Connect Open Street Maps to pollution data
- [X] Firebase connection for publish reports
- [ ] Osmdroid clusters (for static points)
- [ ] Osmdroid routes (for line or dinamic points)
- [ ] Export data to json or others

## Dependencies

- Android SDK and NDK
- Tested with gradle 4.4, SDK tools 26 and NDK 15
- CMake

## Requirements

This application uses a Firebase Database instance to store air quality reports.

For local development, you will need to create a database in the [Firebase Console](https://console.firebase.google.com/) using `hpsaturn.pollutionreporter` as the application identifier and retrieve a `google-services.json` file. See instructions [here](https://support.google.com/firebase/answer/7015592?hl=en).

## Compiling

* Create a local keystore:

`keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000`

* Add a reference to the keystore to your environment variables by adding the following to `~/.bash_profile` or `~/.bashrc` depending on your local setup.

```bash
export airStoreFile=<your_test.keystore>
export airStorePassword=<your_test_keystore_password>
export airKeyAlias=<your_test_keystore_alias>
export airKeyPassword=<your_key_password>
```



```bash
git clone https://github.com/kike-canaries/android-hpma115s0.git
cd android-hpma115s0.git && ./gradlew assembleDebug
```

## Credits

<div>Icons made by <a href="https://www.flaticon.com/authors/prosymbols" title="Prosymbols">Prosymbols</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
