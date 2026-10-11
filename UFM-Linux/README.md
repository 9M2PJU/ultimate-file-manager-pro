# Ultimate File Manager Pro Companion (Linux)

> **Official Linux desktop companion for [Ultimate File Manager Pro (UFM)](https://github.com/Kilowatch/ultimate-file-manager-pro)** — a high-performance dual-pane file manager, remote APK/XAPK sideloading tool, and device management utility designed Wayland-first for modern Linux desktops and tiling window managers.

![Platform](https://img.shields.io/badge/platform-Linux%20(Wayland%20%2F%20X11)-orange?logo=linux)
![Packaging](https://img.shields.io/badge/package-AppImage%20%7C%20Deb-blue)
![Tauri](https://img.shields.io/badge/built%20with-Tauri%202-24C8DB?logo=tauri)
![Rust](https://img.shields.io/badge/backend-Rust-orange?logo=rust)
![React](https://img.shields.io/badge/frontend-React%2019-61DAFB?logo=react)
![License](https://img.shields.io/badge/license-GPL--3.0-blue)

---

## Technical Overview

The Ultimate File Manager Pro Linux Companion allows power users to establish a secure, lightning-fast connection to their Android phone or Android TV running UFM Pro over local Wi-Fi.

```
+-------------------------------------------------------------+
|             ULTIMATE FILE MANAGER PRO LINUX COMPANION       |
+------------------------------+------------------------------+
|     LOCAL POSIX FILESYSTEM   |       REMOTE FILESYSTEM      |
|    Browse /, $HOME, /media   |     Browse /sdcard, USB OTG    |
|  --------------------------  |  --------------------------  |
|      <- Download / Upload    |      Upload / Download ->    |
+------------------------------+------------------------------+
|                     REMOTE SIDELOAD ZONE                    |
|           Drag & Drop APK / XAPK -> Remote Install          |
+-------------------------------------------------------------+
```

---

## Wayland-First Features

### 🪟 Tiling Compositor & Window Rule Friendly
- **Stable `app_id`**: Configured as `za.kilowatch.ufm-companion` for seamless window rule targeting:
  - **Hyprland**: `windowrule = float, class:^(za\.kilowatch\.ufm-companion)$`
  - **Sway**: `for_window [app_id="za.kilowatch.ufm-companion"] floating enable`
  - **Niri**: `window-rule { match app-id="za.kilowatch.ufm-companion"; default-column-width {}; }`
- **Native Window Decorations**: Integrates cleanly with server-side decorations (SSD) without redundant custom titlebar chrome.

### 📁 Linux Desktop Portal Integration
- Native file picking and external file openers integrate with `xdg-desktop-portal` via DBus.
- Safe path canonicalization and directory navigation across root `/`, `$HOME`, `/media`, and `/mnt`.

### ⚡ Native Rust Backend Engine
- High-performance native Rust engine delivering multi-NIC UDP broadcast LAN discovery (`UFM_DISCOVER:`), Trust-On-First-Use (TOFU) SHA-256 certificate fingerprint pinning, and chunked streaming file transfers.

---

## Prerequisites

| Requirement | Supported Version | Details |
|---|---|---|
| **Rust** | 1.78+ (`rustup`) | Compiles Tauri native Rust backend modules |
| **Node.js** | 20 LTS+ | Runs Vite dev server and bundles React frontend |
| **System Libraries** | WebKitGTK 4.1 | `libwebkit2gtk-4.1-dev`, `libayatana-appindicator3-dev`, `librsvg2-dev` |

### Installing Linux Build Dependencies (Debian / Ubuntu / WSL)
```bash
sudo apt update
sudo apt install -y \
  libwebkit2gtk-4.1-dev \
  build-essential \
  curl \
  wget \
  file \
  libxdo-dev \
  libssl-dev \
  libayatana-appindicator3-dev \
  librsvg2-dev
```

---

## Building Locally

### 1. Install Node Dependencies
```bash
cd UFM-Linux
npm install
```

### 2. Run in Development Mode
```bash
npm run tauri dev
```

### 3. Build Production AppImage & Deb
```bash
npm run tauri build -- --bundles appimage,deb
```
The resulting standalone `.AppImage` will be located in:
`src-tauri/target/release/bundle/appimage/`

---

## Testing in WSL

If you are developing inside Windows Subsystem for Linux (WSL) without a physical Linux machine:

1. **Unit & Protocol Tests (CLI):**
   ```bash
   cargo test --manifest-path src-tauri/Cargo.toml
   ```

2. **AppImage Extraction Test (FUSE Bypass):**
   ```bash
   ./src-tauri/target/release/bundle/appimage/*.AppImage --appimage-extract
   ./squashfs-root/AppRun --version
   ```

3. **Wayland Display Execution (WSLg):**
   ```bash
   GDK_BACKEND=wayland ./squashfs-root/AppRun
   ```

> **Important:** always test a **production** build (`npm run tauri build`). A Tauri *dev*
> build embeds no frontend assets and loads the Vite dev server at
> `http://localhost:1420` instead — running such an `AppRun` without `npm run dev`
> active shows a blank window ("Could not connect to localhost: Connection refused").
> Run the verification step after every release build to confirm the frontend bundle
> is embedded:
> ```bash
> npm run tauri build -- --bundles appimage,deb
> npm run verify:appimage
> ```

---

## License

Ultimate File Manager Pro Linux Companion is licensed under the **GNU General Public License v3.0**. See the root [LICENSE](../LICENSE) file for complete details.
