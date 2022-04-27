package com.github.writingbettercodethanyou.gamerpluginframework.message;

import org.bukkit.command.CommandSender;

import java.util.Map;

public interface MessageService {

    void loadMessages();

    void sendMessage(CommandSender sender, String key);

    void sendMessage(CommandSender receiver, String key, Map<String, Object> replacements);
}
