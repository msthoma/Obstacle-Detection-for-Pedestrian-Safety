![master build](https://github.com/msthoma/ObsAI/workflows/master%20build/badge.svg?branch=master)

<p align="center">
  <img src="/app/src/main/ic_launcher-web.png" alt="Obstacles app icon" width="200px"/>
</p>

# Obstacles app
App that allows pedestrians to report road obstacles to the authorities.

[Google Doc](https://docs.google.com/document/d/169LHY5ZmROTPiFGfMDgRNj5UOrf9_nqE8IXEGkCpt7A/edit?usp=sharing) 
with more information and documentation about the app.

[Google Colab notebook](https://colab.research.google.com/drive/1h4zyow50jhh2MWnp89w38lNbVp3PenAj)
with Convolutional Neural Network trained for obstacle recognition.

## Repo information
The main code of the app is located at:
- Business logic:
  [/app/src/main/java/cy/org/rise/obsai/](/app/src/main/java/cy/org/rise/obsai/),
  in turn split into these directories:
  - [api](/app/src/main/java/cy/org/rise/obsai/api): Logic for network
    connections with the iNicosia platform.
  - [data](/app/src/main/java/cy/org/rise/obsai/data): GPS and
    Orientation tracking, as well as CNN TensorFlow Lite related files.
  - [db](/app/src/main/java/cy/org/rise/obsai/db): Logic related to the
    app's Room database (SQLite), Obstacle entity, Repository, etc.
  - [ui](/app/src/main/java/cy/org/rise/obsai/ui): UI related logic
    (Activities, Fragments, ViewModel, Settings, etc.).
  - [utils](/app/src/main/java/cy/org/rise/obsai/utils): Contains various helper functions. 
- Interface XML files, string resources, drawables, icons:
  [/app/src/main/res/](/app/src/main/res/)
- Asset files, including TensorFlow Lite model:
  [/app/src/main/assets](/app/src/main/assets/)
- App manifest:
  [/app/src/main/AndroidManifest.xml](/app/src/main/AndroidManifest.xml)

## Building the app locally
- Use the green "Clone or download" button (up, right) and download the
  app files as a ZIP file.
- Download and install Android Studio.
- Go to File → Open…, and point Android Studio to the path where you
  extracted the ZIP file above.
- Alternatively, you can use New → Project from Version Control… →
  Git, and point Android Studio to this repo (you'll have to sign in to
  Github).
