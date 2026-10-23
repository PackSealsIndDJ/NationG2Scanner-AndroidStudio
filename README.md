# Nation G2 Scanner — Android 13

Android Studio project based on the supplied Nation G2 UHF reader demo and SDK. Gradle is configured for Android 13 (compile SDK 33, target SDK 33).

## Implemented features

- **UHF scan mode:** The top bar selects **Single** or **Bulk**. The mode applies to the handheld trigger (vendor demo key code `619`) and the on-screen Inventory button. Single mode uses the vendor SDK's `InventoryMode_Single` and issues a stop after the one-shot window; Bulk uses continuous inventory until stopped.
- **EPC and TID:** EPC inventory is available by default. Enable TID in the Reader Settings tab to read and show TID alongside EPC. The vendor API inventories EPC and reads TID memory as an additional bank.
- **Tag operations:** Read, write, lock, kill, and RF/frequency configuration use the vendor demo flows and `reader.jar` SDK.
- **Scan beep:** A short confirmation tone plays for each valid tag callback during inventory, plus successful barcode and NFC reads.
- **1D and 2D barcode:** Open **Barcode · NFC · GPS · About** and choose 1D or 2D. The camera scanner uses the bundled ZXing library and requests camera permission at runtime.
- **NFC:** Reader mode displays the tag ID and technologies when an NFC tag is held near the device. Requires NFC hardware, if present on the handheld.
- **GPS:** Requests Android location permission and reads a recent or fresh GPS/network location fix. Requires location hardware/services and permission.
- **Hardware info:** Shows Android build fields, manufacturer/model, ABI, NFC/GPS/camera availability, bundled UHF SDK, and vendor trigger key mapping.
- **Android 13 permissions:** Camera and location are runtime requested. NFC, camera, and location hardware features are marked optional so app installation does not require them.

## Open and run

Open the folder containing `settings.gradle` in Android Studio, sync dependencies, then install on the Nation G2 handheld. The built-in UHF reader requires matching Nation G2 firmware and vendor access to `/dev/ttyS3` and the UHF power-control interface. Generic Android devices will not provide the integrated UHF module.

Use **Reader Settings** to enable TID before choosing a scan. Use only test tags when trying Kill; a successful kill permanently disables the tag.

## Device and validation notes

This project retains the vendor's firmware-upgrade and export flows. Confirm those flows on Android 13 because the vendor sample predates scoped storage. The Single inventory window and scanner trigger mapping should also be checked on the exact G2 model/firmware. No Android build or on-device run was performed in this update, so compatibility with that handheld has not been physically verified.
