UNICOMMERCE PUTAWAY ASSISTANT — ANDROID STUDIO SOURCE

This ZIP contains source code, not an installable APK.

Expected sheet layout:
- A2: one shelf code
- A3 onward: barcode URLs, one per row
The first non-empty value from column A after the header is entered once, followed by the remaining values one by one.

Setup:
1. Install Android Studio and open this project folder.
2. Use JDK 17 and install Android SDK 35.
3. Sync Gradle, then Build > Build APK(s).
4. Install the APK on Android 13.
5. Enable Putaway Assistant under Settings > Accessibility.
6. Select the Excel file, set delay (default 2000 ms), open Unicommerce Putaway, focus the scan input, and start.

Important limitations:
- This is an initial prototype and has not been tested against your installed Unicommerce build.
- It uses Android Accessibility to set text in the first visible editable field and invoke the keyboard IME Enter action. Some scanner fields may reject this method or require real key events.
- Keep the Unicommerce scan field focused. Test with one barcode before processing a full task.
- It does not verify that the server accepted each scan; it stops if it cannot locate or enter text into a field. Do not treat a sent value as proof of successful scanning.
- It never taps Close Putaway.
- CSV support assumes one simple value per line in column A.
