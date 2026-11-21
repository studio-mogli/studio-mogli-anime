# Studio Mogli Anime (Android)

A sleek, Netflix-style Android anime streaming app built with **Kotlin, Jetpack Compose, and Media3 (ExoPlayer)**.

## Features
- **English-Only Interface** with anime audio/subtitle language selection (Japanese, English, Hindi).
- **Multi-Provider Streaming Architecture**: Fetches metadata from **AniList GraphQL** (`https://graphql.anilist.co`) and resolves episodes/streams across **Aniwatch-API (HiAnime)**, **Consumet**, and **Anify** for maximum catalog availability.
- **Credits Page**: Links to `https://studiomogli.com` and includes the **Refer & Earn (10%)** button (`https://studiomogli.com/refer-and-earn/`).
- **Trust & Security Page**: Explains the 100% malware-free guarantee and technical zero-data-collection architecture (only `INTERNET` permission requested).
- **Smart Overlay Controls**: First-time launch welcome popup + top-right **SM** button that automatically hides during fullscreen anime playback.
- **GitHub Auto-Update System**: Automatically checks GitHub Releases; the update button stays completely hidden until a newer release tag (e.g., `v1.1.0`) is published.

## How to Auto-Package the APK on GitHub
1. Create a new GitHub repository (e.g., `StudioMogliAnime`).
2. Upload all files from this ZIP to the repository root.
3. Open `app/build.gradle.kts` and replace `YOUR_GITHUB_USERNAME` with your actual GitHub username (so the in-app update button checks your repo).
4. Push to `main`. Go to the **Actions** tab on GitHub — the **Build Studio Mogli Anime APK** workflow will automatically compile and attach `StudioMogliAnime-APK` for download.
5. To release an update that triggers the in-app **Update** button for users:
   - Bump `versionName` in `app/build.gradle.kts` (e.g., `"1.1.0"`).
   - Create a GitHub Release tagged `v1.1.0` — GitHub Actions will automatically attach `StudioMogliAnime.apk` to the release!
