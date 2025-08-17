package me.timpixel;

import me.timpixel.database.DatabaseConnectionInfo;
import me.timpixel.database.DatabaseManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
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

    @Override
    public void onEnable()
    {
        logger = getLogger();
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

    public DatabaseManager databaseManager() { return databaseManager; }
}