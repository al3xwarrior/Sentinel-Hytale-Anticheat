package com.al3x;

import com.al3x.config.AnticheatConfig;
import com.al3x.flags.*;
import com.al3x.utils.DiscordWebhook;
import com.google.protobuf.SingleFieldBuilder;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AnticheatPlayer {

    private final Main main;
    private final StaffManager staffManager;
    private final List<Flag> flags;
    private int timerFlags;
    private int flyFlags;
    private int speedFlags;
    private int infStaminaFlags;
    private final UUID uuid;
    private final Player player;
    private final PlayerRef playerRef;

    public AnticheatPlayer(Main main, Player player) {
        this.main = main;
        this.staffManager = main.getStaffManager();
        this.timerFlags = 0;
        this.flyFlags = 0;
        this.speedFlags = 0;
        this.infStaminaFlags = 0;
        this.uuid = player.getUuid();
        this.flags = Collections.synchronizedList(new ArrayList<>());
        this.player = player;
        this.playerRef = player.getPlayerRef();
    }
    public AnticheatPlayer(Main main, PlayerRef playerRef) {
        this.main = main;
        this.staffManager = main.getStaffManager();
        this.timerFlags = 0;
        this.uuid = playerRef.getUuid();
        this.flags = Collections.synchronizedList(new ArrayList<>());
        this.player = playerRef.getComponent(Player.getComponentType());
        this.playerRef = playerRef;
    }

    public void flagPlayer(Flag flag) {
        flags.add(flag);

        if (flag instanceof TimerFlag timerFlag) {
            timerFlags++;
            if (timerFlags % timerFlag.getMaxFlags() == 0) {
                staffManager.alert(this, timerFlag);
            }
        }

        if (flag instanceof FlyFlag flyFlag) {
            flyFlags++;
            if (flyFlags % flyFlag.getMaxFlags() == 0) {
                staffManager.alert(this, flyFlag);
            }
        }

        if (flag instanceof SpeedFlag speedFlag) {
            speedFlags++;
            if (speedFlags % speedFlag.getMaxFlags() == 0) {
                staffManager.alert(this, speedFlag);
            }
        }

        if (flag instanceof InfStaminaFlag infStaminaFlag) {
            infStaminaFlags++;
            if (infStaminaFlags % infStaminaFlag.getMaxFlags() == 0) {
                staffManager.alert(this, infStaminaFlag);
            }
        }

        if (!AnticheatConfig.isDebugMode() && (
                timerFlags >= AnticheatConfig.getTimerFlagsNeededToPunish() ||
                flyFlags >= AnticheatConfig.getFlyFlagsNeededToPunish() ||
                speedFlags >= AnticheatConfig.getSpeedFlagsNeededToPunish() ||
                infStaminaFlags >= AnticheatConfig.getInfStaminaFlagsNeededToPunish()
        )) {
            DiscordWebhook.sendPunishment(this, AnticheatConfig.shouldBan()  ? "BAN" : "KICK", "Excessive Flags");

            String reason = AnticheatConfig.getPunishmentReason();

            if (AnticheatConfig.shouldBan()) {
                System.out.println("[Anticheat] Banning player " + player.getDisplayName() + " for reason: " + reason);
                player.getWorld().execute(() -> {
                    InfiniteBan ban = new InfiniteBan(uuid, playerRef.getReference().getStore().getComponent(playerRef.getReference(), UUIDComponent.getComponentType()).getUuid(), Instant.now(), reason);
                    HytaleBanProvider banProvider = main.getBanProvider();
                    if (banProvider.modify(uuids -> {uuids.put(uuid, ban); return true;})) {
                        playerRef.getPacketHandler().disconnect(reason);
                    }
                });
            } else {
                System.out.println("[Anticheat] Disconnecting player " + player.getDisplayName() + " for reason: " + reason);
                playerRef.getPacketHandler().disconnect(reason);
            }
        }
    }

    public void resetFlags() {
        timerFlags = 0;
        flyFlags = 0;
        speedFlags = 0;
        infStaminaFlags = 0;
        flags.clear();
    }

    public List<Flag> getFlags() {
        return flags;
    }
    public UUID getUuid() {
        return uuid;
    }
    public Player getPlayer() {
        return player;
    }
    public int getFlagAmount() {
        return flags.size();
    }
}
