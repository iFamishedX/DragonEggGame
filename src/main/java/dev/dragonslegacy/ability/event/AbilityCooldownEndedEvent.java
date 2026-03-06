package dev.dragonslegacy.ability.event;

/**
 * Published when the Dragon's Hunger cooldown finishes and the ability is
 * ready to be activated again.
 */
public final class AbilityCooldownEndedEvent implements AbilityEvent {
    // No additional data – the event itself signals that cooldown is over.
}
