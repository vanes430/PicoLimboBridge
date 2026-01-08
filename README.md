# PicoLimboBridge

PicoLimboBridge is a management plugin for [PicoLimbo](https://github.com/Quozul/PicoLimbo) designed for **Spigot/Paper**, **Folia**, and **Velocity**.

It allows you to automatically download, install, and manage a local PicoLimbo instance directly from your main server or proxy. This is useful for setting up a fallback/limbo server without managing a separate external process manually.

## Features

*   **Automatic Installation**: Automatically detects your OS (Windows, Linux, macOS) and architecture (x86_64, aarch64), then downloads the latest PicoLimbo binary from GitHub Releases.
*   **Process Management**: Start and stop the PicoLimbo process via commands.
*   **Console Integration**: Forwards PicoLimbo's logs to your server console.
*   **Configuration Management**: Easily override the binding port via command arguments, which updates the local `server.toml`.
*   **Auto-Update**: Built-in command to reinstall or update to the latest version.
*   **Multi-Platform Support**: Fully compatible with Spigot, Paper, Folia, and Velocity.

## Installation

1.  Download the appropriate artifact for your platform:
    *   **Spigot/Paper/Folia**: `picolimbobridge-spigot-1.0.0-SNAPSHOT.jar`
    *   **Velocity**: `picolimbobridge-velocity-1.0.0-SNAPSHOT.jar`
2.  Place the jar file into your server's `plugins` folder.
3.  Start your server.

## Usage

All commands require the `picolimbobridge.admin` permission.

### Commands

| Command | Description |
| :--- | :--- |
| `/picolimbo start [port]` | Starts the PicoLimbo process. If provided, `[port]` updates the `server.toml` configuration. If the binary is missing, it will be downloaded automatically. |
| `/picolimbo stop` | Gracefully stops the PicoLimbo process. |
| `/picolimbo reinstall [force]` | Stops the process and re-downloads the latest binary. Use `force` to delete existing configurations (`server.toml`) as well. |

### Example

Start PicoLimbo on port 25566:
```
/picolimbo start 25566
```

## Building from Source

To build this project, you need JDK 17+ and Maven.

```bash
git clone https://github.com/vanes430/PicoLimboBridge.git
cd PicoLimboBridge
mvn clean install
```

The compiled jars will be located in:
*   `spigot/target/picolimbobridge-spigot-1.0.0-SNAPSHOT.jar`
*   `velocity/target/picolimbobridge-velocity-1.0.0-SNAPSHOT.jar`

## Credits

*   **PicoLimbo** by [Quozul](https://github.com/Quozul) - The lightweight Limbo server implementation.
