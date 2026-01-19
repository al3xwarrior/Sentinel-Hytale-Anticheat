package com.al3x.checks;

import com.al3x.AnticheatPlayer;
import com.al3x.Main;
import com.al3x.config.AnticheatConfig;
import com.al3x.flags.TimerFlag;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerCheck {

    private final Main main;

    // Movement Packet Timestamps
    private final Map<UUID, Long> lastPacketNs = new HashMap<>(); // last packet time in nanoseconds
    private final Map<UUID, ArrayList<Long>> recentTimeDifferences = new HashMap<>(); // used for averaging

    // Interaction Packet Timestamps
    private final HashMap<UUID, Long> syncPacketTimestamps;

    public TimerCheck(Main main) {
        this.main = main;
        this.syncPacketTimestamps = new HashMap<>();
    }

    public void register() {
        PacketAdapters.registerInbound((PlayerPacketFilter) (playerRef, packet) -> {
            if (!AnticheatConfig.isTimerEnabled()) return false;

            // Interaction Packets
            if (packet instanceof SyncInteractionChains) {
                UUID playerUUID = playerRef.getUuid();
                long currentTime = System.currentTimeMillis();

                if (syncPacketTimestamps.containsKey(playerUUID)) {
                    long pastTime = syncPacketTimestamps.get(playerUUID);
                    long timeDifference = currentTime - pastTime;
                    syncPacketTimestamps.put(playerUUID, currentTime);

                    // Ignore if time difference is 0 (happens when tabbing out while swinging and possibly other things)
                    if (timeDifference == 0 || timeDifference == 1) return false;

                    // (The big IF statement)
                    int thresholdMs = AnticheatConfig.getTimerMinMsInteractionPacket();
                    int maxFlags = AnticheatConfig.getTimerMaxFlags();
                    if (timeDifference < thresholdMs) {
                        AnticheatPlayer acPlayer = main.getAnticheatPlayer(playerUUID);
                        if (AnticheatConfig.isDebugMode())
                            System.out.println("[TimerCheck] " + playerRef.getUsername() + " - Time Difference" + timeDifference + " - Source: Movement Packet");
                        if (acPlayer == null) return false;
                        acPlayer.flagPlayer(new TimerFlag(timeDifference, TimerFlag.SourceType.INTERACTION_PACKET, maxFlags));
                    }
                } else {
                    syncPacketTimestamps.put(playerUUID, currentTime);
                }
                return false;
            }

            // Movement Packets
            if (packet instanceof ClientMovement mv) {
                if (mv.absolutePosition == null || mv.velocity == null) return false;

                UUID uuid = playerRef.getUuid();

                Long lastNs = lastPacketNs.get(uuid);
                if (lastNs == null) {
                    lastPacketNs.put(uuid, System.nanoTime());
                    return false;
                }

                long now = System.nanoTime();
                lastPacketNs.put(uuid, now);

                ArrayList<Long> timeDiffs = recentTimeDifferences.computeIfAbsent(uuid, k -> new ArrayList<>());
                if (timeDiffs.size() > 20L) {
                    timeDiffs.removeFirst();
                }
                timeDiffs.add(now - lastNs);

                int average = (int) timeDiffs.stream().mapToLong(Long::longValue).average().orElse(0);

                if (average < AnticheatConfig.getTimerMinNanoMovementPacket()) {
                    AnticheatPlayer acPlayer = main.getAnticheatPlayer(playerRef.getUuid());
                    if (AnticheatConfig.isDebugMode())
                        System.out.println("[TimerCheck] " + playerRef.getUsername() + " - Avg MS: " + round3((double) average / 1000000) + " - Source: Movement Packet");
                    if (acPlayer == null) return false;
                    acPlayer.flagPlayer(new TimerFlag((long) round3((double) average / 1000000), TimerFlag.SourceType.MOVEMENT_PACKET, AnticheatConfig.getTimerMaxFlags()));
                }
            }

            return false;
        });
    }

    public void removePlayer(UUID uuid) {
        lastPacketNs.remove(uuid);
        recentTimeDifferences.remove(uuid);
        syncPacketTimestamps.remove(uuid);
    }

    private static double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
    private static double round3(long v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

}
