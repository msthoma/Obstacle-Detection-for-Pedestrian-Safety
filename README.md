<p align="center">
  <img src="/app/src/main/ic_launcher-web.png" alt="Obstacles app icon" width="140px"/>
</p>

# Obstacles: an app for reporting obstacles on pavements

[![Paper DOI](https://img.shields.io/badge/DOI-10.1007%2F978--3--030--76063--2__25-blue.svg)](https://doi.org/10.1007/978-3-030-76063-2_25)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An Android app that lets pedestrians report obstacles blocking a pavement, such as a car parked across it, a pot-hole, a bin or an overgrown shrub. The person photographs the obstacle, a convolutional neural network running on the phone suggests what kind of obstacle it is, and the report goes to the city with its location, the phone's orientation and the photo attached.

The app was built at the RISE Research Centre in Nicosia, Cyprus (now the [CYENS Centre of Excellence](https://www.cyens.org.cy)), as part of the study published as Thoma et al. (2021), [_"A Smartphone Application Designed to Detect Obstacles for Pedestrians' Safety"_](https://doi.org/10.1007/978-3-030-76063-2_25). The reports it produced fed the iNicosia smart-city platform.

## Publication

> Thoma, M., Theodosiou, Z., Partaourides, H., Tylliros, C., Antoniades, D., Lanitis, A. (2021). *A Smartphone Application Designed to Detect Obstacles for Pedestrians' Safety*. In: Paiva, S., Lopes, S.I., Zitouni, R., Gupta, N., Lopes, S.F., Yonezawa, T. (eds) Science and Technologies for Smart Cities. SmartCity360° 2020. Lecture Notes of the Institute for Computer Sciences, Social Informatics and Telecommunications Engineering. Springer, Cham, pp. 358-371. <https://doi.org/10.1007/978-3-030-76063-2_25>

For machine-readable citation metadata, see [`CITATION.cff`](CITATION.cff).

## What the app does

- Takes a photo of the obstacle through an in-app camera, with a cropping and rotation step.
- Classifies the photo on-device with a TensorFlow Lite convolutional neural network, then offers the five most likely obstacle types for the person to pick from, or lets them type their own.
- Records the location from GPS, and lets the person correct it by long-pressing a Google Map.
- Records the phone's orientation from the accelerometer and compass at the moment of the photo, which tells the city which way the camera was pointing.
- Stores each report in a local SQLite database first, so the app works with no signal, then uploads in the background through WorkManager once a connection is available. Uploads over mobile data are off by default.
- Uploads the photo to a MinIO object store and the report itself to the iNicosia platform, authenticating through FIWARE Keyrock.
- Speaks English and Greek.

## App screenshots

![Screenshots of the app: the empty obstacle list, the camera, the type-selection dialog and a filled-in report](/app/src/main/res/drawable/appintrotutorial.jpg)

## The neural network

The bundled model, [`cnn128RGB.tflite`](/app/src/main/assets/cnn128RGB.tflite), classifies a cropped photo into the 15 obstacle types listed in [`cnnRGB_labels.txt`](/app/src/main/assets/cnnRGB_labels.txt), among them pot-holes, parked vehicles, bins, parking meters, traffic cones and missing pavement. The app shows the five highest-scoring types in a dialog rather than deciding for the person, because a photo of a pavement usually contains several of them at once. [`CnnClassifier.kt`](/app/src/main/java/cy/org/rise/obsai/data/CnnClassifier.kt) holds the inference code, which crops the photo square, resizes it to whatever the model asks for, normalises it and runs the interpreter.

The paper describes how the model was trained and how it performed. The image below, shown inside the app to explain what the network does, marks the obstacles it found in four street photos.

![Four street photos with the obstacles the network detected marked and labelled](/app/src/main/res/drawable/cnnexplanation.jpg)

## Repository contents

The app is written in Kotlin, and its code sits under [`/app/src/main/java/cy/org/rise/obsai/`](/app/src/main/java/cy/org/rise/obsai/):

- [`api`](/app/src/main/java/cy/org/rise/obsai/api): Retrofit client for the iNicosia platform, the FIWARE Keyrock login call, the MinIO photo uploader and the WorkManager job that retries failed uploads.
- [`data`](/app/src/main/java/cy/org/rise/obsai/data): GPS and orientation tracking as LiveData, and the TensorFlow Lite classifier.
- [`db`](/app/src/main/java/cy/org/rise/obsai/db): the Room database, the `Obstacle` entity, the DAO and the repository.
- [`ui`](/app/src/main/java/cy/org/rise/obsai/ui): activities, fragments, the view model, the settings screen and the introductory tutorial.
- [`utils`](/app/src/main/java/cy/org/rise/obsai/utils): helper functions, the session manager and the `Application` subclass.

Everything else follows the standard Android layout: interface XML, string resources and drawables in [`/app/src/main/res/`](/app/src/main/res/), the model and label files in [`/app/src/main/assets/`](/app/src/main/assets/), and the manifest at [`/app/src/main/AndroidManifest.xml`](/app/src/main/AndroidManifest.xml).

## Status

Research code from 2020, published as the record of the app built for the paper, and no longer maintained. It targets Android SDK 30 with the Gradle and Android Gradle Plugin versions of that year, and it pulls nightly TensorFlow Lite builds, so building it today takes some updating.

## Configuration

The app reads several values that ship as placeholders. Fill in the ones you need:

| Value | Where | Needed for |
| --- | --- | --- |
| Google Maps API key | `app/src/debug/res/values/google_maps_api.xml` and `app/src/release/res/values/google_maps_api.xml` | the map on the report screen |
| MinIO endpoint, access key and secret key | [`MinIOUploader.kt`](/app/src/main/java/cy/org/rise/obsai/api/MinIOUploader.kt) | uploading obstacle photos |
| Keyrock and iNicosia base URLs | [`INicosiaApi.kt`](/app/src/main/java/cy/org/rise/obsai/api/INicosiaApi.kt) | logging in and submitting reports |
| Crash-report email address | [`CustomApplication.kt`](/app/src/main/java/cy/org/rise/obsai/utils/CustomApplication.kt) | ACRA crash reports |

## Licence

[MIT](LICENSE).
