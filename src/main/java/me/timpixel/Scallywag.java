package me.timpixel;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Scallywag extends JavaPlugin
{
    private static Scallywag instance;
    private Logger logger;

    @Override
    public void onEnable()
    {
        logger = getLogger();
        instance = this;

        logger.info("Scallywag authentication plugin enabled successfully");
    }

    @Override
    public void onDisable()
    {
        logger.info("Disabled Scallywag authentication plugin");
    }

    public Logger logger()
    {
        return logger;
    }
}