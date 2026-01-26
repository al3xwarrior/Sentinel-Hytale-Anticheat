package com.al3x.utils;

import com.al3x.AnticheatPlayer;
import com.al3x.config.AnticheatConfig;
import com.al3x.flags.Flag;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordWebhook {
    private static long lastSendTime = 0;

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void sendPunishment(AnticheatPlayer acPlayer, String punishmentType, String reason) {
        String discordWebhookUrl = AnticheatConfig.getDiscordWebhookUrl();
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty())
            return;

        DiscordMessage message = new DiscordMessage()
                .setContent("")
                .addEmbed(new Embed()
                        .setTitle("🚨 Anticheat Punishment: ")
                        .setDescription("**Player:** " + acPlayer.getPlayer().getDisplayName() + "\n" +
                                        "**Punishment:** " + punishmentType + "\n" +
                                        "**Reason:** " + reason + "\n" +
                                        "**Total Flags:** " + acPlayer.getFlagAmount())
                        .setColor(0xFFA500)
                );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordWebhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message.toJson()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() >= 300)
                        System.err.println("Webhook Error [" + res.statusCode() + "]: " + res.body());
                });
    }

    public static void sendFlag(AnticheatPlayer acPlayer, Flag flag) {
        long now = System.currentTimeMillis();
        if (now - lastSendTime < 1000) // 1 second cooldown
            return;
        lastSendTime = now;

        String discordWebhookUrl = AnticheatConfig.getDiscordWebhookUrl();
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty())
            return;

        DiscordMessage message = new DiscordMessage()
                .setContent("")
                .addEmbed(new Embed()
                        .setTitle("⚠️ Anticheat Alert: ")
                        .setDescription("**Player:** " + acPlayer.getPlayer().getDisplayName() + "\n" +
                                        "**Check:** " + flag.getReason() + "\n" +
                                        "**Details:** " + flag.getDetails() + "\n" +
                                        "**Total Flags:** " + acPlayer.getFlagAmount())
                        .setColor(0xFF0000)
                );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discordWebhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message.toJson()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() >= 300)
                        System.err.println("Webhook Error [" + res.statusCode() + "]: " + res.body());
                });
    }

    public void setLastSendTime(int lastSendTime) {
        DiscordWebhook.lastSendTime = lastSendTime;
    }

    public static class DiscordMessage {
        private String content;
        private final List<Embed> embeds = new ArrayList<>();

        public DiscordMessage setContent(String content) { this.content = content; return this; }
        public DiscordMessage addEmbed(Embed embed) { this.embeds.add(embed); return this; }

        public String toJson() {
            StringBuilder json = new StringBuilder("{");
            if (content != null) json.append("\"content\":\"").append(escape(content)).append("\",");

            if (!embeds.isEmpty()) {
                json.append("\"embeds\": [")
                        .append(embeds.stream().map(Embed::toJson).collect(Collectors.joining(",")))
                        .append("]");
            }
            return json.append("}").toString().replace(",]", "]");
        }
    }

    public static class Embed {
        private String title, description, thumbnail;
        private int color = 0;

        public Embed setTitle(String t) { this.title = t; return this; }
        public Embed setDescription(String d) { this.description = d; return this; }
        public Embed setColor(int c) { this.color = c; return this; }
        public Embed setThumbnail(String url) { this.thumbnail = url; return this; }

        public String toJson() {
            return String.format(
                    "{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d,\"timestamp\":\"%s\"%s}",
                    escape(title), escape(description), color, Instant.now(),
                    thumbnail != null ? ",\"thumbnail\":{\"url\":\""+thumbnail+"\"}" : ""
            );
        }
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("\"", "\\\"").replace("\n", "\\n");
    }
}