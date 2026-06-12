# Volley Scoreboard

An Android scoreboard application for volleyball matches.

## Features

- Displays team names (editable) with a configurable background color per team:
  <img width="1280" height="534" alt="match scores" src="https://github.com/user-attachments/assets/79585171-0e4b-4725-964e-373ded5bfd11" />
- Increase or decrease each team score by dragging up and down
- Option to set volume buttons as shortcuts: volume up = +1 local, volume down = +1 visitor
- Configurable number of sets: best of 1, 3, or 5
- Stats! Shows evolution of set points:
  <img width="1280" height="540" alt="match stats" src="https://github.com/user-attachments/assets/f39a2bfe-d7fa-44a5-a678-127f9b2168b8" />

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

- Android 8.0+ (API 26)
- Gradle 8.13.2
- JDK 17

## Getting started

Clone the repository and open it in Android Studio, or use the available `make` commands:

| Command | Description |
|---|---|
| `make build` | Compile debug APK → `app/build/outputs/apk/debug/app-debug.apk` |
| `make clean` | Delete all build artifacts |
| `make test` | Run unit tests (none yet — the human didn't ask for them to the robot) |
| `make lint` | Run Android lint; report saved to `app/build/reports/lint-results-debug.html` |
| `make deps-check` | List declared dependencies with their resolved versions |

## Roadmap

- ✅ Add Makefile for operations
- Distribute app in Play Store and hope to see somebody using it in a real match
- Add actual tests (the robot is learning)
- Dockerize app
