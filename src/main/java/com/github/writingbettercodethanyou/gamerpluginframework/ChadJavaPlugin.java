package com.github.writingbettercodethanyou.gamerpluginframework;

import com.github.writingbettercodethanyou.gamerpluginframework.command.RegisterForCommand;
import com.github.writingbettercodethanyou.gamerpluginframework.inject.ServiceRegistry;
import com.github.writingbettercodethanyou.gamerpluginframework.message.MessageService;
import com.github.writingbettercodethanyou.gamerpluginframework.message.YamlMessageService;
import com.github.writingbettercodethanyou.gamerpluginframework.util.ScannotationUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class ChadJavaPlugin extends JavaPlugin {

    private ServiceRegistry serviceRegistry;

    @Override
    public void onEnable() {
        ServiceRegistry.Builder builder = new ServiceRegistry.Builder();
        onServiceSetup(builder);
        serviceRegistry = builder.build();
    }

    protected void onServiceSetup(ServiceRegistry.Builder services) {
    }

    protected ServiceRegistry getServiceRegistry() {
        if (serviceRegistry == null)
            throw new NullPointerException("service registry has not been instantiated because super.onEnable() was not called before trying to access the service registry");
        return serviceRegistry;
    }

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

    protected void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }


    protected void registerListener(Class<? extends Listener> listenerClass) {
        getServer().getPluginManager().registerEvents(serviceRegistry.getInstance(listenerClass), this);
    }

    private PluginCommand ensureCommandExists(String commandName) {
        PluginCommand command = this.getCommand(commandName);
        if (command == null)
            throw new NullPointerException(String.format("\"%s\" is not registered in the plugin.yml", commandName));
        return command;
    }

    protected boolean registerCommandExecutor(String commandName, CommandExecutor commandExecutor) {
        return registerCommandExecutor(commandName, commandExecutor, false);
    }

    protected boolean registerCommandExecutor(String commandName, CommandExecutor commandExecutor, boolean overrideExistingExecutor) {
        PluginCommand command = ensureCommandExists(commandName);

        if (!overrideExistingExecutor && command.getExecutor() == this)
            return false;

        command.setExecutor(commandExecutor);
        return true;
    }

    protected boolean registerTabCompleter(String commandName, TabCompleter commandExecutor) {
        return registerTabCompleter(commandName, commandExecutor, false);
    }

    protected boolean registerTabCompleter(String commandName, TabCompleter commandExecutor, boolean overrideExistingCompleter) {
        PluginCommand command = ensureCommandExists(commandName);

        if (!overrideExistingCompleter && command.getTabCompleter() == null)
            return false;

        command.setTabCompleter(commandExecutor);
        return true;
    }

    protected void registerCommands() {
        ScannotationUtil.findClassesWithAnnotation(getClass(), RegisterForCommand.class).forEach((classs) -> {
            boolean isCommandExecutor = CommandExecutor.class.isAssignableFrom(classs);
            boolean isTabCompleter = TabCompleter.class.isAssignableFrom(classs);
            if (!(isCommandExecutor || isTabCompleter))
                throw new IllegalArgumentException("@RegisterForCommand annotation can only go over classes implementing the CommandExecutor or the TabCompleter");

            RegisterForCommand commandAnnotation = classs.getAnnotation(RegisterForCommand.class);

            Object instance = serviceRegistry.getInstance(classs);
            if (isCommandExecutor)
                registerCommandExecutor(commandAnnotation.value(), (CommandExecutor) instance);
            if (isTabCompleter)
                registerTabCompleter(commandAnnotation.value(), (TabCompleter) instance);
        });
    }
}
