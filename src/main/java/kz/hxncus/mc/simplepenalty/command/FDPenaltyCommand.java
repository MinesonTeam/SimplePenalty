package kz.hxncus.mc.simplepenalty.command;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import kz.hxncus.mc.simplepenalty.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FDPenaltyCommand extends AbstractCommand {
    public FDPenaltyCommand(SimplePenalty plugin) {
        super(plugin, "sp");
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String... args) {
        if (args.length == 0) {
            sendHelpMessage(sender, label);
        } else if ("выдать".equalsIgnoreCase(args[0])) {

        } else if ("список".equalsIgnoreCase(args[0])) {
            sendPenaltyList(sender, args);
        } else if ("оплатить".equalsIgnoreCase(args[0])) {

        } else if ("отменить".equalsIgnoreCase(args[0])) {

        } else {
            sendHelpMessage(sender, label);
        }
    }

    private void sendPenaltyList(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

        } else {
            Messages.MUST_BE_PLAYER.sendMessage(sender);
        }
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        if (sender.hasPermission("*") || sender.hasPermission("fdp.command.*")) {
            Messages.HELP_ADMIN.sendMessage(sender, label);
        } else {
            Messages.HELP.sendMessage(sender, label);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, Command command, String... args) {
        if (args.length == 0 && (sender.hasPermission("*") || sender.hasPermission("fdp.command.*"))) {
            return Arrays.asList("выдать", "список", "оплатить", "отменить");
        } else if (args.length == 0) {
            return Arrays.asList("оплатить", "список");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("выдать") || args[0].equalsIgnoreCase("оплатить")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
