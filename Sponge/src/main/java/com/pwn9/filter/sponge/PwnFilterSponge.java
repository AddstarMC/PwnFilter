package com.pwn9.filter.sponge;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.pwn9.filter.ProjectTemplateData;
import com.pwn9.filter.PwnFilterPlugin;
import com.pwn9.filter.engine.FilterService;
import com.pwn9.filter.engine.FilterServiceImpl;
import com.pwn9.filter.engine.api.Console;
import com.pwn9.filter.minecraft.MinecraftConsole;
import com.pwn9.filter.minecraft.api.MinecraftAPI;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Plugin(
      id = ProjectTemplateData.ID,
      name = ProjectTemplateData.ID,
      version = ProjectTemplateData.VERSION,
      url = ProjectTemplateData.URL
)
public class PwnFilterSponge implements PwnFilterPlugin {
    private final Game game;
    private final PluginContainer container;
    private final SpongeAudiences audience;
    private Logger logger;
    private FilterService filterService;
    private Console console;
    private SpongeApi api;
    public static final ConcurrentMap<UUID, String> lastMessage = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();


    @Inject
    public PwnFilterSponge(final @NonNull Logger logger, final @NonNull Game game, final @NonNull PluginContainer container, final @NonNull SpongeAudiences audience) {
        this.logger = logger;
        this.game = game;
        this.container = container;
        this.audience = audience;
        filterService = new FilterServiceImpl();
        console = new MinecraftConsole(this.getApi());
        api = new SpongeApi(this);
    }

    @Override
    public FilterService getFilterService() {
        return filterService;
    }

    @Override
    public Console getConsole() {
        return console;
    }

    public java.util.logging.Logger getLogger() {
        return java.util.logging.Logger.getLogger(ProjectTemplateData.ID);
    }

    @Override
    public MinecraftAPI getApi() {
        return api;
    }

    @Override
    public boolean configurePlugin() {
        return false;
    }

    @Override
    public boolean checkRecentMessage(UUID uuid, String message) {
        return (lastMessage.containsKey(uuid) && lastMessage.get(uuid).equals(message));
    }

    @Override
    public void addRecentMessage(UUID uuid, String string) {
        lastMessage.put(uuid,string);
    }

    public Game getGame() {
        return game;
    }

    public PluginContainer getContainer() {
        return container;
    }

    public SpongeAudiences getAudience() {
        return audience;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }
}
