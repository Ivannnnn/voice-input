# Cloud Voice Input — V0

A minimal Android speech-input activity intended to work with keyboards such as HeliBoard.

## What V0 does

It **does not record audio yet**.

It handles Android's:

`android.speech.action.RECOGNIZE_SPEECH`

and returns this fixed recognition result:

> Hello from Cloud Voice Input. The HeliBoard integration works.

The purpose is to prove the Android → HeliBoard integration before adding microphone recording, networking, and transcription.

## Build without Android Studio

The repository includes a GitHub Actions workflow.

1. Create a GitHub repository.
2. Upload/push all files from this project.
3. Open **Actions → Build Android APK**.
4. Run the workflow if it did not run automatically.
5. Open the completed run.
6. Download the `cloud-voice-input-debug` artifact.
7. Unzip it and install `app-debug.apk` on Android.

You may need to allow installation from unknown sources.

## Test inside the app

Open **Cloud Voice Input** and tap:

**Test recognition activity**

Then tap:

**Return test transcription**

A dialog should show:

`Hello from Cloud Voice Input. The HeliBoard integration works.`

## Test with HeliBoard

After installation, Android should see Cloud Voice Input as an app capable of handling `ACTION_RECOGNIZE_SPEECH`.

If your device asks which voice-input app to use when tapping HeliBoard's microphone, select **Cloud Voice Input**.

If another speech app is already the default, clear that app's default association first.

Then:

1. Open any text field.
2. Bring up HeliBoard.
3. Tap HeliBoard's microphone.
4. Cloud Voice Input should open.
5. Tap **Return test transcription**.
6. The test sentence should be inserted into the original text field.

## Next versions

V1:
- microphone permission
- record audio
- stop button
- local file

V2:
- configurable backend URL
- upload audio using HTTP
- parse transcript response

V3:
- silence detection / auto-stop
- language / model settings
- retries and error UI
- optional voice-IME implementation
