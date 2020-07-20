Baohua Zhu <bz22@uw.edu>

Android App: TheftDetector

Introduction

This is a simple theft detecting system that contains an Android App and an Arduino. The idea is that the user
can put the Arduino to any door and the accelerometer sensor will start to detect the movement about opening the
door. When the sensor detects certain movement, the Arduino will generate an alarm to notify the user, and the user
can choose to cancel the alarm if he/she is in a safe situation. If the alarm is not cancelled within 15 seconds,
the Arduino will make the phone to call the user's emergency contact number.

Functions

1. Connect: Connect the App to the Arduino via BLE
2. Cancel: When the Arduino detects door movement, a dialog will pop up in the App, and the user can tap "cancel"
	   button on the dialog to cancel the alarm.
