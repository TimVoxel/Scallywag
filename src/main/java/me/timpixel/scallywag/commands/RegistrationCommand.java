package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RegistrationCommand extends RootCommand
{
    private final AuthenticationManager manager;

    public RegistrationCommand(AuthenticationManager manager)
    {
        this.manager = manager;
    }

    @Override
    protected SubCommand[] getSubCommands()
    {
        return new SubCommand[] {
            new RegistrationAddCommand(this),
            new RegistrationRemoveCommand(this),
            new RegistrationModifyCommand(this)
        };
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

    public AuthenticationManager manager() { return manager; }
}
