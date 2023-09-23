package org.mcsr.speedrunapi.config.screen.widgets.option;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.mcsr.speedrunapi.config.option.EnumOption;

public class EnumOptionButtonWidget extends ButtonWidget {

    public EnumOptionButtonWidget(EnumOption option, int x, int y) {
        super(x, y, 150, 20, new LiteralText(option.get().toString()), button -> {
            Enum<?> current = option.get();
            Enum<?>[] enumConstants = current.getClass().getEnumConstants();
            option.set(enumConstants[(current.ordinal() + 1) % enumConstants.length]);
            button.setMessage(option.getText());
        });
    }
}
