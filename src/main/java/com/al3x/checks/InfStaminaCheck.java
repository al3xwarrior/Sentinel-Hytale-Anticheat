package com.al3x.checks;

import com.al3x.AnticheatPlayer;
import com.al3x.Main;
import com.al3x.config.AnticheatConfig;
import com.al3x.flags.InfStaminaFlag;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfStaminaCheck {

    private final Main main;

    private final ConcurrentHashMap<UUID, Boolean> isPlayerSprinting;
    private final ConcurrentHashMap<UUID, Long> sprintEndTime;

    public InfStaminaCheck(Main main) {
        this.main = main;
        this.isPlayerSprinting = new ConcurrentHashMap<>();
        this.sprintEndTime = new ConcurrentHashMap<>();
    }

    public void register() {
        PacketAdapters.registerInbound((PlayerPacketFilter) (playerRef, packet) -> {
            if (!(packet instanceof ClientMovement clientMovement)) return false;
            if (!AnticheatConfig.isInfStaminaEnabled()) return false;

            UUID uuid = playerRef.getUuid();

            if (clientMovement.movementStates != null) {
                MovementStates states = clientMovement.movementStates;
                isPlayerSprinting.put(playerRef.getUuid(), states.sprinting);
            }

            if (isPlayerSprinting.getOrDefault(uuid, false) == false) return false;

            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                return false;
            }

            world.execute(() -> {
                Player player = playerRef.getComponent(Player.getComponentType());
                if (player == null) return;

                float stamina = getStamina(playerRef);

                if (stamina <= 0.0f) {
                    long currentTime = System.currentTimeMillis();
                    sprintEndTime.putIfAbsent(uuid, currentTime);
                    long lastEndTime = sprintEndTime.get(uuid);
                    if (currentTime - lastEndTime > 300) {
                        sprintEndTime.put(uuid, currentTime);

                        if (AnticheatConfig.isDebugMode())
                            System.out.println("[InfStaminaCheck] Player " + player.getDisplayName() + " has stamina " + stamina + " while sprinting.");

                        AnticheatPlayer acPlayer = main.getAnticheatPlayer(uuid);
                        if (acPlayer != null) {
                            acPlayer.flagPlayer(new InfStaminaFlag(AnticheatConfig.getInfStaminaFlagsNeededToAlert(), stamina));
                        }
                    }
                }
            });


            return false;
        });
    }

    private float getStamina(PlayerRef playerRef) {
        EntityStatMap stats = playerRef.getComponent(EntityStatMap.getComponentType());
        if (stats == null) return 1.0f;
        EntityStatValue stamina = stats.get(DefaultEntityStatTypes.getStamina());
        return stamina != null ? stamina.get() : 1.0f;
    }

    public void removePlayer(UUID uuid) {
        isPlayerSprinting.remove(uuid);
        sprintEndTime.remove(uuid);
    }
}
