package com.github.writingbettercodethanyou.gamerpluginframework;

import com.github.writingbettercodethanyou.gamerpluginframework.message.MessageService;
import com.github.writingbettercodethanyou.gamerpluginframework.message.YamlMessageService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class ChadJavaPlugin extends JavaPlugin {

    protected MessageService createYamlMessageService(String messageYamlName) {
        File messagesFile = new File(getDataFolder(), messageYamlName);
        if (!messagesFile.exists())
            saveResource(messageYamlName, false);

        YamlConfiguration messageDefaults;
        InputStream messageDefaultsStream = getResource(messageYamlName);
        if (messageDefaultsStream == null)
            messageDefaults = null;
        else
            messageDefaults = YamlConfiguration.loadConfiguration(new InputStreamReader(messageDefaultsStream));

        MessageService messageService = new YamlMessageService(messagesFile, messageDefaults);
        messageService.loadMessages();
        return messageService;
    }

    protected void registerCommandExecutor(String commandName, CommandExecutor commandExecutor) {
        PluginCommand command = this.getCommand(commandName);
        if (command == null)
            throw new NullPointerException(String.format("\"%s\" is not registered in the plugin.yml", commandName));
        command.setExecutor(commandExecutor);
    }
}
