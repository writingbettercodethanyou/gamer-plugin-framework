package com.github.writingbettercodethanyou.gamerpluginframework;

import com.github.writingbettercodethanyou.gamerpluginframework.command.CommandName;
import com.github.writingbettercodethanyou.gamerpluginframework.message.MessageService;
import com.github.writingbettercodethanyou.gamerpluginframework.message.YamlMessageService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    protected void registerCommands() throws URISyntaxException, IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        ZipFile zip = new ZipFile(file);

        try {
            for (Enumeration<? extends ZipEntry> list = zip.entries(); list.hasMoreElements(); ) {
                ZipEntry entry = list.nextElement();
                String name = entry.getName();

                if (!name.endsWith(".class"))
                    continue;

                Class<?> classs = Class.forName(name.substring(0, name.lastIndexOf(".")).replace("/", "."));
                CommandName commandAnnotation = classs.getAnnotation(CommandName.class);
                if (commandAnnotation == null) {
                    continue;
                }

                if (!CommandExecutor.class.isAssignableFrom(classs)) {
                    throw new IllegalArgumentException("@Command annotation can only go over classes implementing the CommandExecutor");
                }

                registerCommandExecutor(commandAnnotation.value(), (CommandExecutor) classs.getConstructor().newInstance());
            }
        } finally {
            zip.close();
        }
    }
}
