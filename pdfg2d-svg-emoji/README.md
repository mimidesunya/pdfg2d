# PDFG2D SVG Emoji Support

This module provides support for SVG images and Emoji rendering in PDFG2D.

## Emoji Setup

Due to the large size and number of emoji assets, the emoji SVG files are not included in the source repository. They must be downloaded and set up manually before the module can be fully used or built with emoji support.

### One-time Setup

To download the emoji assets and generate the necessary index file, run the following Gradle task:

```bash
gradlew :pdfg2d-svg-emoji:setupEmoji
```

This task will:
1.  Download the `noto-emoji` repository archive from GitHub.
2.  Extract the SVG files to `src/main/resources/net/zamasoft/pdfg2d/font/emoji`.
3.  Generate an `INDEX` file in the same directory.

**Note:** This process involves downloading a large file and extracting thousands of SVGs, so it may take some time.

### Build Behavior

*   **Standard Build:** Running `gradlew build` or `gradlew clean build` will **NOT** attempt to download or update the emoji assets. It assumes the assets are already present in `src/main/resources`.
*   **Persistence:** The downloaded assets are stored in the `src` directory (which is git-ignored but persists locally). Running `gradlew clean` will **NOT** delete these downloaded files.
*   **Re-running Setup:** If you need to update or repair the emoji assets, simply run the `setupEmoji` task again. The tool acts intelligently and will skip downloading if the files already exist, only regenerating the index. To force a re-download, delete the contents of the `emoji` directory or set the `FORCE_DOWNLOAD=true` environment variable.

## Using in Projects

Add this module as a dependency to your project to enable SVG and Emoji features.

```gradle
implementation project(':pdfg2d-svg-emoji')
```
