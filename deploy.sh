#!/bin/bash

######################################################
# CanAirIO deploy release utility
#
# Author: @hpsaturn
# 2021
######################################################

SRC_REV=`cat gradle.properties | grep mVersionCode | sed -n -e 's/^.*mVersionCode=//p'`
SRC_VER=`cat gradle.properties | grep mVersionName | sed -n -e 's/^.*mVersionName=//p'`
DATE=`date +%Y%m%d`
RELDIR="releases"
RELNAME="rev${SRC_REV}v${SRC_VER}_signed${DATE}.apk" 
OUTPUT="${RELDIR}/${RELNAME}" 

showHelp () {
  echo ""
  echo "************************************************"
  echo "** Build and deploy tag and release           **"
  echo "************************************************"
  echo ""
  echo "Usage alternatives:"
  echo ""
  echo "./deploy clean"
  echo "./deploy build"
  echo "./deploy publish"
  echo ""
}

clean () {
  ./gradlew clean
}

build () {

  echo ""
  echo "***********************************************"
  echo "** Building $RELNAME"
  echo "***********************************************"
  echo ""
  ./gradlew clean
  ./gradlew assembleRelease
  cp app/build/outputs/apk/release/app-release.apk $OUTPUT
  echo ""
  echo ""
  echo "***********************************************"
  echo "************** Build done *********************" 
  echo "***********************************************"
  echo ""
  md5sum $OUTPUT
  echo ""
}

publish_release () {
  echo ""
  echo "***********************************************"
  echo "********** Publishing release *****************" 
  echo "***********************************************"
  echo ""
  COMMIT_LOG=`git log -1 --format='%ci %H %s'`
  github-release upload --owner kike-canaries --repo canairio_android --tag "rev${SRC_REV}" --release-name "rev${SRC_REV} v${SRC_VER}" --body "${COMMIT_LOG}" $OUTPUT
}

current_branch=`git rev-parse --abbrev-ref HEAD` 

if [ ${current_branch} != "release" ]; then
  echo ""
  echo "Error: you are in ${current_branch} branch please change to release branch."
  echo ""
  exit 1
fi 

if [ "$1" = "" ]; then
  showHelp
else
  case "$1" in
    clean)
      clean
      ;;

    help)
      showHelp
      ;;

    --help)
      showHelp
      ;;

    -help)
      showHelp
      ;;

    print)
      printOutput
      ;;

    publish)
      publish_release
      ;;

    *)
      build $1
      ;;
  esac
fi

exit 0

