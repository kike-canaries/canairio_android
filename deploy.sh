#!/bin/bash

md5oldapk=`md5sum app/build/outputs/apk/debug/app-debug.apk`
./mainframer ./remoteAssemble ./gradlew assembleDebug
md5newapk=`md5sum app/build/outputs/apk/debug/app-debug.apk`

if [ "$md5oldapk" != "$md5newapk" ]; then
  echo "deploying apk.."
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  echo "starting application.."
  pmstart pollution
else
  echo ""
  echo "warnning: the current apk is the same"
fi
