# PicoLimboBridge 🌉

![License](https://img.shields.io/badge/License-GPLv3-blue.svg) ![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Platform](https://img.shields.io/badge/Platform-Spigot%20|%20Paper%20|%20Folia%20|%20Velocity-lightgrey)

**PicoLimboBridge** is the ultimate bridge between your Minecraft server and [PicoLimbo](https://github.com/Quozul/PicoLimbo)—the ultra-lightweight Limbo server.

Designed for **Spigot/Paper**, **Folia**, and **Velocity**, this plugin eliminates the hassle of setting up a separate fallback server. It seamlessly integrates a local PicoLimbo instance directly into your existing infrastructure, saving you time, money, and system resources.

---

### 🚀 Why PicoLimboBridge?

*   **💸 Cost-Effective**: Stop paying for an extra VPS or server slot just for a fallback/AFK server.
*   **⚡ Ultra Lightweight**: Runs a tiny, optimized process alongside your main server.
*   **🛠️ Zero Friction**: No manual binary downloads, no complex shell scripts. It just works.

---

## ✨ Features

*   **⬇️ Automatic Installation**: Smartly detects your OS (Windows, Linux, macOS) and architecture (x86_64, aarch64) to fetch the perfect PicoLimbo binary automatically.
*   **🎮 Process Management**: Effortlessly start, stop, and restart the PicoLimbo process via simple in-game or console commands.
*   **📝 Console Integration**: View PicoLimbo's logs directly in your main server's console—no need to switch windows.
*   **⚙️ Dynamic Configuration**: Change the binding port on the fly using command arguments; `server.toml` is updated automatically.
*   **🔒 Integrity Protection**: Automatically verifies the SHA-256 hash of the PicoLimbo binary against a trusted upstream source. If a mismatch or corruption is detected, it self-heals by re-downloading the correct version.
*   **🔄 Auto-Update**: built-in tools to reinstall or upgrade to the latest binary with a single command.
*   **🌐 Multi-Platform**: One bridge to rule them all—fully compatible with Spigot, Paper, Folia, and Velocity.

## 📦 Installation

1.  **Download** the appropriate artifact for your platform:
    *   **Spigot/Paper/Folia**: `picolimbobridge-spigot-1.0.0-SNAPSHOT.jar`
    *   **Velocity**: `picolimbobridge-velocity-1.0.0-SNAPSHOT.jar`
2.  **Drop** the jar file into your server's `plugins` folder.
3.  **Start** your server. That's it!

## 🖥️ Usage

All commands require the `picolimbobridge.admin` permission.

### Commands

| Command | Description |
| :--- | :--- |
| `/picolimbo start [port]` | 🟢 Starts the PicoLimbo process. If provided, `[port]` updates the `server.toml` configuration. If the binary is missing, it will be downloaded automatically. |
| `/picolimbo stop` | 🔴 Gracefully stops the PicoLimbo process. |
| `/picolimbo reinstall [force]` | 🔄 Stops the process and re-downloads the latest binary. Use `force` to delete existing configurations (`server.toml`) as well. |

### Example

Start PicoLimbo on port `25566`:
```bash
/picolimbo start 25566
```

## 🛠️ Building from Source

To build this project, you need **JDK 17+** and **Maven**.

```bash
git clone https://github.com/vanes430/PicoLimboBridge.git
cd PicoLimboBridge
mvn clean install
```

The compiled jars will be located in:
*   `spigot/target/picolimbobridge-spigot-1.0.0-SNAPSHOT.jar`
*   `velocity/target/picolimbobridge-velocity-1.0.0-SNAPSHOT.jar`

## 🏆 Credits

*   **PicoLimbo** by [Quozul](https://github.com/Quozul) - The lightweight Limbo server implementation.
*   **PicoLimbo Wiki**: [Introduction & Documentation](https://picolimbo.quozul.dev/about/introduction.html)