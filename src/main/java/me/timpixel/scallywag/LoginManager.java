package me.timpixel.scallywag;

import me.timpixel.scallywag.results.*;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.function.Consumer;

public interface LoginManager
{
    void tryLogIn(UUID uuid, String username, String password, Consumer<LoginResult> callback);
    void logIn(UUID uuid, String username);

    void tryLogOut(UUID uuid, String username);

    boolean isLoggedIn(UUID uuid);
    default boolean isLoggedIn(Player player) { return isLoggedIn(player.getUniqueId()); }
}

