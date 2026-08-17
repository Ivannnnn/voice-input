# Cloud Voice Input

A small Android voice-input app that lets HeliBoard use ElevenLabs Scribe v2 for high-quality cloud transcription.

The goal is simple:

1. Open any text field on Android.
2. HeliBoard appears as the normal keyboard.
3. Tap HeliBoard's microphone button.
4. Cloud Voice Input replaces the keyboard temporarily.
5. Tap Record and speak.
6. Tap Stop and transcribe.
7. The recording is uploaded to ElevenLabs Scribe v2.
8. The resulting transcript is inserted into the original text field.
9. The app switches back to the previous keyboard.

This gives system-wide high-quality speech-to-text without having to build or use a complete custom keyboard.

---

## Architecture

The system consists of three pieces:

### 1. HeliBoard

HeliBoard is the normal Android keyboard.

It provides the keyboard layout, autocorrect, typing, etc.

Cloud Voice Input does **not** try to replace HeliBoard as a normal keyboard. Instead, it registers itself as a special Android voice input method.

When the microphone button is pressed, HeliBoard temporarily hands input over to Cloud Voice Input.

### 2. Cloud Voice Input

This repository contains the Android APK.

Its main job is:

```text
HeliBoard microphone
        ↓
Cloud Voice Input voice IME
        ↓
record microphone to .m4a
        ↓
POST recording to ElevenLabs
        ↓
Scribe v2 transcription
        ↓
currentInputConnection.commitText(...)
        ↓
text inserted into current Android text field
        ↓
switch back to HeliBoard
