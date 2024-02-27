
# Blind-Assistant-app

This repository contains the Mobile Application code for the Multi Camera Blind Assistive Device.

## About the app

An innovative Android application designed to assist visually impaired users in accessing various system functions through intuitive touch screen or voice command interactions. 

## Setting Up

To get started, connect an Android device, build and install the Application through Android Studio.

## How To Use
- This application auto-enables Bluetooth when opened. This helps connect to the Raspberry pi module of the Assistive Device using bluetooth.
- Once the Raspberry Pi is connected to the Android Device, a list of buttons are displayed. The user can choose any of the assistive functions listed.
- To navigate through the application, the blind user may use Android's talkback facility. Talkback facility is available in Android devices which helps the blind users effectively use the android applications.
- The user can also choose the application functions without talkback. When they click a button, the button they selected is read out, giving an idea to the blind user which assistive function they are going to select. They can press the button again to confirm the choice.
- Once the user selects the assistive function, the mobile device sends the request to Raspberry Pi, which captures images and sends them to the cloud for processing.
- After cloud processing is done, the cloud sends an asynchronous trigger to the user's mobile app and the mobile app displays the output of the assistive task. Then the application provides audio and vibrational feedback for the assistive task.
- For vehicle detection and obstacle detection, the app enables and disable the functionality through bluetooth commands and it does not send any message to the cloud.
- The user may also use the `speak` functionality to use voice commands and execute their desired assistive task.
