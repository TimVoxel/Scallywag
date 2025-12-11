package me.timpixel.scallywag;

import me.timpixel.scallywag.commands.*;
import me.timpixel.scallywag.database.DatabaseManager;
import me.timpixel.scallywag.listeners.PlayerJoinQuitListener;
import me.timpixel.scallywag.listeners.UnauthorisedPlayerListener;
import me.timpixel.scallywag.logging.PasswordLogFilter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScallywagPlugin extends JavaPlugin implements Scallywag
{
    static
    {
        ConfigurationSerialization.registerClass(DatabaseConnectionInfo.class);
    }

    private static ScallywagPlugin instance;
    private Logger logger;
    private DatabaseManager databaseManager;
    private RegistrationManager registrationManager;

    private static Permission adminPermission;

    @Override
    public void onEnable()
    {
        adminPermission = getServer().getPluginManager().getPermission("scallywag.admin");

        logger = getLogger();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new PasswordLogFilter());

        var config = setupConfig();

        var databaseConnectionInfo = (DatabaseConnectionInfo) config.get("databaseConnection");

        try
        {
            databaseManager = DatabaseManager.tryCreate(databaseConnectionInfo);
            databaseManager.init();
        }
        catch (SQLException exception)
        {
            logger.log(Level.SEVERE, "Unable to initialize the database, disabling Scallywag authentication", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        var automaticallyLogInUponRegistration = config.getBoolean("automaticallyLogInUponRegistration");
        registrationManager = RegistrationManager.database(this, databaseManager, automaticallyLogInUponRegistration);

        var allowPlayerRegistration = config.getBoolean("allowPlayerRegistration");
        var allowPlayerPasswordChanging = config.getBoolean("allowPlayerPasswordChanging");

        registerCommand("register", new RegisterCommand(registrationManager, allowPlayerRegistration));
        registerCommand("login", new LoginCommand(registrationManager));
        registerCommand("registration", new RegistrationCommand(registrationManager));
        registerCommand("password", new PasswordCommand(registrationManager, allowPlayerPasswordChanging));

        Integer timeOutTime = null;
        var timeOutTimeRaw = config.getInt("timeOutSeconds");

        if (timeOutTimeRaw != -1)
        {
            timeOutTime = timeOutTimeRaw;
        }

        registerEvents(config.getBoolean("freezeUnauthorisedPlayers"),
                config.getBoolean("keepQuittersLoggedIn"),
                config.getBoolean("applyDarknessToUnauthorisedPlayers"),
                config.getBoolean("doSetUnauthorisedInvulnerable"),
                timeOutTime);

        var enableDefaultCommandFeedback = config.getBoolean("enableDefaultCommandFeedback");
        CommandLogger.setEnableDefaultCommandFeedback(enableDefaultCommandFeedback);

        instance = this;
        logger.info("Scallywag authentication plugin enabled successfully");
    }

    private FileConfiguration setupConfig()
    {
        var config = getConfig();
        config.addDefault("databaseConnection", new DatabaseConnectionInfo(
                "jdbc:mysql://localhost/scallywag",
                "user",
                "password"));

        config.addDefault("freezeUnauthorisedPlayers", true);
        config.addDefault("keepQuittersLoggedIn", true);
        config.addDefault("applyDarknessToUnauthorisedPlayers", true);
        config.addDefault("autoLogInUponRegistration", false);
        config.addDefault("allowPlayerRegistration", true);
        config.addDefault("allowPlayerPasswordChanging", true);
        config.addDefault("timeOutSeconds", -1);
        config.addDefault("enableDefaultCommandFeedback", true);
        config.addDefault("doSetUnauthorisedInvulnerable", true);
        config.options().copyDefaults(true);
        saveConfig();
        return config;
    }

    private void registerEvents(boolean shouldFreezeNonLoggedIn,
                                boolean keepQuittersLoggedIn,
                                boolean applyDarknessToUnauthorisedPlayers,
                                boolean doSetUnauthorisedInvulnerable,
                                Integer timeOutSeconds)
    {
        var pluginManager = getServer().getPluginManager();

        if (shouldFreezeNonLoggedIn)
        {
            var unauthorisedPlayerListener = new UnauthorisedPlayerListener(registrationManager, doSetUnauthorisedInvulnerable);
            pluginManager.registerEvents(unauthorisedPlayerListener, this);
        }

        var playerJoinListener = new PlayerJoinQuitListener(registrationManager,
                keepQuittersLoggedIn,
                applyDarknessToUnauthorisedPlayers,
                timeOutSeconds,
                this);

        pluginManager.registerEvents(playerJoinListener, this);
    }

    private void registerCommand(String name, TabExecutor executor)
    {
        var command = getCommand(name);

        if (command != null)
        {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        else
        {
            logger.severe("Unable to register command \"" + name + "\"");
        }
    }

    @Override
    public void onDisable()
    {
        logger.info("Disabled Scallywag authentication plugin");

        if (registrationManager instanceof DatabaseRegistrationManager databaseRegistrationManager)
        {
            databaseRegistrationManager.shutdown();
        }

        if (databaseManager != null)
        {
            try
            {
                databaseManager.shutdown();
            }
            catch (SQLException exception)
            {
                logger.log(Level.SEVERE, "Unable to shutdown database due to an exception: ", exception);
            }
        }

    }

    @ApiStatus.Internal
    public static Logger logger()
    {
        return instance.logger;
    }

    @ApiStatus.Internal
    static RegistrationManager registrationManager()
    {
        return instance.registrationManager;
    }

    @ApiStatus.Internal
    public static boolean hasAdminPermission(CommandSender sender)
    {
        return sender.hasPermission(adminPermission);
    }

    @ApiStatus.Internal
    public static boolean isAllowedUnauthorizedCommand(String message)
    {
        return message.startsWith("/l ") || message.startsWith("/r ") || message.startsWith("/login ")
                || message.startsWith("/register ");
    }
}