#!/bin/bash
# Quick semi-auto file to build Windows SDK tools.
#
# Limitations:
# - Expects the emulator has been built first, will pick it up from prebuilt.
# - Run in Cygwin
# - Needs Cygwin package zip
# - Expects to have one of the existing SDK (Darwin or Linux) to build the Windows one

set -e  # Fail this script as soon as a command fails -- fail early, fail fast

SDK_ZIP="$1"
DIST_DIR="$2"

function die() {
  echo "Error:" $*
  echo "Aborting"
  exit 1
}

function check() {
    [ -f "$SDK_ZIP" ] || die "Pass the path of an existing Linux/Darwin SDK .zip as first parameter"
    [ -d "$DIST_DIR" ] || die "Pass the output directory as second parameter"

    # Use the BUILD_ID as SDK_NUMBER if defined, otherwise try to get it from the
    # provided zip filename.
    if [ -f config/build_id.make ]; then
        BUILD_ID=`cat config/build_id.make | sed -n '/BUILD_ID=/s/^[^=]\+=\(.*\)$/\1/p'`
        [ -n "$BUILD_ID" ] && SDK_NUMBER="$BUILD_ID"
    fi
    if [ -z "$SDK_NUMBER" ]; then
        # Look for a pattern like "anything_sdknumber.extension"
        # The pattern is now "any-thing_sdknumber_anything-else.extension"
        #
        # The bottom line is that the SDK number is whatever is enclosed by
        # the LAST couple of underscores. You can have underscores *before* the
        # SDK number if you want, but not after, e.g these are valid:
        #    android_sdk_4242_platform.zip or blah_42_.zip
        #
        # SDK_NUMBER will be empty if nothing matched.
        filename=`basename "$SDK_ZIP"`
        SDK_NUMBER=`echo $filename | sed -n 's/^.*_\([^_./]\+\)_[^_.]*\..*$/\1/p'`
    fi

    [ -n "$SDK_NUMBER" ] || die "Failed to extract the SDK number from $SDK_ZIP. Check its format."

    [ $OSTYPE == "cygwin" ] || die "This expects to run under Cygwin"
    [ -e `which zip` ] || die "Please install 'zip' package in Cygwin"
    [ -f "build/envsetup.sh" ] || die "Please run this from the 'android' directory"

    echo "Using SDK ${SDK_NUMBER}"
}

function build() {

    echo 
    echo "Building..."
    [ -n "$MAKE_OPT" ] && echo "Make options: $MAKE_OPT"
    . build/envsetup.sh
    make -j 4 emulator || die "Build failed"
    # Disable parallel build: it generates "permission denied" issues when
    # multiple "ar.exe" are running in parallel.
    make prebuilt adb fastboot aidl aapt dexdump dmtracedump mksdcard sqlite3 || die "Build failed"
}

function package() {
    echo
    echo "Packaging..."
    DEST_NAME="android-sdk_${SDK_NUMBER}_windows"
    DEST="$DIST_DIR/$DEST_NAME"
    DEST_NAME_ZIP="${DEST_NAME}.zip"

    # Unzip current linux/mac SDK and rename using the windows name
    [ -e "$DEST" ] && rm -rfv "$DEST"  # cleanup dest first if exists
    UNZIPPED=`basename "$SDK_ZIP"`
    UNZIPPED="$DIST_DIR/${UNZIPPED/.zip/}"
    [ -e "$UNZIPPED" ] && rm -rfv "$UNZIPPED"  # cleanup unzip dir (if exists)
    unzip "$SDK_ZIP" -d "$DIST_DIR"
    mv -v "$UNZIPPED" "$DEST"
    
    # USB Driver for ADB
    mkdir -pv $DEST/usb_driver/x86
    cp -rv development/host/windows/prebuilt/usb/driver/* $DEST/usb_driver/x86/
    mkdir -pv $DEST/usb_driver/amd64
    cp -rv development/host/windows/prebuilt/usb/driver_amd_64/* $DEST/usb_driver/amd64/

    # Remove obsolete stuff from tools
    TOOLS="$DEST/tools"
    LIB="$DEST/tools/lib"
    rm -v "$TOOLS"/{aapt,aidl,adb,emulator,traceview,draw9patch,hierarchyviewer,dx,dexdump,apkbuilder,ddms,dmtracedump,mksdcard,sqlite3,android}
    rm -v --force "$LIB"/*.so "$LIB"/*.jnilib

    # Copy all the new stuff in tools
    cp -v out/host/windows-x86/bin/*.{exe,dll} "$TOOLS"
    cp -v prebuilt/windows/swt/*.{jar,dll} "$LIB"
    # Do we want the emulator NOTICE in the tools dir? Cf http://b/930608.
    # If yes, uncomment the following line:
    # cp -v external/qemu/NOTICE "$TOOLS"/emulator_NOTICE.txt


    # We currently need libz from MinGW for aapt
    cp -v /cygdrive/c/cygwin/bin/mgwz.dll "$TOOLS"

    # Update a bunch of bat files
    cp -v dalvik/dx/etc/dx.bat "$TOOLS"
    cp -v development/tools/apkbuilder/etc/apkbuilder.bat "$TOOLS"
    cp -v development/tools/ddms/app/etc/ddms.bat "$TOOLS"
    cp -v development/tools/traceview/etc/traceview.bat "$TOOLS"
    cp -v development/tools/hierarchyviewer/etc/hierarchyviewer.bat "$TOOLS"
    cp -v development/tools/draw9patch/etc/draw9patch.bat "$TOOLS"
    cp -v development/tools/sdkmanager/app/etc/android.bat "$TOOLS"

    # Fix EOL chars to make window users happy - fix all files at the top level only
    find "$DIST_DIR" -maxdepth 1 -type f -print | xargs unix2dos -D

    # Done.. Zip it
    pushd "$DIST_DIR" > /dev/null
    [ -e "$DEST_NAME_ZIP" ] && rm -rfv "$DEST_NAME_ZIP"
    zip -9r "$DEST_NAME_ZIP" "$DEST_NAME" && rm -rfv "$DEST_NAME"
    popd > /dev/null
    echo "Done"
    echo
    echo "Resulting SDK is in $DIST_DIR/$DEST_NAME_ZIP"
}

check
build
package

echo "Done"
