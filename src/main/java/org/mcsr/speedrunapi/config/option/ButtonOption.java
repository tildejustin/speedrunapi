package org.mcsr.speedrunapi.config.option;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcsr.speedrunapi.config.api.SpeedrunConfig;
import org.mcsr.speedrunapi.config.api.SpeedrunOption;

import java.util.function.Supplier;

@ApiStatus.Internal
public class ButtonOption implements SpeedrunOption<Void> {
    private final SpeedrunConfig config;
    @Nullable
    private final String id;
    private final Text message;
    private final ButtonWidget.PressAction onPress;
    private final Supplier<Boolean> hideIf;
    @Nullable
    private final String name;
    @Nullable
    private final String description;
    @Nullable
    protected String category;

    public ButtonOption(SpeedrunConfig config, @Nullable String id, Text message, ButtonWidget.PressAction onPress, Supplier<Boolean> hideIf, @Nullable String name, @Nullable String description, @Nullable String category) {
        this.config = config;
        this.id = id;
        this.message = message;
        this.onPress = onPress;
        this.hideIf = hideIf;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public @Nullable String getCategory() {
        return this.category;
    }

    @Override
    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    @Override
    public String getModID() {
        return this.config.modID();
    }

    @Override
    public @NotNull Text getName() {
        if (this.name != null) {
            return new TranslatableText(this.name);
        }
        return SpeedrunOption.super.getName();
    }

    @Override
    public @Nullable Text getDescription() {
        if (this.description != null) {
            return new TranslatableText(this.description);
        }
        return SpeedrunOption.super.getDescription();
    }

    @Override
    public Void get() {
        return null;
    }

    @Override
    public void set(Void value) {

    }

    @Override
    public void setUnsafely(Object value) throws ClassCastException {

    }

    @Override
    public void fromJson(JsonElement jsonElement) {

    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public boolean hasWidget() {
        return this.hideIf.get();
    }

    @Override
    public @NotNull AbstractButtonWidget createWidget() {
        return new ButtonWidget(0, 0, 150, 20, this.message, this.onPress);
    }
}
