package com.al3x;

import com.al3x.config.AnticheatConfig;
import com.al3x.flags.Flag;
import com.al3x.flags.TimerFlag;
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
import java.util.UUID;

public class AnticheatPlayer {

    private final Main main;
    private final StaffManager staffManager;
    private final ArrayList<Flag> flags;
    private int timerFlags;
    private final UUID uuid;
    private final Player player;

    public AnticheatPlayer(Main main, Player player) {
        this.main = main;
        this.staffManager = main.getStaffManager();
        this.timerFlags = 0;
        this.uuid = player.getUuid();
        this.flags = new ArrayList<>();
        this.player = player;
    }
    public AnticheatPlayer(Main main, PlayerRef playerRef) {
        this.main = main;
        this.staffManager = main.getStaffManager();
        this.timerFlags = 0;
        this.uuid = playerRef.getUuid();
        this.flags = new ArrayList<>();
        this.player = playerRef.getComponent(Player.getComponentType());
    }

    public void flagPlayer(Flag flag) {
        flags.add(flag);

        if (flag instanceof TimerFlag timerFlag) {
            timerFlags++;
            if (timerFlags >= timerFlag.getMaxFlags()) {
                timerFlags = 0;
                staffManager.alert(this, timerFlag);
            }
        }

        if (flags.size() >= AnticheatConfig.getPunishmentMaxFlags()) {
            DiscordWebhook.sendPunishment(this, "BAN", "Excessive Flags");

            String reason = AnticheatConfig.getPunishmentReason();

            if (AnticheatConfig.shouldBan()) {
                System.out.println("[Anticheat] Banning player " + player.getDisplayName() + " for reason: " + reason);
                player.getWorld().execute(() -> {
                    InfiniteBan ban = new InfiniteBan(uuid, player.getPlayerRef().getReference().getStore().getComponent(player.getPlayerRef().getReference(), UUIDComponent.getComponentType()).getUuid(), Instant.now(), reason);
                    if (ban == null) {
                        System.err.println("[Anticheat] Failed to create ban for player " + player.getDisplayName());
                        return;
                    }
                    HytaleBanProvider banProvider = main.getBanProvider();
                    if (banProvider.modify(uuids -> {uuids.put(uuid, ban);return true;})) {
                        player.getPlayerRef().getPacketHandler().disconnect(reason);
                    }
                });
            } else {
                System.out.println("[Anticheat] Disconnecting player " + player.getDisplayName() + " for reason: " + reason);
                player.getPlayerRef().getPacketHandler().disconnect(reason);
            }
        }
    }

    public void resetFlags() {
        timerFlags = 0;
        flags.clear();
    }

    public ArrayList<Flag> getFlags() {
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
