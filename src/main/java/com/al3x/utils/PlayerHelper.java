package com.al3x.utils;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerHelper {

    // Stat Related Methods
    public static void setHealth(Player player, float amount) {
        player.getWorld().execute(() -> {
            Store<EntityStore> playerStore = player.getWorld().getEntityStore().getStore();
            EntityStatMap entityStatMap = playerStore.getComponent(player.getReference(), EntityStatMap.getComponentType());
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (entityStatMap != null) {
                entityStatMap.setStatValue(healthIndex, amount);
            }
        });
    }
    public static void fullHeal(Player player) {
        setHealth(player, 999.0f);
    }
    public static void fullHeal(PlayerRef playerRef) {
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        world.execute(() -> {
            Store<EntityStore> playerStore = world.getEntityStore().getStore();
            EntityStatMap entityStatMap = playerStore.getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
            if (entityStatMap != null) {
                entityStatMap.setStatValue(DefaultEntityStatTypes.getHealth(), 999.0f);
            }
        });
    }
    public static int getPlayerHealth(Player player) {
        Store<EntityStore> playerStore = player.getWorld().getEntityStore().getStore();
        EntityStatMap entityStatMap = playerStore.getComponent(player.getReference(), EntityStatMap.getComponentType());
        if (entityStatMap != null) {
            return (int) entityStatMap.get(DefaultEntityStatTypes.getHealth()).get();
        }
        return 0;
    }
    public static int getPlayerHealth(PlayerRef playerRef) {
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        Store<EntityStore> playerStore = world.getEntityStore().getStore();
        EntityStatMap entityStatMap = playerStore.getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
        if (entityStatMap != null) {
            return (int) entityStatMap.get(DefaultEntityStatTypes.getHealth()).get();
        }
        return 0;
    }

    // Position Related Methods
    public static void teleportPlayer(Player player, double x, double y, double z) {
        World world = player.getWorld();
        if (world == null) return;
        world.execute(() -> {
            if (player.getReference() == null) return;
            Store<EntityStore> store = player.getReference().getStore();
            Teleport teleport = Teleport.createForPlayer(new Transform(x, y, z));
            store.addComponent(player.getReference(), Teleport.getComponentType(), teleport);
        });
    }

    // Permission Related Methods
    public static boolean isOp(Player player) {
        return PermissionsModule.get().hasPermission(player.getUuid(), "OP");
    }
    public static boolean isOp(PlayerRef playerRef) {
        return PermissionsModule.get().hasPermission(playerRef.getUuid(), "OP");
    }

}
