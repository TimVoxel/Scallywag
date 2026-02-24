package me.timpixel.scallywag;

import me.timpixel.scallywag.commands.*;
import me.timpixel.scallywag.database.NativeDatabaseConnector;
import me.timpixel.scallywag.exceptions.ScallywagAuthenticationSetException;
import me.timpixel.scallywag.exceptions.ScallywagNativeSourceException;
import me.timpixel.scallywag.exceptions.ScallywagNoAuthenticationException;
import me.timpixel.scallywag.exceptions.ScallywagUninitializedException;
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
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScallywagPlugin extends JavaPlugin implements Scallywag, LoginHolder, RegistrationHolder
{
    static
    {
        ConfigurationSerialization.registerClass(DatabaseConnectionInfo.class);
    }

    private static ScallywagPlugin instance;
    private Logger logger;
    private NativeDatabaseConnector nativeDatabaseConnector;
    private AuthenticationSource authenticationSource;

    private LoginManager loginManager = null;
    private RegistrationManager registrationManager = null;

    private static Permission adminPermission;

    @Override
    public void onEnable()
    {
        adminPermission = getServer().getPluginManager().getPermission("scallywag.admin");

        logger = getLogger();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new PasswordLogFilter());

        var config = new ScallywagConfig(getConfig(), new File(getDataFolder(), "config.yml"));
        authenticationSource = config.authenticationSource();

        logger.info("Authentication source: " + authenticationSource);

        if (authenticationSource == AuthenticationSource.NATIVE_DATABASE)
        {
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

        registerCommand("register", new RegisterCommand(this, allowPlayerRegistration));
        registerCommand("login", new LoginCommand(this));
        registerCommand("registration", new RegistrationCommand(this));
        registerCommand("password", new PasswordCommand(this, allowPlayerPasswordChanging));

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

    private void initializeNativeAuthenticator(ScallywagConfig config) throws SQLException
    {
        logger.info("Connecting to the native database...");
        nativeDatabaseConnector = NativeDatabaseConnector.tryCreate(config.databaseConnectionInfo());
        nativeDatabaseConnector.init();
        var automaticallyLogInUponRegistration = config.isAutoLogInUponRegistration();

        var authenticationManager = new NativeDatabaseAuthenticationManager(this, nativeDatabaseConnector, automaticallyLogInUponRegistration);
        loginManager = authenticationManager;
        registrationManager = authenticationManager;
    }

    private void registerEvents(ScallywagConfig config,
                                Integer timeOutSeconds)
    {
        var pluginManager = getServer().getPluginManager();

        if (config.isFreezeUnauthorisedPlayers())
        {
            var unauthorisedPlayerListener = new UnauthorisedPlayerListener(loginManager, config.isSetUnauthorisedInvulnerable());
            pluginManager.registerEvents(unauthorisedPlayerListener, this);
        }

        var playerJoinListener = new PlayerJoinQuitListener(loginManager,
                config.isKeepQuittersLoggedIn(),
                config.isApplyDarknessToUnauthorisedPlayers(),
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

        if (loginManager instanceof NativeDatabaseAuthenticationManager databaseRegistrationManager)
        {
            databaseRegistrationManager.shutdown();
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

    @ApiStatus.Internal
    @Override
    public void set(LoginManager manager) throws ScallywagUninitializedException,
            ScallywagNativeSourceException, ScallywagAuthenticationSetException
    {
        if (authenticationSource == null)
        {
            throw new ScallywagUninitializedException();
        }
        if (authenticationSource == AuthenticationSource.NATIVE_DATABASE)
        {
            throw new ScallywagNativeSourceException();
        }
        if (loginManager != null)
        {
            throw new ScallywagAuthenticationSetException();
        }
        loginManager = manager;
    }

    @ApiStatus.Internal
    @Override
    public void set(RegistrationManager manager) throws ScallywagUninitializedException
    {
        if (manager == null)
        {
            throw new ScallywagUninitializedException();
        }
        registrationManager = manager;
    }

    @ApiStatus.Internal
    public static Logger logger()
    {
        return instance.logger;
    }

    @ApiStatus.Internal
    public static LoginHolder login() { return instance; }

    @ApiStatus.Internal
    public static RegistrationHolder registration() { return instance; }

    @ApiStatus.Internal
    @Override
    public LoginManager loginManager() throws ScallywagNoAuthenticationException
    {
        if (loginManager == null)
        {
            throw new ScallywagNoAuthenticationException();
        }
        return loginManager;
    }

    @ApiStatus.Internal
    @Override
    public RegistrationManager registrationManager() throws ScallywagNoAuthenticationException
    {
        if (registrationManager == null)
        {
            throw new ScallywagNoAuthenticationException();
        }
        return registrationManager;
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