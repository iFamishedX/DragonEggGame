package dev.dragonslegacy;

import dev.dragonslegacy.api.DragonEggAPI;
import dev.dragonslegacy.config.AnnouncementsConfig;
import dev.dragonslegacy.config.Data;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MCIntegration {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
        LegacyComponentSerializer.legacySection();

    private static UUID BEARER;

    public static void init() {
        DragonEggAPI.onUpdate(MCIntegration::onUpdate);
    }

    public static void onUpdate(Data data) {
        if (data.playerUUID != null) {
            if (BEARER != null && !data.playerUUID.equals(BEARER)) announceChange(data.playerUUID);
            BEARER = data.playerUUID;
        } else BEARER = UUID.randomUUID();
    }

    public static void announceChange(UUID newBearer) {
        Optional.ofNullable(DragonsLegacyMod.server).ifPresent(server -> {
            ServerPlayer player = server.getPlayerList().getPlayer(newBearer);
            if (player == null) return;

            // Use the bearer_changed template from announcements.yaml
            Map<String, String> templates = DragonsLegacyMod.configManager.getAnnouncements().templates;
            String template = (templates != null && templates.containsKey("bearer_changed"))
                ? templates.get("bearer_changed")
                : AnnouncementsConfig.defaults().getOrDefault("bearer_changed", "");

            String playerName = player.getGameProfile().name();
            Component mcComponent;
            try {
                net.kyori.adventure.text.Component adventureComponent = MINI_MESSAGE.deserialize(
                    template, Placeholder.unparsed("player", playerName));
                mcComponent = Component.literal(LEGACY_SERIALIZER.serialize(adventureComponent));
            } catch (Exception e) {
                mcComponent = Component.literal(playerName + " now has the Dragon Egg!");
            }

            server.getPlayerList().broadcastSystemMessage(mcComponent, false);
            server.getPlayerList().broadcastAll(
                new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EXPERIENCE_ORB_PICKUP),
                    SoundSource.MASTER,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    .5f,
                    1f,
                    RandomSource.create().nextLong()
                )
            );
        });
    }
}

