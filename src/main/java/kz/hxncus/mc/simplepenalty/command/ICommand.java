package kz.hxncus.mc.simplepenalty.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public interface ICommand extends CommandExecutor, TabExecutor  {
    void execute(CommandSender sender, Command command, String label, String... args);
    List<String> complete(CommandSender sender, Command command, String... args);
}
