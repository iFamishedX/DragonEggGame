package dev.dragonslegacy.ability;

import dev.dragonslegacy.DragonsLegacyMod;
import dev.dragonslegacy.config.AttributeEntry;
import dev.dragonslegacy.config.ConfigAttributeParser;
import dev.dragonslegacy.config.ConfigEffectParser;
import dev.dragonslegacy.config.EffectEntry;
import dev.dragonslegacy.config.PassiveEffectsConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

/**
 * Applies and removes the passive status-effects and attribute modifiers that the
 * Dragon Egg bearer receives simply by holding the egg (Dragon's Hunger not required).
 *
 * <p>Effects and attributes are loaded from
 * {@code config/dragonslegacy/passive_effects.yaml} via {@link PassiveEffectsConfig}.
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *   <li>Call {@link #applyToBearer(ServerPlayer)} when a player becomes the bearer.</li>
 *   <li>Call {@link #removeFromPlayer(ServerPlayer)} when a player loses the bearer role.</li>
 * </ul>
 */
public class PassiveEffectsEngine {

    // -------------------------------------------------------------------------
    // Apply
    // -------------------------------------------------------------------------

    /**
     * Applies all passive effects and attribute modifiers from the current config
     * to {@code player}.  Safe to call multiple times – existing modifiers are
     * removed before re-adding to prevent stacking.
     *
     * @param player the new bearer
     */
    public void applyToBearer(ServerPlayer player) {
        PassiveEffectsConfig config = DragonsLegacyMod.configManager.getPassiveEffects();
        applyEffects(player, config.effects);
        applyAttributes(player, config.attributes);
        DragonsLegacyMod.LOGGER.debug(
            "[Dragon's Legacy] Passive effects applied to {}.", player.getGameProfile().name());
    }

    // -------------------------------------------------------------------------
    // Remove
    // -------------------------------------------------------------------------

    /**
     * Removes all passive effects and attribute modifiers (as defined by the current
     * config) from {@code player}.
     *
     * @param player the former bearer
     */
    public void removeFromPlayer(ServerPlayer player) {
        PassiveEffectsConfig config = DragonsLegacyMod.configManager.getPassiveEffects();
        removeEffects(player, config.effects);
        removeAttributes(player, config.attributes);
        // Clamp HP if max health was reduced
        float maxHp = player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
        DragonsLegacyMod.LOGGER.debug(
            "[Dragon's Legacy] Passive effects removed from {}.", player.getGameProfile().name());
    }

    // -------------------------------------------------------------------------
    // Private helpers – apply
    // -------------------------------------------------------------------------

    private static void applyEffects(ServerPlayer player, List<EffectEntry> entries) {
        for (EffectEntry entry : entries) {
            Holder<MobEffect> effect = ConfigEffectParser.parseEffect(entry);
            if (effect == null) continue;
            player.addEffect(ConfigEffectParser.createInstance(effect, entry, ConfigEffectParser.PASSIVE_DURATION));
        }
    }

    private static void applyAttributes(ServerPlayer player, List<AttributeEntry> entries) {
        for (AttributeEntry entry : entries) {
            Holder<Attribute> attr = ConfigAttributeParser.parseAttribute(entry);
            Identifier modId = ConfigAttributeParser.getModifierIdentifier(entry);
            if (attr == null || modId == null) continue;

            AttributeInstance instance = player.getAttribute(attr);
            if (instance == null) {
                DragonsLegacyMod.LOGGER.warn(
                    "[Dragon's Legacy] Player does not have attribute '{}' – skipping.", entry.id);
                continue;
            }
            // Idempotent: remove first so we never stack
            instance.removeModifier(modId);
            instance.addTransientModifier(new AttributeModifier(
                modId, entry.amount, ConfigAttributeParser.parseOperation(entry)));
        }
        // Clamp HP to the new max (safety guard for edge cases)
        float maxHp = player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers – remove
    // -------------------------------------------------------------------------

    private static void removeEffects(ServerPlayer player, List<EffectEntry> entries) {
        for (EffectEntry entry : entries) {
            Holder<MobEffect> effect = ConfigEffectParser.parseEffect(entry);
            if (effect == null) continue;
            player.removeEffect(effect);
        }
    }

    private static void removeAttributes(ServerPlayer player, List<AttributeEntry> entries) {
        for (AttributeEntry entry : entries) {
            Holder<Attribute> attr = ConfigAttributeParser.parseAttribute(entry);
            Identifier modId = ConfigAttributeParser.getModifierIdentifier(entry);
            if (attr == null || modId == null) continue;

            AttributeInstance instance = player.getAttribute(attr);
            if (instance != null) {
                instance.removeModifier(modId);
            }
        }
    }
}
