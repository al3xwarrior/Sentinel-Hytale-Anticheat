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
    private static int timerMinMsInteractionPacket = 9;
    private static double timerMinNanoMovementPacket = 15000000.0;
    private static int timerFlagsNeededToAlert = 100;
    private static int timerFlagsNeededToPunish = 1000;

    private static boolean flyEnabled = true;
    private static int flyFlagsNeededToAlert = 1;
    private static int flyFlagsNeededToPunish = 5;

    private static boolean speedEnabled = true;
    private static double maxSpeedThreshold = 9.5;
    private static int speedFlagsNeededToAlert = 5;
    private static int speedFlagsNeededToPunish = 50;

    private static boolean infStaminaEnabled = true;
    private static int infStaminaFlagsNeededToAlert = 4;
    private static int infStaminaFlagsNeededToPunish = 8;

    private static boolean alertNotifyReset = true;
    private static int alertResetIntervalSeconds = 60;

    private static boolean debugMode = false;
    private static String discordWebhookUrl = "";

    private static String punishmentReason = "[Anticheat] Cheating";
    private static boolean shouldBan = false;

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
        if (data.fly == null) {
            data.fly = new Fly();
            changed = true;
        }
        if (data.speed == null) {
            data.speed = new Speed();
            changed = true;
        }
        if (data.infStamina == null) {
            data.infStamina = new InfStamina();
            changed = true;
        }
        if (data.alerts == null) {
            data.alerts = new Alerts();
            changed = true;
        }
        if (data.punishments == null) {
            data.punishments = new Punishments();
            changed = true;
        }
        return changed;
    }

    private static void apply(Data data) {
        timerEnabled = data.timer.enabled;
        timerMinMsInteractionPacket = data.timer.minMsInteractionPacket;
        timerMinNanoMovementPacket = data.timer.minNanoMovementPacket;
        timerFlagsNeededToAlert = data.timer.flagsNeededToAlert;
        timerFlagsNeededToPunish = data.timer.flagsNeededToPunish;

        flyEnabled = data.fly.enabled;
        flyFlagsNeededToAlert = data.fly.flagsNeededToAlert;
        flyFlagsNeededToPunish = data.fly.flagsNeededToPunish;

        speedEnabled = data.speed.enabled;
        maxSpeedThreshold = data.speed.maxSpeedThreshold;
        speedFlagsNeededToAlert = data.speed.flagsNeededToAlert;
        speedFlagsNeededToPunish = data.speed.flagsNeededToPunish;

        infStaminaEnabled = data.infStamina.enabled;
        infStaminaFlagsNeededToAlert = data.infStamina.flagsNeededToAlert;
        infStaminaFlagsNeededToPunish = data.infStamina.flagsNeededToPunish;

        alertNotifyReset = data.alerts.notifyReset;
        alertResetIntervalSeconds = data.alerts.resetIntervalSeconds;

        debugMode = data.alerts.debugMode;
        discordWebhookUrl = data.alerts.discordWebhookUrl;

        punishmentReason = data.punishments.reason;
        shouldBan = data.punishments.shouldBan;
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
    public static int getTimerFlagsNeededToAlert() {
        return timerFlagsNeededToAlert;
    }
    public static int getTimerFlagsNeededToPunish() {
        return timerFlagsNeededToPunish;
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
    public static boolean isFlyEnabled() {
        return flyEnabled;
    }
    public static int getFlyFlagsNeededToAlert() {
        return flyFlagsNeededToAlert;
    }
    public static int getFlyFlagsNeededToPunish() {
        return flyFlagsNeededToPunish;
    }
    public static boolean isSpeedEnabled() {
        return speedEnabled;
    }
    public static double getMaxSpeedThreshold() {
        return maxSpeedThreshold;
    }
    public static int getSpeedFlagsNeededToAlert() {
        return speedFlagsNeededToAlert;
    }
    public static int getSpeedFlagsNeededToPunish() {
        return speedFlagsNeededToPunish;
    }
    public static boolean isInfStaminaEnabled() {
        return infStaminaEnabled;
    }
    public static int getInfStaminaFlagsNeededToAlert() {
        return infStaminaFlagsNeededToAlert;
    }
    public static int getInfStaminaFlagsNeededToPunish() {
        return infStaminaFlagsNeededToPunish;
    }

    private static final class Data {
        private Timer timer = new Timer();
        private Fly fly = new Fly();
        private Speed speed = new Speed();
        private Alerts alerts = new Alerts();
        private InfStamina infStamina = new InfStamina();
        private Punishments punishments = new Punishments();
    }

    private static final class Timer {
        private boolean enabled = true;
        private int minMsInteractionPacket = 9;
        private double minNanoMovementPacket = 15000000.0;
        private int flagsNeededToAlert = 100;
        private int flagsNeededToPunish = 1000;
    }

    private static final class Fly {
        private boolean enabled = true;
        private int flagsNeededToAlert = 1;
        private int flagsNeededToPunish = 5;
    }

    private static final class Speed {
        private boolean enabled = true;
        private double maxSpeedThreshold = 9.5;
        private int flagsNeededToAlert = 5;
        private int flagsNeededToPunish = 50;
    }

    private static final class InfStamina {
        private boolean enabled = true;
        private int flagsNeededToAlert = 4;
        private int flagsNeededToPunish = 8;
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
        private int maxFlags = 1000;
    }
}
