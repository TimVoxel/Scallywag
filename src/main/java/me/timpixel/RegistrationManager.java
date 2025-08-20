package me.timpixel;

import me.timpixel.database.DatabaseManager;
import me.timpixel.listeners.LoginListener;
import org.bukkit.entity.Player;
import java.util.*;

public interface RegistrationManager
{
    static RegistrationManager database(DatabaseManager databaseManager, boolean automaticallyLogInUponRegistration)
    {
        return new DatabaseRegistrationManager(databaseManager, automaticallyLogInUponRegistration);
    }

     LoginResult tryLogIn(UUID uuid, String username, String password);
     void tryLogOut(UUID uuid, String username);

     RegistrationResult tryRegister(UUID uuid, String username, String password);
     RegistrationRemovalResult tryRemoveRegistration(UUID uuid);
     RegistrationRemovalResult tryRemoveRegistration(String username);

     boolean isLoggedIn(UUID uuid);
     boolean isLoggedIn(Player player);

     void addListener(LoginListener listener);
     void removeListener(LoginListener listener);

     <T> UpdateResult updateRegistrationProperty(UUID uuid, RegistrationVariableProperty<T> property, T value);
     <T> UpdateResult updateRegistrationProperty(String username, RegistrationVariableProperty<T> property, T value);

     List<String> registeredUsernames();
}

