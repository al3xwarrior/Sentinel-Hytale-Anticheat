# Sentinel

## Showcase video
YouTube showcase + explanation: (link coming soon)

A Hytale server-side anticheat plugin focused on movement/interaction checks with staff alerts, recent flag logs, and optional Discord webhook notifications and punishments.

## Current Checks
- Timer: Detects abnormally fast movement and interaction packets.
- Fly: Flags flying in Adventure mode.
- Speed: Flags sustained horizontal movement speed above a threshold.
- Infinite Stamina: Flags sprinting without stamina depletion.

## Features
- Staff Alerts in Chat
- Optional Discord Webhook Alerts
- Optional automatic punishments (disconnect/ban) at high flag counts

## Configuration
The config file is created in the plugin data directory on first run (typically `mods/Al3x_HytaleAC/config.json`).

Default config:
```json
{
  "timer": {
    "enabled": true,
    "minMsInteractionPacket": 9,
    "minNanoMovementPacket": 15000000.0,
    "flagsNeededToAlert": 100,
    "flagsNeededToPunish": 1000
  },
  "fly": {
    "enabled": true,
    "flagsNeededToAlert": 1,
    "flagsNeededToPunish": 5
  },
  "speed": {
    "enabled": true,
    "maxSpeedThreshold": 9.5,
    "flagsNeededToAlert": 1,
    "flagsNeededToPunish": 5
  },
  "infStamina": {
    "enabled": true,
    "flagsNeededToAlert": 4,
    "flagsNeededToPunish": 8
  },
  "alerts": {
    "resetIntervalSeconds": 60,
    "notifyReset": true,
    "debugMode": false,
    "discordWebhookUrl": ""
  },
  "punishments": {
    "reason": "[Anticheat] Cheating",
    "shouldBan": false
  }
}
```

Config options:

| Path | Type | Default | Description |
| --- | --- | --- | --- |
| `timer.enabled` | boolean | `true` | Enables or disables the timer check entirely. |
| `timer.minMsInteractionPacket` | int | `9` | Minimum milliseconds between interaction packets before flagging. |
| `timer.minNanoMovementPacket` | number | `15000000.0` | Minimum average nanoseconds between movement packets before flagging. |
| `timer.flagsNeededToAlert` | int | `100` | Flags required before sending a staff alert (timer). |
| `timer.flagsNeededToPunish` | int | `1000` | Flags required before punishments trigger (timer). |
| `fly.enabled` | boolean | `true` | Enables or disables the fly check entirely. |
| `fly.flagsNeededToAlert` | int | `1` | Flags required before sending a staff alert (fly). |
| `fly.flagsNeededToPunish` | int | `5` | Flags required before punishments trigger (fly). |
| `speed.enabled` | boolean | `true` | Enables or disables the speed check entirely. |
| `speed.maxSpeedThreshold` | number | `14.0` | Maximum horizontal speed before flagging. |
| `speed.flagsNeededToAlert` | int | `1` | Flags required before sending a staff alert (speed). |
| `speed.flagsNeededToPunish` | int | `5` | Flags required before punishments trigger (speed). |
| `infStamina.enabled` | boolean | `true` | Enables or disables the infinite stamina check entirely. |
| `infStamina.flagsNeededToAlert` | int | `2` | Flags required before sending a staff alert (stamina). |
| `infStamina.flagsNeededToPunish` | int | `4` | Flags required before punishments trigger (stamina). |
| `alerts.resetIntervalSeconds` | int | `60` | Interval in seconds to clear player flags. |
| `alerts.notifyReset` | boolean | `true` | If `true`, notifies staff members when flags are reset. |
| `alerts.debugMode` | boolean | `false` | If `true`, prints debug output to the server console for different checks. And doesn't execute punishments |
| `alerts.discordWebhookUrl` | string | `""` | Discord webhook URL for alert embeds; leave empty to disable. |
| `punishments.reason` | string | `"[Anticheat] Cheating"` | Reason used for disconnects/bans and punishment webhooks. |
| `punishments.shouldBan` | boolean | `false` | If `true`, bans instead of disconnecting when punishments trigger. |

## Webhook alerts
If `alerts.discordWebhookUrl` is set, staff alerts and punishment actions also post to Discord. Alerts are rate-limited to once per second except for punishments.

## Punishments
When any check reaches its `flagsNeededToPunish` threshold, the plugin will disconnect the player by default and send a punishment webhook. If `punishments.shouldBan` is `true`, it will issue an infinite ban instead.

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
- Too many/false flags: increase the relevant check's `flagsNeededToAlert` or `flagsNeededToPunish`, and/or relax thresholds (for example `timer.minMsInteractionPacket`, `timer.minNanoMovementPacket`, or `speed.maxSpeedThreshold`).
- No staff alerts: ensure staff have `hytaleac.alerts`, and that they toggled alerts with `/alerts`.
- Logs always empty: logs reset on `alerts.resetIntervalSeconds`, this is to prevent false flags from cluttering logs.
- No Discord alerts: verify `alerts.discordWebhookUrl` is a valid webhook url.

## Performance Notes
Hytale is still new and the server API is evolving. While this plugin has been tested on lower-end hardware, performance can vary based on server load and player count. And may break if a sudden update changes how packets work. Always monitor your server's performance after adding new plugins.
