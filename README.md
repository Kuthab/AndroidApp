# Photos — CS 213 Android Project

An Android port of the JavaFX Photos application for a single-user personal device.

## Features

- **Albums** — create, rename, delete, open. Each entry shows a cover thumbnail and photo count.
- **Photos** — add from device storage (with persistent URI permission), remove, move between albums, view full-screen.
- **Slideshow** — manual forward/backward navigation through an album one photo at a time.
- **Tags** — `person` and `location` only, case-insensitive, displayed as colored chips on the photo screen, deletable.
- **Search** — across all albums by tag-value pairs with single / AND / OR combinators and starting-substring auto-complete.
- **Persistence** — albums, photos, and tags are serialized to internal storage and reloaded on launch.

## Build

- Open the project in Android Studio.
- Java 17, Gradle (Kotlin DSL), `compileSdk` 36, `minSdk` 26, target Pixel 6 (1080 x 2400, 420 dpi).
- No external image libraries — bitmap decoding is done in `edu.rutgers.photos.util.ImageLoader`.

## Project structure

```
app/src/main/
  java/edu/rutgers/photos/
    MainActivity.java          album list + search entry
    AlbumActivity.java         photo grid + add/remove/move
    PhotoActivity.java         full-screen view, slideshow, tags
    SearchActivity.java        cross-album search with auto-complete
    AlbumAdapter.java
    PhotoGridAdapter.java
    SearchResultAdapter.java
    model/
      Album.java
      Photo.java
      Tag.java
      DataStore.java           singleton + Java serialization
    util/ImageLoader.java
  res/
    layout/                    XML for all screens and dialogs
    drawable/, values/, xml/, mipmap-anydpi-v26/
```
