package com.pwn9.filter.sponge;

import com.pwn9.filter.minecraft.api.MinecraftAPI;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/08/2020.
 */
public class SpongeApi implements MinecraftAPI {
    private EconomyService economyService;
    private final PwnFilterSponge plugin ;
    private boolean globalMute = false;


    public SpongeApi(PwnFilterSponge plugin) {
        this.plugin = plugin;
        Optional<EconomyService> serviceOpt = Sponge.getServiceManager().provide(EconomyService.class);
        serviceOpt.ifPresent(service-> economyService = service);
    }

    @Override
    public void reset() {
        //not required by this install as we dont cache players.
    }

    @Override
    public boolean burn(UUID uuid, int duration, String messageString) {
        return false;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Sponge.getServer().getPlayer(uuid).ifPresent(player -> player.sendMessage(Text.of(message)));
    }

    @Override
    public void sendMessages(UUID uuid, List<String> messages) {
        Sponge.getServer().getPlayer(uuid).ifPresent(player -> messages.forEach(message->{
            Text text = Text.of(message);
            player.sendMessage(text);
        }));
    }

    @Override
    public void executePlayerCommand(UUID uuid, String command) {
        Sponge.getServer().getPlayer(uuid)
              .ifPresent(player -> Sponge.getCommandManager().process(player,command));
    }

    @Override
    public boolean withdrawMoney(UUID uuid, Double amount, String messageString) {
        return economyService.getOrCreateAccount(uuid).map(uniqueAccount ->
              uniqueAccount.withdraw(economyService.getDefaultCurrency(),  new BigDecimal(amount), Cause.builder()
                    .build(EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getContainer())
                          .build()))
                    .getResult()).orElse(ResultType.FAILED)
              .compareTo(ResultType.SUCCESS) == 0;
    }

    @Override
    public void kick(UUID uuid, String messageString) {
        Sponge.getServer().getPlayer(uuid)
              .ifPresent(player -> player.kick(Text.of(messageString)));
    }

    @Override
    public void kill(UUID uuid, String messageString) {
        Sponge.getServer().getPlayer(uuid)
              .ifPresent(player -> player.offer(Keys.HEALTH,0D)
                    .ifSuccessful(immutableValues -> player.sendMessage(Text.of(messageString))));
    }

    @Override
    public String getPlayerWorldName(UUID uuid) {
        final Collection<World> worlds = Sponge.getServer().getWorlds();
        Optional<Player> user = Sponge.getServer().getPlayer(uuid);
        if(user.isPresent()){
            Optional<String> name =  user.map(user1 -> {
                Optional<UUID> worldUUID = user1.getWorldUniqueId();
                return worldUUID.flatMap(value -> worlds.stream().filter(world -> world.getUniqueId() == value).findFirst()).orElse(null);
            }).flatMap(world -> Optional.of(world.getName()));
            return name.orElse(null);
        }
        return null;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return Sponge.getServer().getPlayer(uuid).flatMap(player -> Optional.of(player.getName())).orElse(null);
    }

    @Override
    public @Nullable Boolean playerIdHasPermission(UUID u, String s) {
        return Sponge.getServer()
              .getPlayer(u)
              .flatMap(player -> Optional.of(player.hasPermission(s))).orElse(Boolean.FALSE);
    }

    @Override
    public void sendConsoleMessage(String message) {
        Sponge.getServer().getConsole().sendMessage(Text.of(message));
    }

    @Override
    public void sendConsoleMessage(TextComponent message) {
        plugin.getAudience().console().sendMessage(message);
    }

    @Override
    public void sendConsoleMessages(List<String> messageList) {
        messageList.forEach(this::sendConsoleMessage);
    }

    @Override
    public void sendBroadcast(String message) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(message), ChatTypes.CHAT);
    }

    @Override
    public void sendBroadCast(TextComponent component) {
        plugin.getAudience().all().sendMessage(component);
    }

    @Override
    public void sendBroadcast(List<String> messageList) {
        messageList.forEach(this::sendBroadcast);
    }

    @Override
    public void executeCommand(String command) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }

    @Override
    public boolean globalMute() {
        return this.globalMute;
    }

    @Override
    public void setMutStatus(boolean status) {
        this.globalMute = status;
    }

    @Override
    public AudienceProvider audiences() {
        return plugin.getAudience();
    }
}
