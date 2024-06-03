package kz.hxncus.mc.simplepenalty.command;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import kz.hxncus.mc.simplepenalty.cache.PenaltyCache;
import kz.hxncus.mc.simplepenalty.cache.PlayerPenaltyCache;
import kz.hxncus.mc.simplepenalty.util.Messages;
import kz.hxncus.mc.simplepenalty.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimplePenaltyCommand extends AbstractCommand {
    public SimplePenaltyCommand(SimplePenalty plugin) {
        super(plugin, "sp");
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        if (sender.hasPermission("*") || sender.hasPermission("sp.command.*")) {
            Messages.HELP_ADMIN.sendMessages(sender, label);
        } else {
            Messages.HELP.sendMessages(sender, label);
        }
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String... args) {
        if (args.length == 0) {
            sendHelpMessage(sender, label);
        } else if ("выдать".equalsIgnoreCase(args[0])) {
            givePenalty(sender, label, args);
        } else if ("список".equalsIgnoreCase(args[0])) {
            preSendPenaltyList(sender, args);
        } else if ("оплатить".equalsIgnoreCase(args[0])) {
            payPenalty(sender, label, args);
        } else if ("отменить".equalsIgnoreCase(args[0])) {
            cancelPenalty(sender, label, args);
        } else if ("reload".equalsIgnoreCase(args[0])) {
            reloadPlugin(sender);
        } else {
            sendHelpMessage(sender, label);
        }
    }

    private void reloadPlugin(CommandSender sender) {
        plugin.onDisable();
        plugin.onEnable();
        for (Messages message : Messages.values()) {
            message.updateMessage();
        }
        Messages.PLUGIN_SUCCESSFULLY_RELOADED.sendMessage(sender);
    }

    private void givePenalty(CommandSender sender, String label, String... args) {
        if (args.length < 3 || !sender.hasPermission("*") && !sender.hasPermission("sp.command.give")) {
            sendHelpMessage(sender, label);
            return;
        }
        PlayerPenaltyCache penaltyCache;
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore()) {
                Messages.PLAYER_NOT_FOUND.sendMessages(sender);
                return;
            }
            penaltyCache = plugin.getCacheManager().unloadPlayerFromDatabase(offlinePlayer);
            penaltyCache.setTask(Bukkit.getScheduler().runTaskLater(plugin,
                    () -> plugin.getCacheManager().loadPlayerIntoDatabase(offlinePlayer.getUniqueId()), 6000L));
        } else {
            penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
        }
        if (!NumberUtil.isCreatable(args[2])) {
            return;
        }
        int amount = NumberUtil.createNumber(args[2]).intValue();
        String description = args.length > 3 ? args[3] : "";
        penaltyCache.getPenalties().add(new PenaltyCache(plugin.getCacheManager().getMaxId().getAndIncrement(),
                sender.getName(), args[1], description, amount, plugin.getConfig().getInt("penalty-expire-time", 604800) * 1000L + System.currentTimeMillis()));
    }

    private void preSendPenaltyList(CommandSender sender, String... args) {
        if (args.length > 1 && (sender.hasPermission("*") || sender.hasPermission("sp.command.list"))) {
            PlayerPenaltyCache penaltyCache;
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    Messages.PLAYER_NOT_FOUND.sendMessages(sender);
                    return;
                }
                penaltyCache = plugin.getCacheManager().unloadPlayerFromDatabase(offlinePlayer);
                penaltyCache.setTask(Bukkit.getScheduler().runTaskLater(plugin,
                        () -> plugin.getCacheManager().loadPlayerIntoDatabase(offlinePlayer.getUniqueId()), 6000L));
            } else {
                penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
            }
            sendPenaltyList(sender, penaltyCache.getPenalties());
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            List<PenaltyCache> penalties = plugin.getCacheManager()
                                                 .getPlayerPenaltyCache(player.getUniqueId())
                                                 .getPenalties();
            for (PenaltyCache penalty : penalties) {
                if (System.currentTimeMillis() >= penalty.getTime()) {
                    penalty.setCount((int) (penalty.getCount() * plugin.getConfig().getDouble("penalty-expire-multiplayer", 2.0)));
                    penalty.setTime(plugin.getConfig().getInt("penalty-expire-time", 604800) * 1000L + System.currentTimeMillis());
                    player.sendTitle(Messages.PENALTY_EXPIRED_TITLE.getMessage(0), Messages.PENALTY_EXPIRED_TITLE.getMessage(1),
                            plugin.getConfig().getInt("penalty-title-fadeIn", 10), plugin.getConfig().getInt("penalty-title-stay", 70),
                            plugin.getConfig().getInt("penalty-title-fadeOut", 20));
                }
            }
            sendPenaltyList(sender, penalties);
        } else {
            Messages.MUST_BE_PLAYER.sendMessages(sender);
        }
    }

    private void sendPenaltyList(CommandSender sender, List<PenaltyCache> penalties) {
        Messages.PENALTY_LIST_HEADER.sendMessage(sender);
        for (PenaltyCache penalty : penalties) {
            Duration duration = Duration.ofMillis(penalty.getTime() - System.currentTimeMillis());
            sender.sendMessage(Messages.PENALTY_INFORMATION.getMessage(0).replace("{0}", String.valueOf(penalty.getId()))
                .replace("{1}", penalty.getOffender()).replace("{2}", penalty.getOfficer()).replace("{3}", String.valueOf(penalty.getCount()))
                .replace("{4}", penalty.getDescription()).replace("{5}", String.valueOf(duration.toDays()))
                .replace("{6}", String.valueOf(duration.toHours())).replace("{7}", String.valueOf(duration.toMinutes()))
                    .replace("{8}", String.valueOf(duration.getSeconds() % 60)));
        }
    }

    private void payPenalty(CommandSender sender, String label, String... args) {
        if (sender instanceof Player) {
            if (args.length < 3) {
                sendHelpMessage(sender, label);
                return;
            }
            if (!NumberUtil.isCreatable(args[1]) || !NumberUtil.isCreatable(args[2])) {
                return;
            }
            int number = NumberUtil.createNumber(args[1]).intValue();
            int amount = NumberUtil.createNumber(args[2]).intValue();
            Player player = (Player) sender;
            PlayerPenaltyCache penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
            for (PenaltyCache penalty : penaltyCache.getPenalties()) {
                if (penalty.getId() != number) {
                    continue;
                }
                int balance = 0;
                for (ItemStack itemStack : player.getInventory()) {
                    if (itemStack == null) {
                        continue;
                    }
                    if (itemStack.getType() == Material.DIAMOND) {
                        balance += itemStack.getAmount();
                    }
                }
                int count = penalty.getCount();
                if (amount > count) {
                    Messages.PENALTY_COST_LESS.sendMessages(sender);
                } else if (balance >= amount) {
                    penalty.setCount(count - amount);
                    for (ItemStack item : player.getInventory()) {
                        if (item != null && item.getType() == Material.DIAMOND) {
                            if (amount > item.getAmount()) {
                                amount -= item.getAmount();
                                item.setAmount(0);
                            } else {
                                item.setAmount(item.getAmount() - amount);
                                break;
                            }
                        }
                    }
                    Messages.PENALTY_SUCCESSFULLY_PAID.sendMessages(sender, penalty.getId(), penalty.getCount());
                } else {
                    Messages.NOT_ENOUGH_MONEY.sendMessages(sender);
                }
                return;
            }
            Messages.PENALTY_NOT_FOUND.sendMessages(sender);
        } else {
            Messages.MUST_BE_PLAYER.sendMessages(sender);
        }
    }

    private void cancelPenalty(CommandSender sender, String label, String... args) {
        if (args.length < 3 || !sender.hasPermission("*") && !sender.hasPermission("sp.command.give")) {
            sendHelpMessage(sender, label);
            return;
        }
        PlayerPenaltyCache penaltyCache;
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore()) {
                Messages.PLAYER_NOT_FOUND.sendMessages(sender);
                return;
            }
            penaltyCache = plugin.getCacheManager().unloadPlayerFromDatabase(offlinePlayer);
            penaltyCache.setTask(Bukkit.getScheduler().runTaskLater(plugin,
                    () -> plugin.getCacheManager().loadPlayerIntoDatabase(offlinePlayer.getUniqueId()), 6000L));
        } else {
            penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
        }
        if (!NumberUtil.isCreatable(args[2])) {
            return;
        }
        int id = NumberUtil.createNumber(args[2]).intValue();
        for (PenaltyCache penalty : penaltyCache.getPenalties()) {
            if (penalty.getId() == id) {
                penalty.setCount(0);
                Messages.PENALTY_SUCCESSFULLY_CANCELED.sendMessages(sender);
                return;
            }
        }
        Messages.PENALTY_NOT_FOUND.sendMessages(sender);
    }

    @Override
    public List<String> complete(CommandSender sender, Command command, String... args) {
        if (sender.hasPermission("*") || sender.hasPermission("fpd.command.*")) {
            if (args.length == 1) {
                return Arrays.asList("выдать", "список", "оплатить", "отменить", "reload");
            } else if (args.length == 2) {
                if ("выдать".equalsIgnoreCase(args[0]) || "список".equalsIgnoreCase(args[0])) {
                    return Bukkit.getOnlinePlayers()
                                 .stream()
                                 .map(Player::getName)
                                 .collect(Collectors.toList());
                }
            }
        } else {
            if (args.length == 1) {
                return Arrays.asList("оплатить", "список");
            }
        }
        return Collections.emptyList();
    }
}
