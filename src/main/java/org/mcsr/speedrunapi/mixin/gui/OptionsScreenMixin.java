package org.mcsr.speedrunapi.mixin.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.mcsr.speedrunapi.config.screen.SpeedrunModConfigsScreen;
import org.mcsr.speedrunapi.config.screen.widgets.IconButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSpeedrunConfigButton(CallbackInfo ci) {
        this.addButton(new IconButtonWidget(new Identifier("textures/item/writable_book.png"), this.width / 2 + 160, this.height / 6 - 12, I18n.translate("speedrunapi.gui.config.button"), button -> {
            assert this.client != null;
            this.client.openScreen(new SpeedrunModConfigsScreen(this));
        }));
    }
}
