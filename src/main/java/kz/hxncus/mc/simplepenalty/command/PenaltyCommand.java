package kz.hxncus.mc.simplepenalty.command;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import kz.hxncus.mc.simplepenalty.cache.PenaltyCache;
import kz.hxncus.mc.simplepenalty.cache.PlayerPenaltyCache;
import kz.hxncus.mc.simplepenalty.util.Messages;
import kz.hxncus.mc.simplepenalty.util.NumberUtil;
import kz.hxncus.mc.simplepenalty.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PenaltyCommand extends AbstractCommand {
    public PenaltyCommand(SimplePenalty plugin) {
        super(plugin, "sp");
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        if (sender.hasPermission("simplepenalty.command.*")) {
            Messages.HELP_ADMIN.sendMessage(sender, label);
        } else {
            Messages.HELP.sendMessage(sender, label);
        }
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String... args) {
        if (args.length == 0) {
            sendHelpMessage(sender, label);
        } else if ("impose".equalsIgnoreCase(args[0])) {
            imposePenalty(sender, label, args);
        } else if ("list".equalsIgnoreCase(args[0])) {
            preSendPenaltyList(sender, args);
        } else if ("pay".equalsIgnoreCase(args[0])) {
            payPenalty(sender, label, args);
        } else if ("cancel".equalsIgnoreCase(args[0])) {
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

    private void imposePenalty(CommandSender sender, String label, String... args) {
        if (args.length < 4 || !sender.hasPermission("simplepenalty.command.give")) {
            sendHelpMessage(sender, label);
            return;
        }
        if (args[1].equalsIgnoreCase(sender.getName()) || args[1].equalsIgnoreCase(args[2])) {
            Messages.CANT_GIVE_PENALTY_YOURSELF.sendMessage(sender);
            return;
        }
        if (Bukkit.getPlayer(args[2]) == null && !Bukkit.getOfflinePlayer(args[2]).hasPlayedBefore()) {
            Messages.OFFICER_NOT_FOUND.sendMessage(sender);
            return;
        }
        if (!NumberUtil.isCreatable(args[3])) {
            return;
        }
        PlayerPenaltyCache penaltyCache;
        Player player = Bukkit.getPlayer(args[1]);
        int amount = NumberUtil.createNumber(args[3]).intValue();
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore()) {
                Messages.PLAYER_NOT_FOUND.sendMessage(sender);
                return;
            }
            if (plugin.getCacheManager().isPlayerHasPenaltyCache(offlinePlayer.getUniqueId())) {
                penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(offlinePlayer.getUniqueId());
            } else {
                penaltyCache = plugin.getCacheManager().unloadPlayerFromDatabase(offlinePlayer);
                penaltyCache.setTask(Bukkit.getScheduler().runTaskLater(plugin,
                        () -> plugin.getCacheManager().loadPlayerIntoDatabase(offlinePlayer.getUniqueId()), 6000L));
            }
        } else {
            penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
            Messages.PENALTY_SUCCESSFULLY_RECEIVED.sendMessage(player, args[2], amount);
        }
        penaltyCache.getPenalties().add(new PenaltyCache(plugin.getCacheManager().getMaxId().getAndIncrement(),
                args[2], args[1], args.length > 4 ? StringUtil.join(Arrays.asList(args).subList(4, args.length), " ") : "", amount, plugin.getConfig().getInt("penalty-expire-time", 604800) * 1000L + System.currentTimeMillis()));
        Messages.PENALTY_SUCCESSFULLY_IMPOSED.sendMessage(sender, args[1]);
    }

    private void preSendPenaltyList(CommandSender sender, String... args) {
        if (args.length > 1 && sender.hasPermission("simplepenalty.command.list")) {
            PlayerPenaltyCache penaltyCache;
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    Messages.PLAYER_NOT_FOUND.sendMessage(sender);
                    return;
                }
                if (plugin.getCacheManager().getPlayerPenaltyCacheMap().containsKey(offlinePlayer.getUniqueId())) {
                    penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(offlinePlayer.getUniqueId());
                } else {
                    penaltyCache = plugin.getCacheManager()
                         .unloadPlayerFromDatabase(offlinePlayer);
                    penaltyCache.setTask(Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getCacheManager()
                        .loadPlayerIntoDatabase(offlinePlayer.getUniqueId()), 6000L));
                }
            } else {
                penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
            }
            sendPenaltyList(sender, penaltyCache.getPenalties());
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            List<PenaltyCache> penalties = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId()).getPenalties();
            sendPenaltyList(sender, penalties);
        } else {
            Messages.MUST_BE_PLAYER.sendMessage(sender);
        }
    }

    private void sendPenaltyList(CommandSender sender, List<PenaltyCache> penalties) {
        if (penalties.isEmpty()) {
            Messages.PENALTY_LIST_EMPTY.sendMessage(sender);
            return;
        }
        Messages.PENALTY_LIST_HEADER.sendMessage(sender);
        for (PenaltyCache penalty : penalties) {
            // If the penalty counts smaller than 1 (0, -1...), continue.
            if (penalty.getCount() < 1) {
                continue;
            }
            Messages.PENALTY_INFORMATION.sendMessage(sender, penalty.getId(), penalty.getOffender(), penalty.getOfficer(), penalty.getCount(), penalty.getDescription(), LocalDateTime.ofInstant(
                Instant.ofEpochMilli(penalty.getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(plugin.getConfig().getString("penalty-list-date-format", "dd-MM-yyyy HH:mm:ss"))));
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
            if (amount < 1) {
                Messages.AMOUNT_IS_SMALL.sendMessage(sender);
                return;
            }
            Player player = (Player) sender;
            PlayerPenaltyCache penaltyCache = plugin.getCacheManager().getPlayerPenaltyCache(player.getUniqueId());
            for (PenaltyCache penalty : penaltyCache.getPenalties()) {
                if (penalty.getId() != number) {
                    continue;
                }
                Player officer = Bukkit.getPlayer(penalty.getOfficer());
                if (officer == null) {
                    Messages.OFFICER_IS_OFFLINE.sendMessage(player);
                    return;
                }
                if (officer.getInventory().firstEmpty() == -1) {
                    Messages.OFFICER_HAS_FULL_INVENTORY.sendMessage(player);
                    return;
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
                if (amount > penalty.getCount()) {
                    Messages.PENALTY_COST_LESS.sendMessage(sender);
                } else if (balance >= amount) {
                    int paid = 0;
                    for (ItemStack item : player.getInventory()) {
                        if (item != null && item.getType() == Material.DIAMOND) {
                            if (officer.getInventory().firstEmpty() == -1) {
                                Messages.OFFICER_HAS_FULL_INVENTORY.sendMessage(player);
                                break;
                            }
                            if (amount > item.getAmount()) {
                                paid += item.getAmount();
                                officer.getInventory().addItem(item.clone());
                                amount -= item.getAmount();
                                penalty.setCount(penalty.getCount() - item.getAmount());
                                item.setAmount(0);
                            } else {
                                paid += amount;
                                officer.getInventory().addItem(new ItemStack(item.getType(), amount));
                                penalty.setCount(penalty.getCount() - amount);
                                item.setAmount(item.getAmount() - amount);
                                break;
                            }
                        }
                    }
                    Messages.PENALTY_SUCCESSFULLY_GOT.sendMessage(officer, sender.getName(), paid);
                    Messages.PENALTY_SUCCESSFULLY_PAID.sendMessage(sender, penalty.getId(), paid);
                } else {
                    Messages.NOT_ENOUGH_MONEY.sendMessage(sender);
                }
                return;
            }
            Messages.PENALTY_NOT_FOUND.sendMessage(sender);
        } else {
            Messages.MUST_BE_PLAYER.sendMessage(sender);
        }
    }

    private void cancelPenalty(CommandSender sender, String label, String... args) {
        if (args.length < 3 || !sender.hasPermission("sp.command.give")) {
            sendHelpMessage(sender, label);
            return;
        }
        PlayerPenaltyCache penaltyCache;
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore()) {
                Messages.PLAYER_NOT_FOUND.sendMessage(sender);
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
                Messages.PENALTY_SUCCESSFULLY_CANCELED.sendMessage(sender);
                return;
            }
        }
        Messages.PENALTY_NOT_FOUND.sendMessage(sender);
    }

    @Override
    public List<String> complete(CommandSender sender, Command command, String... args) {
        if (sender.hasPermission("fpd.command.*")) {
            if (args.length == 1) {
                return Arrays.asList("impose", "list", "pay", "cancel", "reload");
            } else if (args.length == 2 || args.length == 3) {
                if ("impose".equalsIgnoreCase(args[0]) || "list".equalsIgnoreCase(args[0]) || "cancel".equalsIgnoreCase(args[0])) {
                    return Bukkit.getOnlinePlayers()
                                 .stream()
                                 .map(Player::getName)
                                 .collect(Collectors.toList());
                }
            }
        } else {
            if (args.length == 1) {
                return Arrays.asList("pay", "list");
            }
        }
        return Collections.emptyList();
    }
}
