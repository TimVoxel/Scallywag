package me.timpixel.scallywag;

import me.timpixel.scallywag.commands.*;
import me.timpixel.scallywag.database.NativeDatabaseConnector;
import me.timpixel.scallywag.exceptions.ScallywagAuthenticationSetException;
import me.timpixel.scallywag.exceptions.ScallywagNativeSourceException;
import me.timpixel.scallywag.listeners.PlayerJoinQuitListener;
import me.timpixel.scallywag.listeners.UnauthorisedPlayerListener;
import me.timpixel.scallywag.logging.PasswordLogFilter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private NativeDatabaseConnector nativeDatabaseConnector;
    private ExecutorService executorService;
    private AuthenticationManager authenticationManager;

    private static Permission adminPermission;

    @Override
    public void onEnable()
    {
        adminPermission = getServer().getPluginManager().getPermission("scallywag.admin");

        logger = getLogger();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new PasswordLogFilter());

        var config = new ScallywagConfig(getConfig(), new File(getDataFolder(), "config.yml"));

        var authenticationSource = config.authenticationSource();
        authenticationManager = new AuthenticationManager(this, config.isAutoLogInUponRegistration(), authenticationSource);

        logger.info("Authentication source: " + authenticationSource);

        if (authenticationSource == AuthenticationSource.NATIVE_DATABASE)
        {
            executorService = Executors.newFixedThreadPool(config.threadPoolSize());

            try
            {
                initializeNativeAuthenticator(config);
            }
            catch (SQLException exception)
            {
                logger.log(Level.SEVERE, "Unable to initialize the database, disabling Scallywag authentication");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        var allowPlayerRegistration = config.isAllowPlayerRegistration();
        var allowPlayerPasswordChanging = config.isAllowPlayerPasswordChanging();

        registerCommand("register", new RegisterCommand(authenticationManager, allowPlayerRegistration));
        registerCommand("login", new LoginCommand(authenticationManager));
        registerCommand("registration", new RegistrationCommand(authenticationManager));
        registerCommand("password", new PasswordCommand(authenticationManager, allowPlayerPasswordChanging));

        Integer timeOutTime = null;
        var timeOutTimeRaw = config.timeOutSeconds();

        if (timeOutTimeRaw != -1)
        {
            timeOutTime = timeOutTimeRaw;
        }

        registerEvents(config, timeOutTime);

        var enableDefaultCommandFeedback = config.isEnableDefaultCommandFeedback();
        CommandLogger.setEnableDefaultCommandFeedback(enableDefaultCommandFeedback);

        instance = this;
        logger.info("Scallywag authentication plugin enabled successfully");
    }

    @Override
    public void onDisable()
    {
        logger.info("Disabled Scallywag authentication plugin");

        if (executorService != null)
        {
            executorService.shutdown();
        }

        if (nativeDatabaseConnector != null)
        {
            try
            {
                nativeDatabaseConnector.shutdown();
            }
            catch (SQLException exception)
            {
                logger.log(Level.SEVERE, "Unable to shutdown database due to an exception: ", exception);
            }
        }
    }

    private void initializeNativeAuthenticator(ScallywagConfig config) throws SQLException
    {
        logger.info("Connecting to the native database...");
        nativeDatabaseConnector = NativeDatabaseConnector.tryCreate(config.databaseConnectionInfo(), executorService);
        nativeDatabaseConnector.init();

        var registrationManager = new NativeDatabaseAuthenticationHandler(executorService, nativeDatabaseConnector);

        try
        {
            authenticationManager.setAuthenticationHandler(registrationManager);
        }
        catch (ScallywagAuthenticationSetException exception)
        {
            logger.log(Level.SEVERE, "Unable to set the native database authentication handler, a different handler has already been set", exception);
        }
        catch (ScallywagNativeSourceException exception)
        {
            throw new RuntimeException("Impossible", exception);
        }
        authenticationManager.setPasswordValidator(s -> true); //TODO: add native validation
    }

    private void registerEvents(ScallywagConfig config,
                                Integer timeOutSeconds)
    {
        var pluginManager = getServer().getPluginManager();

        if (config.isFreezeUnauthorisedPlayers())
        {
            var unauthorisedPlayerListener = new UnauthorisedPlayerListener(authenticationManager,
                    config.isSetUnauthorisedInvulnerable(),
                    config.isApplyDarknessToUnauthorisedPlayers(),
                    config.limboLocation());
            pluginManager.registerEvents(unauthorisedPlayerListener, this);
        }

        var playerJoinListener = new PlayerJoinQuitListener(authenticationManager,
                config.isKeepQuittersLoggedIn(),
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

    @ApiStatus.Internal
    public static Logger logger()
    {
        return instance.logger;
    }

    @ApiStatus.Internal
    static AuthenticationManager authenticationManager() { return instance.authenticationManager; }

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