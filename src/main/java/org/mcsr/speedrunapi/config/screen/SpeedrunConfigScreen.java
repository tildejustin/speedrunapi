package org.mcsr.speedrunapi.config.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.mcsr.speedrunapi.SpeedrunAPI;
import org.mcsr.speedrunapi.config.SpeedrunConfigContainer;
import org.mcsr.speedrunapi.config.screen.widgets.list.SpeedrunOptionListWidget;

import java.io.IOException;
import java.util.function.Predicate;

@ApiStatus.Internal
public class SpeedrunConfigScreen extends Screen {
    private final SpeedrunConfigContainer<?> config;
    @Nullable
    private final Predicate<InputUtil.KeyCode> inputListener;
    private final Screen parent;

    private SpeedrunOptionListWidget list;
    private TextFieldWidget searchField;
    private boolean searchFieldOpen;

    public SpeedrunConfigScreen(SpeedrunConfigContainer<?> config, @Nullable Predicate<InputUtil.KeyCode> inputListener, Screen parent) {
        super(new LiteralText(config.getModContainer().getMetadata().getName()));
        this.config = config;
        this.inputListener = inputListener;
        this.parent = parent;
    }

    private void toggleSearchField() {
        this.searchFieldOpen = !this.searchFieldOpen;
        this.searchField.setVisible(this.searchFieldOpen);
        if (this.searchFieldOpen) {
            this.setFocused(this.searchField);
            this.searchField.setSelected(true);
            this.list.adjustTop(50);
        } else {
            this.searchField.setText("");
            this.list.adjustTop(25);
        }
    }

    @Override
    protected void init() {
        assert this.client != null;
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 25, 200, 20, this.searchField, I18n.translate("speedrunapi.gui.config.search"));
        this.searchField.setVisible(this.searchFieldOpen);
        this.searchField.setChangedListener(string -> this.list.updateEntries(string));
        this.addChild(this.searchField);
        this.list = new SpeedrunOptionListWidget(this, this.config, this.client, this.width, this.height, 25, this.height - 32, this.searchField.getText());
        if (this.searchFieldOpen) {
            this.list.adjustTop(50);
        }
        this.addChild(this.list);
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, I18n.translate("gui.done"), button -> this.onClose()));
        this.client.keyboard.enableRepeatEvents(true);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.list.render(mouseX, mouseY, delta);
        this.searchField.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.textRenderer, this.title.asFormattedString(), this.width / 2, 10, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.inputListener != null && this.inputListener.test(InputUtil.getKeyCode(keyCode, scanCode))) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F && Screen.hasControlDown()) {
            this.toggleSearchField();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.inputListener != null && this.inputListener.test(InputUtil.Type.MOUSE.createFromCode(button))) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        assert this.client != null;
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed() {
        assert this.client != null;
        this.client.keyboard.enableRepeatEvents(false);
        try {
            this.config.save();
        } catch (IOException e) {
            SpeedrunAPI.LOGGER.warn("Failed to save config file for {}.", this.config.getConfig().modID());
        }
    }
}
