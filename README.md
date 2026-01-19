# Sentinel

## Showcase video
YouTube showcase + explanation: (link coming soon)

The first (that I know of) public Hytale anticheat plugin focused on timer-based movement/interaction checks with staff alerts and recent flag logs.

## Current Checks
- Timer: Detects abnormally fast movement and interaction packets.
- More soon!

## Features
- Staff Alerts in Chat
- Optional Discord Webhook Alerts
- Optional automatic punishments (disconnect/ban) at high flag counts

## Configuration
The config file is created in the plugin data directory on first run (located in `mods/Al3x_HytaleAC/config.json`).

Default config:
```json
{
  "timer": {
    "enabled": true,
    "minMsInteractionPacket": 10,
    "minNanoMovementPacket": 15000000.0,
    "maxFlags": 75
  },
  "alerts": {
    "resetIntervalSeconds": 60,
    "notifyReset": true,
    "debugMode": false,
    "discordWebhookUrl": ""
  },
  "punishments": {
    "reason": "[Anticheat] Cheating",
    "shouldBan": false,
    "maxFlags": 1000
  }
}
```

Config options:

| Path | Type | Default                  | Description                                                                |
| --- | --- |--------------------------|----------------------------------------------------------------------------|
| `timer.enabled` | boolean | `true`                   | Enables or disables the timer check entirely.                              |
| `timer.minMsInteractionPacket` | int | `10`                     | Minimum milliseconds between interaction packets before flagging.          |
| `timer.minNanoMovementPacket` | number | `15000000.0`             | Minimum average nanoseconds between movement packets before flagging.      |
| `timer.maxFlags` | int | `75`                     | Flags required before sending an alert.                                    |
| `alerts.resetIntervalSeconds` | int | `60`                     | Interval in seconds to clear player flags.                                 |
| `alerts.notifyReset` | boolean | `true`                   | If `true`, notifies staff members when flags are reset.                    |
| `alerts.debugMode` | boolean | `false`                  | If `true`, prints debug output to the server console for different checks. |
| `alerts.discordWebhookUrl` | string | `""`                     | Discord webhook URL for alert embeds; leave empty to disable.              |
| `punishments.reason` | string | `"[Anticheat] Cheating"` | Reason used for disconnects/bans and punishment webhooks.                  |
| `punishments.shouldBan` | boolean | `false`                  | If `true`, bans instead of disconnecting when punishments trigger.         |
| `punishments.maxFlags` | int | `1000`                   | Total flags required before punishments trigger.                            |

## Webhook alerts
If `alerts.discordWebhookUrl` is set, staff alerts and punishment actions also post to Discord. Alerts are rate-limited to once per second.

## Punishments
When a player reaches `punishments.maxFlags` total flags, the plugin will disconnect them by default and send a punishment webhook. If `punishments.shouldBan` is `true`, it will issue an infinite ban instead.

## Commands

| Command             | Description                           | Permission | Notes                                                   |
|---------------------|---------------------------------------| --- |---------------------------------------------------------|
| `/anticheat reload` | Reloads plugin configuration options. | `anticheat.command` | Note that a server restart is better.                   |
| `/anticheat help`   | Shows available anticheat commands.   | `anticheat.command` | N/A                                                     |
| `/alerts`           | Toggles receiving anticheat alerts.   | `hytaleac.alerts` | Players with this permission are auto-enrolled on join. |
| `/logs <player>`    | Shows recent flags for a player.      | `hytaleac.logs` | Logs are cleared on the reset interval.                 |

## Permissions
- `anticheat.command`: Use `/anticheat help` and `/anticheat reload`.
- `hytaleac.alerts`: Use `/alerts` and receive staff alerts.
- `hytaleac.logs`: Use `/logs <player>`.

## Installation
1. Grab the .jar from the latest release.
2. Copy the jar into the Hytale server's mods directory.
3. Start the server to generate `config.json` in the plugin data directory.

## Troubleshooting
- Too many/false flags: make checks less sensitive by lowering `timer.minMsInteractionPacket` and/or `timer.minNanoMovementPacket`, or increase `timer.maxFlags`.
- No staff alerts: ensure staff have `hytaleac.alerts`, and that they toggled alerts with `/alerts`.
- Logs always empty: logs reset on `alerts.resetIntervalSeconds`, this is to prevent false flags from cluttering logs.
- No Discord alerts: verify `alerts.discordWebhookUrl` is a valid webhook url.

## Performance Notes
Hytale is still new and the server API is evolving. While this plugin has been tested on lower-end hardware, performance can vary based on server load and player count. And may break if a sudden update changes how packets work. Always monitor your server's performance after adding new plugins.
