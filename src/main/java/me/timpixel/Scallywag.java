package me.timpixel;

import me.timpixel.commands.LoginCommand;
import me.timpixel.commands.RegisterCommand;
import me.timpixel.commands.RegistrationCommand;
import me.timpixel.database.DatabaseConnectionInfo;
import me.timpixel.database.DatabaseManager;
import me.timpixel.logging.PasswordLogFilter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Scallywag extends JavaPlugin
{
    static
    {
        ConfigurationSerialization.registerClass(DatabaseConnectionInfo.class);
    }

    private static Scallywag instance;
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

        instance = this;

        logger.info("Scallywag authentication plugin enabled successfully");

        var config = getConfig();
        config.addDefault("databaseConnection", new DatabaseConnectionInfo(
                "jdbc:mysql://localhost/scallywag",
                "user",
                "password"));
        config.options().copyDefaults(true);
        saveConfig();

        var databaseConfig = (DatabaseConnectionInfo) config.get("databaseConnection");
        databaseManager = DatabaseManager.tryCreate(databaseConfig);

        if (databaseManager != null)
        {
            databaseManager.init();
        }

        registrationManager = new RegistrationManager(databaseManager);

        registerCommand("register", new RegisterCommand(registrationManager));
        registerCommand("login", new LoginCommand(registrationManager));
        registerCommand("registration", new RegistrationCommand(registrationManager));
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
    }

    public static Logger logger()
    {
        return instance.logger;
    }

    public static boolean hasAdminPermission(CommandSender sender)
    {
        return sender.hasPermission(adminPermission);
    }
}