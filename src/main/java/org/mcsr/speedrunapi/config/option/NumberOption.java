package org.mcsr.speedrunapi.config.option;

import org.mcsr.speedrunapi.config.api.SpeedrunConfig;
import org.mcsr.speedrunapi.config.api.annotations.Config;

import java.lang.reflect.Field;

public abstract class NumberOption<T extends Number> extends BaseOption<T> {

    protected final boolean useTextField;

    public NumberOption(SpeedrunConfig config, Field option) {
        super(config, option);

        this.useTextField = option.isAnnotationPresent(Config.Numbers.TextField.class);
    }

    public abstract void setFromSliderValue(double sliderValue);

    public abstract void setFromString(String stringValue) throws NumberFormatException;
}
