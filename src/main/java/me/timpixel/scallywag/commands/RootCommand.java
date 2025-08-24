package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class RootCommand implements TabExecutor
{
    private final SubCommand[] subCommands;
    private final List<String> subCommandNames = new ArrayList<>();

    public RootCommand()
    {
        this.subCommands = getSubCommands();

        for (var subCommand : subCommands)
        {
            subCommandNames.add(subCommand.getName());
        }
    }

    protected abstract SubCommand[] getSubCommands();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args)
    {
        if (args.length == 0)
        {
            return CommandLogger.error(sender, "Specify a sub command");
        }

        for (var subCommand : subCommands)
        {
            if (subCommand.getName().equalsIgnoreCase(args[0]))
            {
                subCommand.onCommand(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }
        return CommandLogger.error(sender, "Command does not implement sub command " + args[0]);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args)
    {
        if (args.length == 1)
        {
            return subCommandNames;
        }
        else if (args.length > 1)
        {
            for (var subCommand : subCommands)
            {
                if (subCommand.getName().equalsIgnoreCase(args[0]))
                {
                    return subCommand.onTabComplete(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }
        return Collections.emptyList();
    }

}
