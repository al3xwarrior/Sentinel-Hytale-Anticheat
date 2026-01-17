package com.al3x.config;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class AnticheatConfig {

    private static final String FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean timerEnabled = true;
    private static int timerMinMsInteractionPacket = 10;
    private static double timerMinNanoMovementPacket = 15000000.0;
    private static int timerMaxFlags = 30;

    private static boolean alertNotifyReset = true;
    private static int alertResetIntervalSeconds = 60;

    private static boolean debugMode = false;
    private static String discordWebhookUrl = "";

    private static String punishmentReason = "[Anticheat] Cheating";
    private static boolean shouldBan = false;
    private static int punishmentMaxFlags = 150;

    private AnticheatConfig() {}

    public static void reload(JavaPlugin plugin) {
        Path path = plugin.getDataDirectory().resolve(FILE_NAME);
        Data data = null;
        boolean shouldSave = false;

        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                data = GSON.fromJson(json, Data.class);
            } catch (Exception e) {
                plugin.getLogger().at(Level.WARNING).log("Failed to read config.json, using defaults: %s", e.getMessage());
                data = null;
            }
        } else {
            shouldSave = true;
        }

        if (data == null) {
            data = new Data();
            shouldSave = true;
        }

        if (normalize(data)) {
            shouldSave = true;
        }

        apply(data);

        if (shouldSave) {
            save(plugin, path, data);
        }
    }

    private static boolean normalize(Data data) {
        boolean changed = false;
        if (data.timer == null) {
            data.timer = new Timer();
            changed = true;
        }
        if (data.alerts == null) {
            data.alerts = new Alerts();
            changed = true;
        }
        return changed;
    }

    private static void apply(Data data) {
        timerEnabled = data.timer.enabled;
        timerMinMsInteractionPacket = data.timer.minMsInteractionPacket;
        timerMinNanoMovementPacket = data.timer.minNanoMovementPacket;
        timerMaxFlags = data.timer.maxFlags;

        alertNotifyReset = data.alerts.notifyReset;
        alertResetIntervalSeconds = data.alerts.resetIntervalSeconds;

        debugMode = data.alerts.debugMode;
        discordWebhookUrl = data.alerts.discordWebhookUrl;

        punishmentReason = data.punishments.reason;
        shouldBan = data.punishments.shouldBan;
        punishmentMaxFlags = data.punishments.maxFlags;
    }

    private static void save(JavaPlugin plugin, Path path, Data data) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(data));
        } catch (IOException e) {
            plugin.getLogger().at(Level.WARNING).log("Failed to write config.json: %s", e.getMessage());
        }
    }

    public static boolean isTimerEnabled() {
        return timerEnabled;
    }

    public static int getTimerMinMsInteractionPacket() {
        return timerMinMsInteractionPacket;
    }

    public static String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public static double getTimerMinNanoMovementPacket() {
        return timerMinNanoMovementPacket;
    }

    public static int getTimerMaxFlags() {
        return timerMaxFlags;
    }

    public static int getAlertResetIntervalSeconds() {
        return alertResetIntervalSeconds;
    }

    public static boolean isAlertNotifyReset() {
        return alertNotifyReset;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static String getPunishmentReason() {
        return punishmentReason;
    }

    public static boolean shouldBan() {
        return shouldBan;
    }

    public static int getPunishmentMaxFlags() {
        return punishmentMaxFlags;
    }

    private static final class Data {
        private Timer timer = new Timer();
        private Alerts alerts = new Alerts();
        private Punishments punishments = new Punishments();
    }

    private static final class Timer {
        private boolean enabled = true;
        private int minMsInteractionPacket = 10;
        private double minNanoMovementPacket = 15000000.0;
        private int maxFlags = 30;
    }

    private static final class Alerts {
        private int resetIntervalSeconds = 60;
        private boolean notifyReset = true;
        private boolean debugMode = false;
        private String discordWebhookUrl = "";
    }

    private static final class Punishments {
        private String reason = "[Anticheat] Cheating";
        private boolean shouldBan = false;
        private int maxFlags = 150;
    }
}
