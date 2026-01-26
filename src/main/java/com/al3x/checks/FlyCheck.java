package com.al3x.checks;

import com.al3x.AnticheatPlayer;
import com.al3x.Main;
import com.al3x.config.AnticheatConfig;
import com.al3x.flags.FlyFlag;
import com.al3x.flags.TimerFlag;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyCheck {

    private final Main main;

    public FlyCheck(Main main) {
        this.main = main;
    }

    public void register() {
        PacketAdapters.registerInbound((PlayerPacketFilter) (playerRef, packet) -> {
            if (packet instanceof ClientMovement clientMovement) {
                if (!AnticheatConfig.isFlyEnabled()) return false;

                if (clientMovement.movementStates != null) {
                    MovementStates states = clientMovement.movementStates;
                    boolean flying = states.flying;

                    UUID worldUuid = playerRef.getWorldUuid();
                    if (worldUuid == null) return false;

                    World world = Universe.get().getWorld(worldUuid);
                    if (world == null) return false;
                    world.execute(() -> {
                        Player player = playerRef.getComponent(Player.getComponentType());
                        if (player == null) return;

                        GameMode gameMode = player.getGameMode();

                        if (AnticheatConfig.isDebugMode())
                            System.out.println("[FlyCheck] " + playerRef.getUsername() + " - Flying: " + flying);

                        // This should be impossible that I know of so far.
                        if (gameMode.equals(GameMode.Adventure) && flying) {
                            AnticheatPlayer anticheatPlayer = main.getAnticheatPlayer(playerRef.getUuid());
                            if (anticheatPlayer == null) return;
                            anticheatPlayer.flagPlayer(new FlyFlag(AnticheatConfig.getFlyFlagsNeededToAlert()));
                        }
                    });
                }

            }

            return false;
        });
    }
}
