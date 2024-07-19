package kz.hxncus.mc.simplepenalty.util;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum Messages {
    HELP, HELP_ADMIN, MUST_BE_PLAYER, PREFIX, PLAYER_NOT_FOUND, PENALTY_SUCCESSFULLY_PAID, PENALTY_EXPIRED_TITLE, PENALTY_LIST_HEADER, PENALTY_INFORMATION,
    NOT_ENOUGH_MONEY, PENALTY_NOT_FOUND, PENALTY_SUCCESSFULLY_CANCELED, PENALTY_COST_LESS, PLUGIN_SUCCESSFULLY_RELOADED, OFFICER_HAS_FULL_INVENTORY,
    OFFICER_IS_OFFLINE, AMOUNT_IS_SMALL, OFFICER_NOT_FOUND, CANT_GIVE_PENALTY_YOURSELF, PENALTY_SUCCESSFULLY_RECEIVED, PENALTY_SUCCESSFULLY_IMPOSED,
    PENALTY_SUCCESSFULLY_GOT, PENALTY_LIST_EMPTY, TEST_MESSAGE;

    private List<String> messageList;
    private final FileConfiguration langConfig = SimplePenalty.getInstance().getFileManager().getLangConfig();

    Messages() {
        updateMessage();
    }

    public void updateMessage() {
        Object val = langConfig.get(name().toLowerCase(Locale.ROOT), "");
        if (val instanceof List<?>) {
            messageList = ((List<?>) val).stream().map(Object::toString).collect(Collectors.toList());
        } else {
            messageList = Collections.singletonList(val.toString());
        }
        if (StringUtil.isEmpty(messageList.get(0))) {
            SimplePenalty.getInstance().getLogger().severe(() -> "Message not found: " + name().toLowerCase(Locale.ROOT));
        }
    }

    public String getMessage(int index, Object... args) {
        return messageList.get(index) == null ? "" : getReplacedMessage(messageList.get(index), args);
    }

    public String getReplacedMessage(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i].toString());
        }
        return message.replace("{PREFIX}", PREFIX.messageList.get(0));
    }

    public void sendMessage(CommandSender sender, Object... args) {
        for (String message : messageList) {
            sender.sendMessage(getReplacedMessage(message, args));
        }
    }

    public static void updateAllMessages() {
        for (Messages messages : values()) {
            messages.updateMessage();
        }
    }
}