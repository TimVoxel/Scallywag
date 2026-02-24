
package me.timpixel.scallywag;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ScallywagConfig
{
    private final FileConfiguration configuration;

    public ScallywagConfig(FileConfiguration configuration, File saveFile)
    {
        this.configuration = configuration;
        addDefaults();
        configuration.options().copyDefaults(true);

        try
        {
            configuration.save(saveFile);
        }
        catch (IOException exception)
        {
            ScallywagPlugin.logger().log(Level.SEVERE, "Could not save config to " + saveFile, exception);
        }
    }

    private void addDefaults()
    {
        configuration.addDefault("databaseConnection", new DatabaseConnectionInfo(
                "jdbc:mysql://localhost/scallywag",
                "user",
                "password"));

        configuration.addDefault("freezeUnauthorisedPlayers", true);
        configuration.addDefault("keepQuittersLoggedIn", true);
        configuration.addDefault("applyDarknessToUnauthorisedPlayers", true);
        configuration.addDefault("autoLogInUponRegistration", false);
        configuration.addDefault("allowPlayerRegistration", true);
        configuration.addDefault("allowPlayerPasswordChanging", true);
        configuration.addDefault("timeOutSeconds", -1);
        configuration.addDefault("enableDefaultCommandFeedback", true);
        configuration.addDefault("doSetUnauthorisedInvulnerable", true);
        configuration.addDefault("authenticationSource", AuthenticationSource.NATIVE_DATABASE.toString());
    }

    public DatabaseConnectionInfo databaseConnectionInfo()
    {
        return (DatabaseConnectionInfo) configuration.get("databaseConnection");
    }

    public boolean isFreezeUnauthorisedPlayers()
    {
        return configuration.getBoolean("freezeUnauthorisedPlayers");
    }

    public boolean isKeepQuittersLoggedIn()
    {
        return configuration.getBoolean("keepQuittersLoggedIn");
    }

    public boolean isApplyDarknessToUnauthorisedPlayers()
    {
        return configuration.getBoolean("applyDarknessToUnauthorisedPlayers");
    }

    public boolean isAutoLogInUponRegistration()
    {
        return configuration.getBoolean("autoLogInUponRegistration");
    }

    public boolean isAllowPlayerRegistration()
    {
        return configuration.getBoolean("allowPlayerRegistration");
    }

    public boolean isAllowPlayerPasswordChanging()
    {
        return configuration.getBoolean("allowPlayerPasswordChanging");
    }

    public int timeOutSeconds()
    {
        return configuration.getInt("timeOutSeconds");
    }

    public boolean isEnableDefaultCommandFeedback()
    {
        return configuration.getBoolean("enableDefaultCommandFeedback");
    }

    public boolean isSetUnauthorisedInvulnerable()
    {
        return configuration.getBoolean("doSetUnauthorisedInvulnerable");
    }

    public AuthenticationSource authenticationSource()
    {
        var value = configuration.getString("authenticationSource");
        try
        {
            return AuthenticationSource.valueOf(value);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            return AuthenticationSource.NATIVE_DATABASE;
        }
    }
}
