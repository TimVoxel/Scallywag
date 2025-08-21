package me.timpixel.listeners;

import me.timpixel.RegistrationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerJoinQuitListener implements Listener, LoginListener
{
    private final static PotionEffect DARKNESS_EFFECT = new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 1, true, false);

    private final Map<UUID, BukkitRunnable> timeOuts;

    private final RegistrationManager registrationManager;
    private final boolean keepQuittersLoggedIn;
    private final boolean applyDarkness;
    private final Integer timeOutSeconds;
    private final JavaPlugin plugin;

    public PlayerJoinQuitListener(RegistrationManager registrationManager,
                                  boolean keepQuittersLoggedIn,
                                  boolean applyDarkness,
                                  Integer timeOutSeconds,
                                  JavaPlugin plugin)
    {
        this.registrationManager = registrationManager;
        this.keepQuittersLoggedIn = keepQuittersLoggedIn;
        this.applyDarkness = applyDarkness;
        this.timeOutSeconds = timeOutSeconds;
        this.plugin = plugin;
        this.timeOuts = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event)
    {
        var player = event.getPlayer();

        if (applyDarkness)
        {
            player.addPotionEffect(DARKNESS_EFFECT);
        }

        startTimeOutRunnable(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (!keepQuittersLoggedIn)
        {
            var player = event.getPlayer();
            registrationManager.tryLogOut(player.getUniqueId(), player.getName());
        }
    }

    @Override
    public void onPlayerLoggedIn(UUID uuid, String username)
    {
        System.out.println("HELLOEEEEEEE");
        if (applyDarkness)
        {
            var player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline())
            {
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
        }

        var timeOut = timeOuts.get(uuid);

        if (timeOut != null)
        {
            timeOut.cancel();
            timeOuts.remove(uuid);
        }
    }

    @Override
    public void onPlayerLoggedOut(UUID uuid, String username)
    {
        var player = Bukkit.getPlayer(uuid);

        if (applyDarkness && player != null && player.isOnline())
        {
            player.addPotionEffect(DARKNESS_EFFECT);
        }

        startTimeOutRunnable(uuid);
    }

    private void startTimeOutRunnable(final UUID uuid)
    {
        if (timeOutSeconds != null)
        {
            var runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    var player = Bukkit.getPlayer(uuid);

                    if (player != null && player.isOnline())
                    {
                        player.kick(Component.text("Timed out (Scallywag authentication)"), PlayerKickEvent.Cause.TIMEOUT);
                    }
                }
            };
            runnable.runTaskLater(plugin, timeOutSeconds * 20);
            timeOuts.put(uuid, runnable);
        }
    }
}
