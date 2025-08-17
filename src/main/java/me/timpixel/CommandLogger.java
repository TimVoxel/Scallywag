package me.timpixel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class CommandLogger
{
    public boolean info(CommandSender sender, String message)
    {
        sender.sendMessage(Component.text("[info]" + message));
        return true;
    }

    public boolean warning(CommandSender sender, String message)
    {
        sender.sendMessage(Component.text("[warning]" + message).color(NamedTextColor.YELLOW));
        return true;
    }

    public boolean error(CommandSender sender, String message)
    {
        sender.sendMessage(Component.text("[error]" + message).color(NamedTextColor.RED));
        return true;
    }
}
