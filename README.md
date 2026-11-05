# Nation G2 Scanner — Android 13

Android Studio project based on the supplied Nation G2 UHF reader demo and SDK. Gradle is configured for Android 13 (compile SDK 33, target SDK 33).

## Implemented features

- **UHF scan mode:** The top bar selects **Single** or **Bulk**. In Single mode, each hardware-trigger press starts an EPC inventory, reads EPC plus TID only, accepts the first tag, and immediately sends a stop command. Results accumulate across trigger presses; repeat events while holding the trigger and later callbacks within a scan are ignored. Bulk mode continues inventory until stopped. EPC + TID single scans require an EPC-capable tag protocol; 6B tags do not provide an EPC.
- **EPC and TID:** EPC inventory is available by default. Enable TID in the Reader Settings tab to read and show TID alongside EPC. The vendor API inventories EPC and reads TID memory as an additional bank.
- **Tag operations:** Read, write, lock, kill, and RF/frequency configuration use the vendor demo flows and `reader.jar` SDK.
- **Scan beep:** A short confirmation tone plays for each valid tag callback during inventory, plus successful barcode and NFC reads.
- **1D and 2D barcode:** Open **Scanner tools** and choose either camera scan option. The camera reader recognizes common 1D barcodes, QR codes, and Data Matrix codes, requests camera permission when needed, and displays the decoded value.
- **NFC:** Reader mode displays the tag ID and technologies when an NFC tag is held near the device. Requires NFC hardware, if present on the handheld.
- **GPS:** Requests Android location permission and reads a recent or fresh GPS/network location fix. Requires location hardware/services and permission.
- **Hardware info:** Shows Android build fields, manufacturer/model, ABI, NFC/GPS/camera availability, bundled UHF SDK, and vendor trigger key mapping.
- **Android 13 permissions:** Camera and location are runtime requested. NFC, camera, and location hardware features are marked optional so app installation does not require them.

## Open and run

Open the folder containing `settings.gradle` in Android Studio, sync dependencies, then install on the Nation G2 handheld. The built-in UHF reader requires matching Nation G2 firmware and vendor access to `/dev/ttyS3` and the UHF power-control interface. Generic Android devices will not provide the integrated UHF module.

Use **Reader Settings** to enable TID before choosing a scan. Use only test tags when trying Kill; a successful kill permanently disables the tag.

## Device and validation notes

This project retains the vendor's firmware-upgrade and export flows. Confirm those flows on Android 13 because the vendor sample predates scoped storage. The Single inventory window and scanner trigger mapping should also be checked on the exact G2 model/firmware. The scanner SDK jar is compile-only: the runtime scanner service must be provided by the Nation G2 firmware. Physical hardware compatibility should be verified on the target handheld.
