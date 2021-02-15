package com.dumbdogdiner.stickychat.api.misc;

import net.md_5.bungee.api.chat.TextComponent;

/**
 * Handles the storage of data for broadcasts.
 */
public interface BroadcastTimer {
    /**
     * @return The {@link TextComponent} that this timer wraps.
     */
    TextComponent getTextComponent();

    /**
     * @return The interval in seconds between timer broadcasts.
     */
    Long getInterval();
}
