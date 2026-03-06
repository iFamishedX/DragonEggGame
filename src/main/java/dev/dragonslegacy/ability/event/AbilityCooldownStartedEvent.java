package dev.dragonslegacy.ability.event;

/**
 * Published when the Dragon's Hunger ability enters cooldown, either after
 * natural expiry ({@link AbilityExpiredEvent}) or manual deactivation
 * ({@link AbilityDeactivatedEvent}).
 */
public final class AbilityCooldownStartedEvent implements AbilityEvent {

    private final int cooldownTicks;

    public AbilityCooldownStartedEvent(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    /** The total cooldown duration in ticks. */
    public int getCooldownTicks() { return cooldownTicks; }
}
