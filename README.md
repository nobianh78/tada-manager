# \u26a1 TADa Manager

> Want to watch ads? Us neither.

TADa Manager is an Android app that patches YouTube, YouTube Music, and Reddit - stripping ads and giving you back control over your experience. No root required.

> **Fork attribution:** TADa Manager is an independent fork of [Morphe](https://github.com/MorpheApp/morphe-manager) by MorpheApp, which itself is built on the foundations of [ReVanced Manager](https://github.com/ReVanced/revanced-manager) and [URV](https://github.com/Jman-Github/Universal-ReVanced-Manager). Licensed under [GPLv3](LICENSE); see [NOTICE](NOTICE) for the additional Section 7 conditions, including the trademark restrictions that apply to all derivatives.

## \u2728 Why TADa Manager?

Modern apps are full of ads and dark patterns designed to grab your attention. TADa Manager patches them at the bytecode level - modifying the app directly on your device, without any of your data leaving it. The result is a cleaner version of the app you already know, exactly the way you want it.

## \U0001f4f2 Download

Grab the latest APK from the **[Releases page](https://github.com/nobianh78/tada-manager/releases/latest)**.

No account needed. *(Replace `nobianh78` with the actual repo owner after forking.)*

## \U0001f680 How it works

1. **Install TADa Manager** from the Releases page above.
2. **Pick an app** - YouTube, YouTube Music, or Reddit are supported out of the box.
3. **Choose your mode:**
    - **Simple mode** - designed for a one-tap experience. Just tap Patch and TADa Manager handles the rest with sensible defaults. No configuration needed.
    - **Expert mode** - gives you full control. Choose exactly which of the 100+ patches to apply, configure per-patch options (colors, toggles, and more), and fine-tune everything before patching.
4. **Provide the APK** - TADa Manager guides you through obtaining the original app file via step-by-step dialogs. The patching itself happens entirely on your device.
5. **Install and enjoy** - once patching is complete, install the result like any normal APK.

Everything happens locally. TADa Manager never uploads your APKs or personal data anywhere.

## 📖 Guides

Step-by-step walkthroughs with screenshots, covering patching in both modes, installers, updates, patch sources, backups, and customization: **[→ TADa Manager guides](docs/README.md)**

## 🔧 Features

**Patching**
- Simple mode for one-tap patching with curated defaults
- Expert mode for full patch selection, per-patch configuration, and experimental version support
- Expert mode also shows an expanded patching screen with real-time logs and live RAM usage monitoring during patching
- 100+ patches for YouTube, YouTube Music, and Reddit
- Support for split APKs
- Optional "Optimize for device architecture" mode - skips split APK modules for unsupported CPU architectures, locales, and screen densities during merge, and strips native libraries for unsupported architectures from plain APKs after patching
- Sends a notification the moment patching finishes, so you don't have to keep the app open
- Optional completion sound, with a distinct tone for success and failure
- Optional auto-install right after patching completes, through Shizuku, or through the system installer where Android lets TADa Manager replace a build it installed itself
- Batch patching - select several apps and patch them in one queue, with every question asked
  up front so the run never stops to wait for you
- A re-patch banner on the home screen when a patch source releases changes for your apps, counting them and queueing them all in one tap

**Patch options** *(Simple mode: available in the Advanced tab; Expert mode: available on the patch selection screen)*
- Custom app display name, launcher icon, and header logo per app, with built-in creators that generate every density variant for you
- App theme colors (background color presets)
- Hide Shorts app shortcut and widget (YouTube)
- And more, depending on installed patch bundles

**Patch sources**
- Add any compatible patch bundle via GitHub URL or deep link
- Per-source pre-release toggle to get early patch access
- Automatic background update notifications (even when the app is closed)
- Sort your app list and patch sources however you like (name, install date, and more)

**Installer**
- Standard Android installer
- Shizuku, Shizuku+ or Sui for installs with no confirmation dialog
- Root installer with Magisk module support (mount-based, no data loss on update)
- Play Store installer variants, so Google Play recognizes itself as the install source (with a warning about the trade-off - Play Store may then offer updates that would overwrite your patched build)
- Any third-party installer apps detected on the system are also available as an option
- Prompt-on-install option to choose per session
- On rooted devices, TADa Manager asks whether you want a Root Mount install or a Standard install before patching starts, and adjusts the applied patches to match your choice

**Appearance**
- System / Light / Dark / Material You themes
- Pure Black mode for OLED screens
- Accent color selection
- Animated backgrounds - pick one you like, or let TADa Manager shuffle them for you on each launch, daily, or every three days
- App icon selection

**Home screen**
- Friendly time-of-day greeting when you open the app
- Rearrange your app list into the order that suits you
- Group your apps by patch source or your own categories
- Hide apps you never patch, and bring them back whenever you like
- Home cards for apps patched with universal patches, not just app-specific ones
- Multi-select and bulk actions for cleaning up saved APKs and patch selections, and for
  patching several apps in one queue
- Launcher shortcuts for re-patching outdated apps, checking for updates, and jumping straight
  into patching a recently patched app
- Floating scroll-to-top button when your lists get long
- A short guided tour after your first patch, so you know where everything lives

**Advanced**
- Import/export your TADa Manager settings as JSON, with a Replace or Merge choice on import
- Import/export your signing keystore
- Manage saved original APKs and patched APKs
- Manage saved patch selections per app
- GitHub Personal Access Token support for higher API rate limits
- Process runtime - run patching in a separate process for better stability, with configurable memory limit
- Bytecode processing mode - controls how bytecode is processed during patching, affecting patching speed, memory usage, and output APK size
- Built-in file picker as an alternative to the system one, with an option to show hidden files
- Optional external trigger, so automation apps can queue a batch through an intent, gated by
  a per-app confirmation
- Export debug logs for troubleshooting

## ❓ New to GitHub?

If you ended up here but aren't sure what to do next - no worries. Here's the short version:

1. Go to the **[Releases page](https://github.com/nobianh78/tada-manager/releases/latest)**.
2. Under **Assets**, tap the file ending in `.apk` to download it.
3. Open the downloaded file on your Android device and tap **Install**.
4. If Android asks you to allow installs from unknown sources, follow the prompt to enable it - this is required for any app not from the Play Store.

That's it. Once TADa Manager is installed, everything else happens inside the app - the **[guides](docs/README.md)** above walk you through the first patch.

For FAQs and troubleshooting, check the [guides](docs/README.md) above or open an issue on this repo.

## 📙 Contributing

Thank you for considering contributing to TADa Manager.
You can find the contribution guidelines [here](CONTRIBUTING.md).

## ❗ About

TADa Manager is a fork of [Morphe](https://github.com/MorpheApp/morphe-manager), which is built on the foundation of [ReVanced Manager](https://github.com/ReVanced/revanced-manager) and [URV](https://github.com/Jman-Github/Universal-ReVanced-Manager). All changes made by Morphe are documented in their Git history; changes made by TADa Manager are documented here.

## 📜 License

TADa Manager is licensed under the [GNU General Public License v3.0](LICENSE), with additional conditions under GPLv3 Section 7:

- **Name & Branding Restrictions (7c & 7e):** Derivative works must use their own distinct branding. The **"Morphe"** name, logos, and trademarks may not be used for the branding or title of derivative works (e.g., names like *"Morphe Plus"*, *"Morphe Expanded"*, or *"Morphe UserXYZ"* are strictly prohibited).

See the [LICENSE](LICENSE) file for the full GPLv3 terms and the [NOTICE](NOTICE) file for full conditions of GPLv3 Section 7.
