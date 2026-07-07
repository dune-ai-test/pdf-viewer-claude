# PDF Productivity — Android (Kotlin + Jetpack Compose)

A native Android PDF viewer app styled from `design.md` (Apple-HIG-inspired,
Material 3 / Jetpack Compose). No demo/sample data — everything shown is a
real file you opened.

## What's included

- **Library screen** — empty state until you open a real PDF; after that,
  shows a grid of your actually-opened files (name, real size, "Just now"),
  session-only (resets on app restart — see "Next steps").
- **+ FAB** — opens the system file picker (SAF) for any `.pdf` on the device.
- **"Open with" support** — the app now registers for `ACTION_VIEW` on
  `application/pdf` (both `content://` and `file://`), so it shows up in the
  Android share/open sheet when you tap a PDF anywhere else on the device
  (Files app, email, browser downloads, etc.) and jumps straight to the viewer.
- **Real PDF viewer** (`PdfViewerScreen`) — backed by `android-pdf-viewer`
  (Pdfium): real page rendering, swipe/zoom, and password support.
- **Password-protected PDFs** — a dialog prompts for the password; wrong
  password shows an inline error and lets you retry; cancel returns you back
  (to the Library, or exits the app if you opened the file via "Open with").
- **Theme layer** mapped 1:1 from `design.md` (`ui/theme/Color.kt`,
  `Type.kt`, `Shape.kt`) — colors, the exact type scale, and squircle radii.

## Fixed since last version

1. **"Couldn't open this PDF" on every real file** — the `.load()` call was
   inside `AndroidView`'s `update` block, which re-runs on every recomposition.
   Since the load itself flipped `isLoading` (read by this same composable),
   it created a reload loop that cancelled every in-flight render and always
   surfaced as a generic open failure — password or not. Fixed by moving the
   load into a single `LaunchedEffect(uri, password, loadAttempt)` that only
   fires when one of those actually changes.
2. **Demo/dummy data removed** — `PdfDocument`, `SampleData.kt`, and the
   sample `DocumentScreen` are gone. The Library now only ever shows PDFs you
   opened yourself, via a small in-memory `RecentPdfsStore`.
3. **"Open with" registration** — added a `VIEW` intent filter + `singleTask`
   launch mode in `AndroidManifest.xml`, and `MainActivity` now extracts the
   incoming `Uri` and jumps straight to the viewer.
4. Fixed the `Icons.Filled.Chat` deprecation warning from your build log
   (now `Icons.AutoMirrored.Filled.Chat`).

## Opening the project

1. Unzip.
2. Open in Android Studio — first sync needs internet (fetches
   `com.github.mhiew:android-pdf-viewer` from JitPack, already added to
   `settings.gradle.kts`).
3. Run on a device/emulator, minSdk 24.

## Verify "Open with" after installing

Long-press any `.pdf` in Files (or a downloaded PDF, or a PDF email
attachment) → Open with → "PDF Productivity" should appear in the list.

## Next steps (kept simple per request)

- Persist `RecentPdfsStore` with Room/DataStore so recents survive an app
  restart (currently in-memory only, per session).
- Implement the annotation tools (toolbar is currently visual/selectable only).
- Real page thumbnails on Library cards (currently a generic PDF glyph, to
  keep the Library screen fast without rendering every recent file up front).

## Project structure

```
app/src/main/java/com/productivity/pdf/
├── MainActivity.kt
├── model/RecentPdf.kt
├── data/RecentPdfsStore.kt
├── util/PdfFileUtils.kt
├── navigation/NavGraph.kt
└── ui/
    ├── theme/       Color.kt, Type.kt, Shape.kt, Theme.kt
    ├── components/  PdfCard.kt, TranslucentTopBar.kt, AnnotationToolbar.kt,
    │                PasswordPromptDialog.kt
    └── screens/     LibraryScreen.kt, PdfViewerScreen.kt
```
