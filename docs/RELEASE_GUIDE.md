# Release Guide

This guide describes the automated release process for the `aws-lambda-apim-sdk` project, as implemented in the GitHub Actions workflow (`.github/workflows/build-multi-version.yml`).

## Overview

Releases are fully automated and triggered by code changes pushed to the `master`, `main`, or `develop` branches. The workflow builds the SDK for all supported Axway API Gateway versions, applies semantic versioning, creates a tag if needed, and publishes a GitHub Release with all artifacts.

## Automated Release Flow

### Trigger
- **Push** to `master`, `main`, or `develop` branches (affecting code, build.gradle, gradle config, or axway-versions.json)
- **Manual dispatch** (workflow_dispatch) with optional version input
- **Pull requests** (for build/test only, no release)

### Main Steps
1. **Checkout code** (full history)
2. **Set up Docker Buildx**
3. **Parse Axway versions** from `axway-versions.json` (builds for all or a specific version)
4. **Run semantic versioning** (`scripts/version-bump.sh`)
   - Determines if a new version is needed
   - Updates `build.gradle` and `.version_info` as needed
5. **Build for each Axway version**
   - Uses Docker images for each version
   - Runs Gradle build and dependency copy
   - Produces a JAR for each version
6. **Upload JARs as workflow artifacts**
7. **Create Release (on master only, not PRs)**
   - Determines the final version and tag (auto-increments if needed)
   - Updates `build.gradle` and pushes if version changed
   - Creates a tag (e.g., `v1.2.3`) and pushes it
   - Packages all build outputs and resources into ZIPs (one per Axway version)
   - Each ZIP includes:
     - Main JAR
     - Gradle wrapper and build files
     - Installation scripts (Linux)
     - Policy Studio resources (fed/yaml)
     - External dependencies
     - README with installation instructions
   - Publishes a GitHub Release with all ZIPs attached

## How to Trigger a Release

- **Just push your code to `master` or `main`** (or merge a PR). The workflow will:
  - Analyze if a release is needed (ignores doc-only or config-only changes)
  - Bump the version if required
  - Build and publish the release automatically
- **Manual trigger:** Go to GitHub Actions > Build Multi-Version JARs > Run workflow, and optionally specify an Axway version.

## What Happens in a Release

- **Semantic versioning** is applied automatically based on commit messages and file changes.
- **A new tag** is created if a new version is detected (auto-incremented if the tag exists).
- **A GitHub Release** is published with ZIPs for each supported Axway version.
- **Each ZIP** contains everything needed for installation on Linux or Windows (via Gradle tasks).

## Release Artifacts

- **ZIP files**: One per Axway version, named `aws-lambda-apim-sdk-<version>-<axway-version>-<date>.zip`
- **Contents:**
  - Main JAR (`aws-lambda-apim-sdk-*.jar`)
  - `dependencies/` (external Gradle dependencies)
  - `src/main/resources/fed/` and `src/main/resources/yaml/` (Policy Studio resources)
  - `install-linux.sh` (Linux install script)
  - `gradlew`, `gradlew.bat`, `gradle/`, `build.gradle` (for Windows install via Gradle)
  - README with installation instructions

## Installation (from Release ZIP)

### Linux
1. Unzip the file
2. Set the `AXWAY_HOME` variable (e.g., `/opt/Axway/API_Gateway/7.7.0.20240830`)
3. Run: `./install-linux.sh`

### Windows (Policy Studio project)
1. Unzip the file
2. Open a terminal in the ZIP directory
3. Run: `./gradlew "-Dproject.path=C:\\path\\to\\your\\project" installWindowsToProject`

## Notes
- **No manual tag or release creation is needed.** The workflow handles versioning, tagging, and publishing.
- **If a tag already exists**, the workflow auto-increments the PATCH version and retries.
- **All build and release logs** are available in the GitHub Actions tab.
- **Only code, build, or resource changes** trigger a release. Documentation-only changes are ignored for versioning.

## Useful Links
- **Workflow:** `.github/workflows/build-multi-version.yml`
- **Versioning:** `SEMANTIC_VERSIONING.md`
- **Documentation:** `README.md` 