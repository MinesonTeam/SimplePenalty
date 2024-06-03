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
    NOT_ENOUGH_MONEY, PENALTY_NOT_FOUND, PENALTY_SUCCESSFULLY_CANCELED, PENALTY_COST_LESS, PLUGIN_SUCCESSFULLY_RELOADED;

    private List<String> messageList;
    private final FileConfiguration langConfig = SimplePenalty.getInstance()
                                                              .getLangManager()
                                                              .getLangConfig();

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
            throw new RuntimeException("Message not found: " + name().toLowerCase(Locale.ROOT));
        }
    }

    public String getMessage(int index, Object... args) {
        return messageList.get(index) == null ? "" : getFormattedMessage(messageList.get(index), args);
    }

    public String getMessage(String message) {
        return message.replace("{PREFIX}", PREFIX.messageList.get(0));
    }

    public String getFormattedMessage(String message, Object... args) {
        return String.format(getMessage(message), args);
    }

    public void sendMessage(CommandSender sender) {
        sender.sendMessage(getFormattedMessage(getMessage(0)));
    }

    public void sendMessages(CommandSender sender, Object... args) {
        for (String message : messageList) {
            sender.sendMessage(getFormattedMessage(message, args));
        }
    }

    public static void updateAllMessages() {
        for (Messages messages : values()) {
            messages.updateMessage();
        }
    }
}