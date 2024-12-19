package org.mcsr.speedrunapi.config.screen.widgets.list;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.mcsr.speedrunapi.SpeedrunAPI;
import org.mcsr.speedrunapi.config.api.SpeedrunConfigScreenProvider;
import org.mcsr.speedrunapi.config.screen.SpeedrunModConfigsScreen;
import org.mcsr.speedrunapi.config.screen.widgets.TextWidget;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class SpeedrunModConfigListWidget extends EntryListWidget<SpeedrunModConfigListWidget.ModConfigListEntry> {
    private static final Identifier NO_MOD_ICON = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier EDIT_MOD_CONFIG = new Identifier("textures/gui/world_selection.png");

    private final SpeedrunModConfigsScreen parent;

    public SpeedrunModConfigListWidget(Map<ModContainer, SpeedrunConfigScreenProvider> modConfigScreenProviders, SpeedrunModConfigsScreen parent, MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom, 36);
        this.parent = parent;

        for (Map.Entry<ModContainer, SpeedrunConfigScreenProvider> config : modConfigScreenProviders.entrySet()) {
            this.addEntry(new ModConfigEntry(config.getKey(), config.getValue()));
        }
/*
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            // the mod.getContainingMod().isPresent() check fails in a dev environment
            if (modConfigScreenProviders.containsKey(mod) || mod.getMetadata().getType().equals("builtin") || mod.getMetadata().getId().equals("fabricloader") || mod.getContainingMod().isPresent()) {
                continue;
            }
            this.addEntry(new ModEntry(mod));
        }
 */

        if (this.children().isEmpty()) {
            this.addEntry(new NoModConfigsEntry());
        }
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    @Override
    public ModConfigListEntry getFocused() {
        return this.getSelected();
    }

    public abstract static class ModConfigListEntry extends EntryListWidget.Entry<ModConfigListEntry> {
    }

    public class ModEntry extends ModConfigListEntry {
        protected final ModContainer modContainer;
        protected final ModMetadata mod;
        protected final Identifier icon;
        protected final Text name;
        protected final Text version;
        @Nullable
        protected final TextWidget authors;
        protected final List<String> description;
        protected boolean hasIcon;

        public ModEntry(ModContainer mod) {
            this.modContainer = mod;
            this.mod = this.modContainer.getMetadata();
            this.icon = new Identifier("speedrunapi", "mods/" + this.mod.getId() + "/icon");

            this.name = new LiteralText(this.mod.getName());
            this.version = new LiteralText(this.mod.getVersion().getFriendlyString().split("\\+")[0]).formatted(Formatting.GRAY);
            this.authors = this.createAuthorsText(this.mod.getAuthors());
            this.description = this.createDescription(this.mod.getDescription());

            this.registerIcon();
        }

        private @Nullable TextWidget createAuthorsText(Collection<Person> authors) {
            if (authors == null || authors.isEmpty()) {
                return null;
            }
            Text text = new LiteralText(" by ").styled(style -> style.setColor(Formatting.GRAY).setItalic(true));
            boolean shouldAddComma = false;
            for (Person person : this.mod.getAuthors()) {
                LiteralText author = new LiteralText(person.getName());
                person.getContact().get("homepage").ifPresent(link -> author.styled(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)).setUnderline(true)));
                if (shouldAddComma) {
                    text.append(new LiteralText(", "));
                }
                text.append(author);
                shouldAddComma = true;
            }
            return new TextWidget(SpeedrunModConfigListWidget.this.parent, SpeedrunModConfigListWidget.this.client.textRenderer, text);
        }

        private List<String> createDescription(String description) {
            List<String> list = SpeedrunModConfigListWidget.this.client.textRenderer.wrapStringToWidthAsList(description, SpeedrunModConfigListWidget.this.getRowWidth() - 32 - 6);
            if (list.size() > 2) {
                list.set(1, list.get(1) + "...");
                return list.subList(0, 2);
            }
            return list;
        }

        private void registerIcon() {
            if (SpeedrunModConfigListWidget.this.client.getTextureManager().getTexture(this.icon) != null) {
                this.hasIcon = true;
                return;
            }
            this.mod.getIconPath(32).flatMap(this.modContainer::findPath).ifPresent(iconPath -> {
                try (InputStream inputStream = Files.newInputStream(iconPath)) {
                    SpeedrunModConfigListWidget.this.client.getTextureManager().registerTexture(this.icon, new NativeImageBackedTexture(NativeImage.read(inputStream)));
                    this.hasIcon = true;
                } catch (IOException e) {
                    SpeedrunAPI.LOGGER.warn("Failed to load mod icon for {}.", this.mod.getId(), e);
                }
            });
        }

        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            MinecraftClient client = SpeedrunModConfigListWidget.this.client;
            TextRenderer textRenderer = client.textRenderer;

            textRenderer.draw(this.name.asFormattedString(), x + 32 + 3, y + 1, 0xFFFFFF);
            textRenderer.draw(this.version.asFormattedString(), x + 32 + 3 + textRenderer.getStringWidth(this.name.asFormattedString()) + 4, y + 1, 0xFFFFFF);

            if (this.authors != null) {
                this.authors.x = x + entryWidth - this.authors.getWidth() - 5;
                this.authors.y = y + 1;
                Text hoveredComponent = this.authors.getTextComponentAtPosition(mouseX, mouseY);
                if (hoveredComponent instanceof BaseText && hoveredComponent.getStyle().getClickEvent() != null) {
                    Formatting originalColor = hoveredComponent.getStyle().getColor();
                    hoveredComponent.styled(style -> style.setColor(Formatting.WHITE));
                    this.authors.render(mouseX, mouseY, tickDelta);
                    hoveredComponent.styled(style -> style.setColor(originalColor));
                } else {
                    this.authors.render(mouseX, mouseY, tickDelta);
                }
            }

            int yOffset = 0;
            for (String descriptionLine : this.description) {
                textRenderer.draw(descriptionLine, x + 32 + 3, y + textRenderer.fontHeight + 3 + yOffset, 0x808080);
                yOffset += textRenderer.fontHeight;
            }

            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

            client.getTextureManager().bindTexture(this.hasIcon ? this.icon : NO_MOD_ICON);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();

            if (client.options.touchscreen || hovered) {
                this.renderIfHovered(x, y, mouseX, mouseY);
            }
        }

        protected void renderIfHovered(int x, int y, int mouseX, int mouseY) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            SpeedrunModConfigListWidget.this.setSelected(this);
            if (this.authors != null) {
                return this.authors.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
    }

    public class ModConfigEntry extends ModEntry {
        private final SpeedrunConfigScreenProvider configScreenProvider;
        @Nullable
        private final Text unavailableTooltip;
        private long lastPress;

        public ModConfigEntry(ModContainer mod, SpeedrunConfigScreenProvider configScreenProvider) {
            super(mod);
            this.configScreenProvider = configScreenProvider;
            String configUnavailableKey = "speedrunapi.config." + this.mod.getId() + ".unavailable";
            if (Language.getInstance().hasTranslation(configUnavailableKey)) {
                this.unavailableTooltip = new TranslatableText(configUnavailableKey);
            } else {
                this.unavailableTooltip = new TranslatableText("speedrunapi.gui.config.unavailable");
            }
        }

        @Override
        protected void renderIfHovered(int x, int y, int mouseX, int mouseY) {
            boolean available = this.configScreenProvider.isAvailable();

            SpeedrunModConfigListWidget.this.client.getTextureManager().bindTexture(EDIT_MOD_CONFIG);
            DrawableHelper.fill(x, y, x + 32, y + 32, -1601138544);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            int textureOffset = mouseX - x < 32 ? 32 : 0;
            DrawableHelper.drawTexture(x, y, available ? 0.0f : 96.0f, textureOffset, 32, 32, 256, 256);

            if (!available && this.isMouseOver(mouseX, mouseY)) {
                SpeedrunModConfigListWidget.this.parent.renderTooltip(SpeedrunModConfigListWidget.this.client.textRenderer.wrapStringToWidthAsList(this.unavailableTooltip.asFormattedString(), 200), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            SpeedrunModConfigListWidget.this.setSelected(this);
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (mouseX - SpeedrunModConfigListWidget.this.getRowLeft() <= 32.0) {
                return this.openConfig();
            }
            if (Util.getMeasuringTimeMs() - this.lastPress < 250L) {
                return this.openConfig();
            }
            this.lastPress = Util.getMeasuringTimeMs();
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                return this.openConfig();
            }
            return false;
        }

        private boolean openConfig() {
            if (!this.configScreenProvider.isAvailable()) {
                return false;
            }
            SpeedrunModConfigListWidget.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            SpeedrunModConfigListWidget.this.client.openScreen(this.configScreenProvider.createConfigScreen(SpeedrunModConfigListWidget.this.parent));
            return true;
        }
    }

    public class NoModConfigsEntry extends ModConfigListEntry {
        private final Text text = new TranslatableText("speedrunapi.gui.config.noConfigs");

        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            SpeedrunModConfigListWidget.this.drawCenteredString(SpeedrunModConfigListWidget.this.client.textRenderer, this.text.asFormattedString(), x + entryWidth / 2, y + entryHeight / 2, 0xFFFFFF);
        }
    }
}
