
[![Actions Status](https://github.com/kike-canaries/canairio_android/workflows/Android%20CI/badge.svg)](https://github.com/kike-canaries/android-hpma115s0/actions) ![ViewCount](https://views.whatilearened.today/views/github/kike-canaries/canairio_android.svg) [![Liberapay Status](http://img.shields.io/liberapay/receives/CanAirIO.svg?logo=liberapay)](https://liberapay.com/CanAirIO)  

# CanAirIO Air quality Reporter

<a href="images/main.jpg"><img src="images/main.jpg" align="right" width="220" ></a>


[CanAirIO](https://canair.io) is a citizen science initiative for air quality tracking, visualization and dissemination by using PM2.5 particulate material sensors paired with your smartphone via bluetooth.

This code is for [CanAir.io](https://canair.io) Android app that using a [DIY device](https://github.com/kike-canaries/canairio_firmware#canairio-firmware) that handle a pollution sensor (Honeywell, Sensirion, Plantower, Panasonic) or others air quality sensors.


# Installation

<a href="https://play.google.com/store/apps/details?id=hpsaturn.pollutionreporter" target="_blank"><img src="https://raw.githubusercontent.com/kike-canaries/android-hpma115s0/master/assets/googleplay/gplayicon.png" align="right" width="128" ></a>

You can install it from the [release section](https://github.com/kike-canaries/esp32-hpma115s0/releases) downloading the last signed apk or installing from [GooglePlay](https://play.google.com/store/apps/details?id=hpsaturn.pollutionreporter)

# Building

## Dependencies

- Android SDK
- CMake

## Requirements

Please first clone the project with all submodules:

```bash
git clone --recursive https://github.com/kike-canaries/canairio_android.git
```

### Firebase

This application uses a Firebase Database instance to store mobile air quality reports, 

For local development, you will need to create a database in the [Firebase Console](https://console.firebase.google.com/) using `hpsaturn.pollutionreporter` as the application identifier and retrieve a `google-services.json` file. See instructions [here](https://support.google.com/firebase/answer/7015592?hl=en).

After that copy this file into the project:

```bash
cd canairio_android && cp ~/google-services.json app/
```

### Aqicn API key

Please put your Aqicn API key in `app/src/main/res/values/api_aqicn.xml` or create a fake file like with:

``` xml
<resources>
    <string name="api_aqicn_key">7cbbbb864b9c0755b8xxxxyyy</string>
</resources>
```

## Compiling

```bash
./gradlew assembleDebug
```

# CanAirIO Device firmare

Please see the firmware [documentation](https://github.com/kike-canaries/canairio_firmware#canairio-firmware) for using and configure your device.

# Usage

For now you need any Android device with Bluetooth 4 or above. You can download the CanAirIO app from [GooglePlay](https://play.google.com/store/apps/details?id=hpsaturn.pollutionreporter), keep in mind that it is in continuos development then please any feedback, report errors, or any thing please let us knowed it via our [contact form](http://canair.io/#three) or on our [Telegram chat](https://t.me/canairio)

You have **two configuration options or modes** of your CanAirIO device from the app:

## Mobile Station Mode

For record tracks on your device (Sdcard) or publish it to the cloud (share), please follow the next steps:

### Connection to device

<a href="https://github.com/kike-canaries/esp32-hpma115s0/blob/master/images/device_connection.jpg" target="_blank"><img src="https://raw.githubusercontent.com/kike-canaries/esp32-hpma115s0/master/images/device_connection.jpg" width="512" align="center" ></a>

### Recording track and share

<a href="https://github.com/kike-canaries/esp32-hpma115s0/blob/master/images/app_track_record.jpg" target="_blank"><img src="https://raw.githubusercontent.com/kike-canaries/esp32-hpma115s0/master/images/app_track_record.jpg" width="512" align="center" ></a>

**NOTE**: Also all recorded tracks will be saved in the `/sdcard/canairio/` directory on `json` format.

---

## Fixed Station Mode

<img width="640" src="images/influxdb_grafana.jpg">

Also, you can connect your CanAirIO device to the WiFi and leave this like a fixed station. In this mode you only need the Android app only for initial settings, after that the device could be publish data without the phone. For this you need configure it in `settings` section:

### Settings

<img align="right" width="400" src="images/canairio_app_settings.png">


- **Station Name**: for example: `PM25_Berlin_Pankow`
- **Wifi Name and Password**:
  - Your Wifi network credentials.
  - Save the credentials with the switch.
- **InfluxDB Cloud**: add the next values,
  - Database name: `canairio`  
  - Hostname: `influxdb.canair.io`
  - Save the settings with the switch.

The data will be configured and showed in [CanAirIO Grafana Server](https://bit.ly/3bLpz0H).

### Settings Tools

- Reboot device: Only for restart your CanAirIO device
- Factory Reset: For set all settings to default on your CanAirIO device

# Supporting the project

If you want to contribute to the code or documentation, consider posting a bug report, feature request or a pull request.

When creating a pull request, we recommend that you do the following:

- Clone the repository
- Create a new branch for your fix or feature. For example, git checkout -b fix/my-fix or git checkout -b feat/my-feature.
- Run to any clang formatter if it is a code, for example using the `vscode` formatter. We are using Google style. More info [here](https://clang.llvm.org/docs/ClangFormatStyleOptions.html)
- Document the PR description or code will be great
- Target your pull request to be merged with `devel` branch

Also you can make a donation, be a patreon or buy a device:  

<a href="images/ethereum_donation_address.png" target="_blank"><img src="images/ethereum_donation_address.png" align="right" width="220" margin-left="10px" ></a>

- Via **Ethereum**: `0x1779cD3b85b6D8Cf1A5886B2CF5C53a0E072C108`
- Via **Liberapay**: [CanAirIO in LiberaPay](https://liberapay.com/CanAirIO)
- **Buy a device**: [CanAirIO Bike in Tindie](https://www.tindie.com/products/hpsaturn/canairio-bike/)
- [Inviting us **a coffee**](https://www.buymeacoffee.com/hpsaturn) 


## TODO

- [X] BLE scanning and connecting 
- [X] Receive data via BLE notification
- [X] Basic chart for PM 2.5 data
- [X] BLE persist connection on background service
- [X] List recorded tracks fragment
- [X] Firebase connection for publish reports
- [X] Open Street map fragment
- [x] Export data to json on external storage (SD)
- [x] Osmdroid routes (for line or dinamic points)
- [ ] Add other air quality APIs to map (AQICN ie)
- [ ] Flutter migration for have to iOS app
- [ ] Osmdroid clusters (for static points)

# Credits

<div>Icons made by <a href="https://www.flaticon.com/authors/prosymbols" title="Prosymbols">Prosymbols</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
