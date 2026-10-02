# Word Puzzle

A polished Wordscapes-style Android word puzzle built for the Android Game Developer Assessment.

Swipe letters on a circular wheel to form words, fill a crossword grid, and collect bonus extras. Includes 12 playable levels, pause overlay, and progress that survives rotation and process death.

## Tech stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **MVVM** with immutable `UiState` flows
- **Navigation Compose** for Home → Level Select → Gameplay
- **DataStore** for completed-level progress
- **Kotlin Serialization** loading levels from `assets/levels.json`
- **SavedStateHandle** for in-level found words / pause across configuration changes

## Architecture

```
ui/          Compose screens + ViewModels (observe StateFlow)
domain/      Word validation against grid + bonus dictionaries
data/        Level models/repository, progress repository
navigation/  Single-activity NavHost with launchSingleTop hygiene
```

Pause is an **overlay on Gameplay**, not a separate back-stack destination. System Back dismisses pause first; otherwise it opens pause (or leaves after clear). Navigating to the next level pops back to Level Select then pushes the new gameplay route to avoid duplicate game screens.

### Why MVVM

Gameplay has rich transient UI state (selection path, feedback, revealed cells). A single `GameViewModel` owns that state, exposes one `StateFlow<GameUiState>`, and keeps domain rules out of Composables.

## Custom swipe (letter wheel)

`LetterWheel` is a Compose `Canvas` with continuous pointer tracking (`awaitEachGesture` / `awaitFirstDown`):

1. Letter centers are laid out on a circle.
2. While the finger moves, the nearest letter within a hit radius is appended (unique path; backtracking allowed by revisiting an earlier letter).
3. A polyline connects selected centers; a live segment follows the fingertip.
4. Selected letters scale up with a gold ring.
5. On release, `GameViewModel` validates the word (≥2 letters). Feedback: grid fill + celebrate, bonus chip, already-found, or invalid shake. Single-letter releases are ignored.

Rapid swipes are handled by consuming pointer moves and never double-adding the current last index.

## Level data

Levels live in [`app/src/main/assets/levels.json`](app/src/main/assets/levels.json).

Schema:

```json
{
  "id": 1,
  "letters": ["C", "A", "T"],
  "gridWords": ["CAT", "AT"],
  "extraWords": ["ACT", "TA"],
  "layout": {
    "rows": 2,
    "cols": 3,
    "placements": [
      { "word": "CAT", "row": 0, "col": 0, "direction": "ACROSS" },
      { "word": "AT", "row": 0, "col": 1, "direction": "DOWN" }
    ]
  }
}
```

- `letters`: 3–5 characters on the wheel (duplicates allowed when needed, e.g. BEE).
- `gridWords`: must match `placements` and are required to clear the level.
- `extraWords`: valid bonus words that do not fill the grid.
- `placements`: crossword positions; overlapping cells share the same letter.

`LevelRepository` parses JSON once and caches domain `Level` objects (including a derived `cellLetters` map for rendering).

## Run / build APK

1. Open the project in **Android Studio** (Ladybug or newer recommended).
2. Let Gradle sync; install the Android SDK if prompted.
3. If sync fails on a very new JBR (e.g. Java 25), point Gradle at **JDK 17 or 21** in *Settings → Build → Gradle → Gradle JDK*.
4. Run on an emulator or device, or assemble a debug APK:

```bash
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Package

`com.wordpuzzle.game` · minSdk 26 · targetSdk 35
