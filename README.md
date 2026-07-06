# PDF Productivity — Android (Kotlin + Jetpack Compose)

A native Android implementation of the design system in `design.md`: a high-utility
PDF reading/annotation app with an Apple-HIG-inspired look (Corporate Modern +
Glassmorphism), built with Material 3 / Jetpack Compose.

## What's included

- **Full Gradle project** — open it directly in Android Studio (Hedgehog or newer).
- **Library screen** — grid of PDF cards (thumbnail "well", title, page count/size,
  favorite star), translucent large-title top bar, FAB to import.
  - Tapping a sample card opens a placeholder viewer (dummy data — no real file).
  - Tapping the **+** FAB opens the system file picker, and any real PDF you pick
    opens in the real viewer below.
- **Real PDF viewer** (`PdfViewerScreen`) — backed by `android-pdf-viewer`
  (Pdfium), so it renders actual pages, supports pinch-zoom/swipe, and **handles
  password-protected PDFs**: if the file is encrypted, a password dialog appears
  (styled to match the design system); wrong passwords show an inline "incorrect
  password" message and let you retry.
- **Document screen** — placeholder page viewer with a lifted "Active Document"
  card and a floating pill-shaped annotation toolbar (Highlight / Draw / Text /
  Comment), matching the `design.md` Components section. (This one is still the
  sample/demo screen for the dummy library cards.)
- **Theme layer** mapped 1:1 from `design.md`:
  - `ui/theme/Color.kt` — every token from the `colors:` front-matter (light
    scheme verbatim; dark scheme derived from the provided inverse/fixed tokens).
  - `ui/theme/Type.kt` — the exact type scale (`nav-title`, `headline-lg`,
    `body-main`, `label-caps`, `caption`, etc.) using Inter (falls back to the
    system font; see note below).
  - `ui/theme/Shape.kt` — squircle corner radii (10px buttons/inputs, 16px
    cards/modals, pill shapes for chips/toolbars).

### Password-protected PDFs — how it works

1. You pick a `.pdf` via the FAB → system file picker.
2. The viewer tries to open it. If Pdfium reports a password error, a dialog
   asks for the password.
3. Entering it re-attempts the load with that password. Wrong password →
   dialog reappears with an "incorrect password" message. Cancel → goes back
   to the Library.

Why a third-party library instead of Android's built-in `PdfRenderer`: the
built-in renderer only gained password support in very recent Android versions
(API 35+), so it wouldn't work on most phones today. `android-pdf-viewer`
(Pdfium-based) supports it uniformly back to API 21.

**Note:** this environment couldn't reach JitPack/Maven Central to verify the
exact dependency version or the password-exception class name against the
library's current release (no internet access here), so double-check
`com.github.mhiew:android-pdf-viewer`'s latest version on JitPack when you
first sync, and skim its README if the password flow doesn't trigger — the
exception class it throws for password errors is checked defensively by name
match rather than a hard import, so a class rename in a future release is
unlikely to break it, but worth a quick look.



## Opening the project

1. Unzip.
2. Open the folder in **Android Studio** (it will fetch the Gradle wrapper and
   dependencies automatically — this environment couldn't reach
   `services.gradle.org`/Google's Maven to pre-build it, so the first sync needs
   an internet connection).
3. Run on an emulator or device with **minSdk 24 (Android 7.0)+**.

## Using the real Inter font

The type scale is exact, but font files aren't bundled (to keep the zip small).
To match `design.md` exactly:
1. Download the Inter `.ttf` files (Regular/Medium/SemiBold/Bold) from Google Fonts.
2. Put them in `app/src/main/res/font/`.
3. Replace `AppFontFamily` in `ui/theme/Type.kt` with a proper `FontFamily(...)`
   referencing those resources.

## Next steps (not included, kept simple per request)

- Persist the library (Room) instead of `SampleData` — currently opened real
  PDFs aren't added back into the Library grid, they just open once from the picker.
- Implement the annotation tools (currently the toolbar is visual/selectable only).
- Add dark-mode screenshots/testing — the scheme is defined but derived, not
  pixel-specified in `design.md`.

## Project structure

```
app/src/main/java/com/productivity/pdf/
├── MainActivity.kt
├── model/PdfDocument.kt
├── data/SampleData.kt
├── navigation/NavGraph.kt
└── ui/
    ├── theme/       Color.kt, Type.kt, Shape.kt, Theme.kt
    ├── components/  PdfCard.kt, TranslucentTopBar.kt, AnnotationToolbar.kt,
    │                PasswordPromptDialog.kt
    └── screens/     LibraryScreen.kt, DocumentScreen.kt, PdfViewerScreen.kt
```
