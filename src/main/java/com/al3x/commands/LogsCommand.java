package com.al3x.commands;

import com.al3x.AnticheatPlayer;
import com.al3x.Main;
import com.al3x.StaffManager;
import com.al3x.config.AnticheatConfig;
import com.al3x.flags.Flag;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogsCommand extends CommandBase {

    private final Main main;
    private final RequiredArg playerArg;

    public LogsCommand(Main main) {
        super("logs", "View recent player logs");
        this.main = main;
        this.playerArg = withRequiredArg("player", "Player to view logs for", (ArgumentType) ArgTypes.STRING);
        requirePermission("hytaleac.logs");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        String targetPlayerName = (String) playerArg.get(commandContext);

        PlayerRef targetPlayerRef =Universe.get().getPlayer(targetPlayerName, NameMatching.STARTS_WITH_IGNORE_CASE);
        if (targetPlayerRef == null) {
            commandContext.sendMessage(Message.join(
                    Message.raw("[Anticheat] ").color(Color.red),
                    Message.raw("Player not found!").color(Color.white)
            ));
            return;
        }

        AnticheatPlayer acPlayer = main.getAnticheatPlayer(targetPlayerRef.getUuid());
        if (acPlayer == null) {
            commandContext.sendMessage(Message.join(
                    Message.raw("[Anticheat] ").color(Color.RED),
                    Message.raw("Player not found!").color(Color.WHITE)
            ));
            return;
        }

        List<Flag> flags = acPlayer.getFlags();
        if (flags.isEmpty()) {
            commandContext.sendMessage(Message.join(
                    Message.raw("[Anticheat] ").color(Color.RED),
                    Message.raw("No logs for player " + targetPlayerName + " in the last " + AnticheatConfig.getAlertResetIntervalSeconds() + " seconds.").color(Color.WHITE)
            ));
            return;
        }

        commandContext.sendMessage(Message.join(
                Message.raw("[Anticheat] ").color(Color.RED),
                Message.raw("Recent logs for player " + targetPlayerName + ":").color(Color.WHITE)
        ));
        for (Flag flag : flags) {
            commandContext.sendMessage(Message.join(
                    Message.raw(flag.getReason() + " | " + flag.getDetails()).color(Color.GRAY)
            ));
        }

    }
}
