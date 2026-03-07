package dev.dragonslegacy.ability;

import dev.dragonslegacy.DragonsLegacyMod;
import dev.dragonslegacy.config.AbilityConfig;
import dev.dragonslegacy.config.AttributeEntry;
import dev.dragonslegacy.config.ConfigAttributeParser;
import dev.dragonslegacy.config.ConfigEffectParser;
import dev.dragonslegacy.config.EffectEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Static utility class that applies and removes all effects associated with the
 * Dragon's Hunger ability.
 *
 * <p>Effects and attributes are loaded dynamically from
 * {@code config/dragonslegacy/ability.yaml} via {@link AbilityConfig}.
 *
 * <h3>Effects applied (configurable)</h3>
 * See {@code config/dragonslegacy/ability.yaml} for the current list.
 *
 * <h3>Additional hardcoded behaviour</h3>
 * <ul>
 *   <li>Curse of Binding is added to / removed from the dragon head helmet.</li>
 * </ul>
 */
public final class DragonHungerAbility {

    private DragonHungerAbility() {}

    // -------------------------------------------------------------------------
    // Apply
    // -------------------------------------------------------------------------

    /**
     * Applies all Dragon's Hunger effects (from config) to {@code player}, plus
     * the Binding Curse on the dragon head.
     *
     * @param player the bearer to buff
     */
    public static void apply(ServerPlayer player) {
        AbilityConfig config = DragonsLegacyMod.configManager.getAbility();
        applyEffects(player, config.effects, config.durationTicks + 20);
        applyAttributes(player, config.attributes);
        applyBindingCurse(player);
    }

    // -------------------------------------------------------------------------
    // Remove
    // -------------------------------------------------------------------------

    /**
     * Removes all Dragon's Hunger effects (from config) from {@code player}, plus
     * the Binding Curse on the dragon head.
     *
     * @param player the bearer to de-buff
     */
    public static void remove(ServerPlayer player) {
        AbilityConfig config = DragonsLegacyMod.configManager.getAbility();
        removeEffects(player, config.effects);
        removeAttributes(player, config.attributes);
        removeBindingCurse(player);
        // Clamp HP in case max_health modifier was removed
        float maxHp = player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers – apply
    // -------------------------------------------------------------------------

    private static void applyEffects(ServerPlayer player, List<EffectEntry> entries, int duration) {
        for (EffectEntry entry : entries) {
            Holder<MobEffect> effect = ConfigEffectParser.parseEffect(entry);
            if (effect == null) continue;
            player.addEffect(ConfigEffectParser.createInstance(effect, entry, duration));
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
            // Idempotent: remove before re-adding to prevent stacking
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

    private static void applyBindingCurse(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(Items.DRAGON_HEAD)) return;

        Holder<Enchantment> binding = getBindingCurseHolder(player);
        if (binding == null) return;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
            head.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
        );
        mutable.set(binding, 1);
        head.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    // -------------------------------------------------------------------------
    // Helpers – remove
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

    private static void removeBindingCurse(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) return;

        Holder<Enchantment> binding = getBindingCurseHolder(player);
        if (binding == null) return;

        ItemEnchantments existing = head.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (existing.getLevel(binding) <= 0) return;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existing);
        mutable.set(binding, 0);
        head.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static Holder<Enchantment> getBindingCurseHolder(ServerPlayer player) {
        try {
            return player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.BINDING_CURSE);
        } catch (Exception e) {
            return null;
        }
    }
}
