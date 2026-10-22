#!/bin/bash
# Script to automate cleanup and copy of built APK output

echo "========================================="
echo "🧹 Cleaning and Copying Build Output"
echo "========================================="

# 1. Delete old APK in the root directory
if [ -f "./app-debug.apk" ]; then
    echo "🗑️ Deleting old app-debug.apk from root directory..."
    rm -f ./app-debug.apk
fi

# 2. Check if fresh APK exists in app build outputs and copy it
if [ -f "./app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "📦 Copying fresh app-debug.apk from app/build/outputs/apk/debug/ to root directory..."
    cp -f ./app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk
    mkdir -p ./.build-outputs
    cp -f ./app/build/outputs/apk/debug/app-debug.apk ./.build-outputs/app-debug.apk
    echo "✅ Success! File copied. Size: $(du -sh ./app-debug.apk | cut -f1)"
elif [ -f "./.build-outputs/app-debug.apk" ]; then
    echo "📦 Copying fresh app-debug.apk from .build-outputs/ to root directory..."
    cp -f ./.build-outputs/app-debug.apk ./app-debug.apk
    echo "✅ Success! File copied. Size: $(du -sh ./app-debug.apk | cut -f1)"
else
    echo "❌ Error: Fresh APK not found!"
    exit 1
fi

echo "========================================="
