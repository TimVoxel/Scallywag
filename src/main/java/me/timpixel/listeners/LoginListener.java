package me.timpixel.listeners;

import java.util.UUID;

public interface LoginListener
{
    void onPlayerLoggedIn(UUID uuid, String username);
    void onPlayerLoggedOut(UUID uuid, String username);
}
