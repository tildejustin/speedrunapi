package org.mcsr.speedrunapi.config.api.option;

import org.mcsr.speedrunapi.config.screen.SpeedrunConfigScreen;

/**
 * Provides names to be used for {@link Enum} option values in the {@link SpeedrunConfigScreen}.
 */
public interface EnumTextProvider {

    /**
     * @return Returns name for the {@link Enum} value.
     */
    String toText();
}
