# Volley Scoreboard

An Android scoreboard application for volleyball matches.

## Features

- Displays team names (editable) with a configurable background color per team
- Increase or decrease each team score by dragging up and down
- Option to set volume buttons as shortcuts: volume up = +1 local, volume down = +1 visitor
- Configurable number of sets: best of 1, 3, or 5
- Scores and sets persist across rotation (forced to landscape) and app restarts

### Scoring rules

- Standard sets to 25 points (win by 2)
- Final set (3rd or 5th) to 15 points (win by 2)
- Sets update automatically when a team wins and reset current-set points

### Visual feedback

- Score counters styled as a flip clock (split digits, monospace font, team color background)
- Scoring streak of 3+ consecutive points → streak counter shown with shake animation (intensity scales with streak length)
- Match point → score turns red with a heartbeat animation
- End of match → celebration and confetti!! :tada:

## Requirements

- Android 7.0+ (API 24)
- Gradle 8.13.2
- JDK 17

## Getting started

Clone the repository and open it in Android Studio, or build from the command line:

```bash
./gradlew :app:assembleDebug
```

File to install in device:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

## Roadmap

- Distribute app in Play Store and hope to see somebody using it in a real match :blush:
