package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RegistrationCommand extends RootCommand
{
    private final RegistrationHolder holder;

    public RegistrationCommand(RegistrationHolder holder)
    {
        this.holder = holder;
    }

    @Override
    protected SubCommand[] getSubCommands()
    {
        var sub = new SubCommand[3];
        sub[0] = new RegistrationAddCommand(this);
        sub[1] = new RegistrationRemoveCommand(this);
        sub[2] = new RegistrationModifyCommand(this);
        return sub;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args)
    {
        if (!ScallywagPlugin.hasAdminPermission(sender))
        {
            return CommandLogger.error(sender, "You have no permission to use this command");
        }
        return super.onCommand(sender, command, s, args);
    }

    public RegistrationHolder holder() { return holder; }
}
