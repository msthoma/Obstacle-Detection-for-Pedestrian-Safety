![master build](https://github.com/msthoma/ObsAI/workflows/master%20build/badge.svg)

<p align="center">
  <img src="/app/src/main/ic_launcher-web.png" alt="Obstacles app icon" width="200px"/>
</p>
<!--<p align="center">Obstacles app<p align="center">-->

# Obstacles app
App that allows pedestrians to report road obstacles to the authorities.

[Google Doc](https://docs.google.com/document/d/169LHY5ZmROTPiFGfMDgRNj5UOrf9_nqE8IXEGkCpt7A/edit?usp=sharing) 
with more information and documentation about the app.

[Google Colab notebook](https://colab.research.google.com/drive/1h4zyow50jhh2MWnp89w38lNbVp3PenAj) with Convolutional Neural Network trained for obstacle recognition.

## Repo information
The main code of the app is located at:
- Business logic:
  [/app/src/main/java/cy/org/rise/obsai/](/app/src/main/java/cy/org/rise/obsai/), in turn split into these directories:
  - [api](/app/src/main/java/cy/org/rise/obsai/api): Contains logic related to network connections, mostly with FIWARE.
  - [db](/app/src/main/java/cy/org/rise/obsai/db): Contains logic related to the app's Room database (SQLite), obstacle entity, etc.
  - [ui](/app/src/main/java/cy/org/rise/obsai/ui): Contains logic related to UI, activities, fragments, View Model etc.
  - [utils](/app/src/main/java/cy/org/rise/obsai/utils): Contains various helper functions. 
- Interface XML: [/app/src/main/res/](/app/src/main/res/)
- App manifest: [/app/src/main/AndroidManifest.xml](/app/src/main/AndroidManifest.xml)

## Building the app locally
- Use the green "Clone or download" button (up, right) and download the
  app files as a ZIP file.
- Download and install Android Studio.
- Go to File → Open..., and point Android Studio to the path where you
  extracted the ZIP file above.
- Alternatively, you can use New → Project from Version Control... →
  Git, and point Android Studio to this repo (you'll have to sign in to
  Github).
