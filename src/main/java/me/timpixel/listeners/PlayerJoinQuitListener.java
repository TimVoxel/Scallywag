package me.timpixel.listeners;

import me.timpixel.RegistrationManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerJoinQuitListener implements Listener, LoginListener
{
    private final static PotionEffect DARKNESS_EFFECT = new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 1, true, false);

    private final RegistrationManager registrationManager;
    private final boolean keepQuittersLoggedIn;
    private final boolean applyDarkness;

    public PlayerJoinQuitListener(RegistrationManager registrationManager,
                                  boolean keepQuittersLoggedIn,
                                  boolean applyDarkness)
    {
        this.registrationManager = registrationManager;
        this.keepQuittersLoggedIn = keepQuittersLoggedIn;
        this.applyDarkness = applyDarkness;
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event)
    {
        if (applyDarkness)
        {
            event.getPlayer().addPotionEffect(DARKNESS_EFFECT);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (!keepQuittersLoggedIn)
        {
            registrationManager.tryLogOut(event.getPlayer());
        }
    }

    @Override
    public void onPlayerLoggedIn(UUID uuid, String username)
    {
        if (applyDarkness)
        {
            var player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline())
            {
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
        }
    }

    @Override
    public void onPlayerLoggedOut(UUID uuid, String username)
    {
    }
}
