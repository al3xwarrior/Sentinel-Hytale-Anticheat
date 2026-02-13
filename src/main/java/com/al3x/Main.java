package com.al3x;

import com.al3x.checks.FlyCheck;
import com.al3x.checks.InfStaminaCheck;
import com.al3x.checks.SpeedCheck;
import com.al3x.checks.TimerCheck;
import com.al3x.commands.AlertsCommand;
import com.al3x.commands.AnticheatCommand;
import com.al3x.commands.LogsCommand;
import com.al3x.config.AnticheatConfig;
import com.al3x.utils.WeaponChargeHelper;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    private TimerCheck timerCheck;
    private FlyCheck flyCheck;
    private SpeedCheck speedCheck;
    private InfStaminaCheck infStaminaCheck;

    private WeaponChargeHelper weaponChargeHelper;

    private final ConcurrentHashMap<UUID, AnticheatPlayer> anticheatPlayers;
    private final StaffManager staffManager;
    private ScheduledFuture<?> flagResetTask;
    private HytaleBanProvider banProvider;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        this.anticheatPlayers = new ConcurrentHashMap<>();
        this.staffManager = new StaffManager();
    }

    @Override
    protected void setup() {
        new HStats("d52fa672-6966-47b2-b501-1a11bddebac3", "1.2.1");

        AnticheatConfig.reload(this);
        this.getCommandRegistry().registerCommand(new AlertsCommand(staffManager));
        this.getCommandRegistry().registerCommand(new AnticheatCommand(this));
        this.getCommandRegistry().registerCommand(new LogsCommand(this));

        this.banProvider = getPublic(AccessControlModule.get(), "banProvider");
        if (banProvider == null) throw new RuntimeException("Could not find Hytale Access Control Module");

        // Add Anticheat Player on Connect and Remove on Disconnect + Give Staff Alert Permission
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, (event) -> {
            PlayerRef playerRef = event.getPlayerRef();
            Player player = playerRef.getComponent(Player.getComponentType());
            if (player == null) return;
            anticheatPlayers.put(playerRef.getUuid(), new AnticheatPlayer(this, playerRef));
            if (player.hasPermission("hytaleac.alerts")) {
                staffManager.addAlertUser(playerRef.getUuid());
            }
        });
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
            UUID uuid = event.getPlayerRef().getUuid();
            anticheatPlayers.remove(uuid);
            timerCheck.removePlayer(uuid);
            speedCheck.removePlayer(uuid);
            infStaminaCheck.removePlayer(uuid);
            staffManager.removeAlertUser(uuid);
            weaponChargeHelper.removePlayer(uuid);
        });

        weaponChargeHelper = new WeaponChargeHelper();

        // Register Checks
        timerCheck = new TimerCheck(this);
        flyCheck = new FlyCheck(this);
        speedCheck = new SpeedCheck(this, weaponChargeHelper);
        infStaminaCheck = new InfStaminaCheck(this);
        timerCheck.register();
        flyCheck.register();
        speedCheck.register();
        infStaminaCheck.register();

        scheduleFlagReset();
    }

    public AnticheatPlayer getAnticheatPlayer(UUID uuid) {
        return anticheatPlayers.getOrDefault(uuid, null);
    }

    public StaffManager getStaffManager() {
        return staffManager;
    }

    public void reloadConfig() {
        AnticheatConfig.reload(this);
        scheduleFlagReset();
    }

    private void scheduleFlagReset() {
        if (flagResetTask != null) {
            flagResetTask.cancel(false);
            flagResetTask = null;
        }
        int intervalSeconds = AnticheatConfig.getAlertResetIntervalSeconds();
        if (intervalSeconds <= 0) {
            return;
        }
        flagResetTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            anticheatPlayers.forEach((uuid, acPlayer) -> acPlayer.resetFlags());
            if (AnticheatConfig.isAlertNotifyReset())
                staffManager.alertViolationsReset();
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    // Credits to buuz135 for this reflection method
    public <T> T getPublic(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HytaleBanProvider getBanProvider() {
        return banProvider;
    }

    @Override
    protected void shutdown() {
        if (flagResetTask != null) flagResetTask.cancel(false);
        super.shutdown();
    }
}
