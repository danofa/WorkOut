# WorkOut

Android app - Allows you to setup named exercise sessions with time steps that beep to let you know when to change activities.

* Add named workout sessions
* Create time steps that signal when to change activity
* Able to run continuously or once
* Can run in background while you use other phone functions

![main](https://cloud.githubusercontent.com/assets/6975806/6030132/82238804-abf5-11e4-8ac6-e55e9617a12a.png)
![mainadd](https://cloud.githubusercontent.com/assets/6975806/6030134/824144fc-abf5-11e4-8b51-a116ba487068.png)
![sess](https://cloud.githubusercontent.com/assets/6975806/6030133/82403238-abf5-11e4-9492-6d114690a306.png)
![sessadd](https://cloud.githubusercontent.com/assets/6975806/6030135/82415a0a-abf5-11e4-9c0f-2c918381d7fd.png)

## What I coded:

This app was made by request of a friend and seeing as android can run java applications I decided it would be a great little introductory project.

The main activity pulls and stores the general sqlite data for each workout session upon startup and also launches the background timer service that talks to the workout session activity.

All running and data storage is kept to a minimum and on exit from the main activity it destroys all references.

## Where is it:

See the release page to download source or the signed .apk 

#### runs on API 16: Android 4.1 (Jelly Bean) and higher.
