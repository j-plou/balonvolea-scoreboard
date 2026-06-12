APK_DEBUG := app/build/outputs/apk/debug/app-debug.apk

.PHONY: help build clean test lint deps-check

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

build: ## Compile debug APK
	./gradlew :app:assembleDebug
	@echo "APK → $(APK_DEBUG)"

clean: ## Delete all build artifacts
	./gradlew clean

test: ## Run unit tests
	./gradlew :app:test

lint: ## Run Android lint checks
	./gradlew :app:lint

deps-check: ## List declared dependencies and their resolved versions
	./gradlew :app:dependencies
