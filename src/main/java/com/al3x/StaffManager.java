package com.al3x;

import com.al3x.flags.Flag;
import com.al3x.utils.DiscordWebhook;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaffManager {

    private final Set<UUID> alertUsers;

    public StaffManager() {
        this.alertUsers = ConcurrentHashMap.newKeySet();
    }

    public void alert(AnticheatPlayer cheater, Flag flag) {
        DiscordWebhook.sendFlag(cheater, flag);
        Universe.get().getPlayers().forEach(playerRef -> {
            Player player = cheater.getPlayer();
            if (isAlertUser(playerRef.getUuid()) && playerRef.getComponent(Player.getComponentType()).hasPermission("hytaleac.alerts")) {
                playerRef.sendMessage(Message.join(
                        Message.raw("[Anticheat] ").color(Color.RED),
                        Message.raw(player.getDisplayName() + " ").color(Color.YELLOW),
                        Message.raw(flag.getReason()).color(Color.WHITE),
                        Message.raw(" (" + cheater.getFlagAmount() + ")").color(Color.GRAY)
                ));
            }
        });
    }

    public void alertViolationsReset() {
        Universe.get().getPlayers().forEach(playerRef -> {
            UUID uuid = playerRef.getUuid();
            if (isAlertUser(uuid)) {
                playerRef.sendMessage(Message.join(
                        Message.raw("[Anticheat] ").color(Color.RED),
                        Message.raw("All player violations have been reset!").color(Color.WHITE)
                ));
            }
        });
    }

    public void addAlertUser(UUID uuid) {
        if (!alertUsers.contains(uuid)) {
            alertUsers.add(uuid);
        }
    }
    public void removeAlertUser(UUID uuid) {
        alertUsers.remove(uuid);
    }

    public boolean isAlertUser(UUID uuid) {
        return alertUsers.contains(uuid);
    }

}
