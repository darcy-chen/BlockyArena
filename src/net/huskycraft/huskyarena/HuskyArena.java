package net.huskycraft.huskyarena;

import net.huskycraft.huskyarena.commands.CreateCmd;
import net.huskycraft.huskyarena.commands.DoneCmd;
import net.huskycraft.huskyarena.commands.SetSpawnCmd;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Plugin(id = "huskyarena", name = "HuskyArena")
public class HuskyArena {

    @Inject
    public Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    public Path getConfigDir() {
        return configDir;
    }

    private Path arenaDir;

    public Path getArenaDir() {
        return arenaDir;
    }

    public ArenaManager arenaManager;

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        createArenaDir();
        registerCommands();
        arenaManager = new ArenaManager(this);
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        arenaManager.initiateArena();
    }

    private void createArenaDir() {

        arenaDir = Paths.get(getConfigDir().toString() + "/arenas");
        try {
            if (!arenaDir.toFile().exists()) {
                Files.createDirectory(arenaDir);
            }
        } catch (IOException e) {
            logger.warn("Error creating arenas directory");
        }
    }

    private void registerCommands() {

        CommandSpec createCmd = CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("name")))
                .executor(new CreateCmd(this))
                .build();

        CommandSpec setSpawnCmd = CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))))
                .executor(new SetSpawnCmd(this))
                .build();

        CommandSpec doneCmd = CommandSpec.builder()
                .executor(new DoneCmd(this))
                .build();

        CommandSpec arenaCommandSpec = CommandSpec.builder()
                .child(createCmd, "create")
                .child(setSpawnCmd, "setspawn")
                .child(doneCmd, "done")
                .build();

        Sponge.getCommandManager().register(this, arenaCommandSpec, "huskyarena", "arena");
    }
}