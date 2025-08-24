package me.timpixel.scallywag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class CommandLogger
{
    private static boolean enableDefaultCommandFeedback;

    public static void setEnableDefaultCommandFeedback(boolean value)
    {
        enableDefaultCommandFeedback = value;
    }

    public static boolean info(CommandSender sender, String message)
    {
        if (enableDefaultCommandFeedback)
        {
            sender.sendMessage(Component.text("[info] " + message));
        }
        return true;
    }

    public static boolean warning(CommandSender sender, String message)
    {
        if (enableDefaultCommandFeedback)
        {
            sender.sendMessage(Component.text("[warning] " + message).color(NamedTextColor.YELLOW));
        }
        return true;
    }

    public static boolean error(CommandSender sender, String message)
    {
        if (enableDefaultCommandFeedback)
        {
            sender.sendMessage(Component.text("[error] " + message).color(NamedTextColor.RED));
        }
        return true;
    }
}
