package me.timpixel.commands;

import org.bukkit.command.TabExecutor;

public interface SubCommand extends TabExecutor
{
    String getName();
}
