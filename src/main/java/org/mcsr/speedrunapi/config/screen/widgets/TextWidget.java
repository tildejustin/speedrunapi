package org.mcsr.speedrunapi.config.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public class TextWidget implements Drawable, Element {
    private final Screen screen;
    private final TextRenderer textRenderer;
    @NotNull
    private final Text text;
    @Nullable
    private final Text tooltip;
    private final int minTooltipY;
    private final int maxTooltipY;

    public int x;
    public int y;

    public TextWidget(Screen screen, TextRenderer textRenderer, @NotNull Text text) {
        this(screen, textRenderer, text, null);
    }

    public TextWidget(Screen screen, TextRenderer textRenderer, @NotNull Text text, @Nullable Text tooltip) {
        this(screen, textRenderer, text, tooltip, 0, screen.height);
    }

    public TextWidget(Screen screen, TextRenderer textRenderer, @NotNull Text text, @Nullable Text tooltip, int minTooltipY, int maxTooltipY) {
        this.screen = screen;
        this.textRenderer = textRenderer;
        this.text = text;
        this.tooltip = tooltip;
        this.minTooltipY = minTooltipY;
        this.maxTooltipY = maxTooltipY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Text textComponent = this.getTextComponentAtPosition(mouseX, mouseY);
            if (textComponent != null) {
                return this.screen.handleTextClick(textComponent);
            }
        }
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderText();
        this.renderTooltip(mouseX, mouseY);
    }

    public void renderText() {
        this.textRenderer.draw(this.text.asFormattedString(), this.x, this.y, 0xFFFFFF);
    }

    public void renderTooltip(int mouseX, int mouseY) {
        if (this.tooltip != null && this.isMouseOver(mouseX, mouseY)) {
            List<String> tooltip = this.textRenderer.wrapStringToWidthAsList(this.tooltip.asFormattedString(), 200);
            int height = tooltip.size() * 10;
            int y = mouseY;
            y = Math.min(y, this.maxTooltipY - height);
            y = Math.max(y, this.minTooltipY - height);
            this.screen.renderTooltip(tooltip, mouseX, y);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX > this.x && mouseX < this.x + this.textRenderer.getStringWidth(this.text.asFormattedString()) && mouseY > this.y && mouseY < this.y + this.textRenderer.fontHeight;
    }

    public Text getTextComponentAtPosition(double x, double y) {
        if (this.isMouseOver(x, y)) {
            return this.getTextComponentAtPositionInternal(this.text, 0, this.textRenderer.getStringWidth(this.text.asFormattedString()), x, y);
        }
        return null;
    }

    private Text getTextComponentAtPositionInternal(Text text, int textX, int width, double x, double y) {
        if (x > this.x + textX && x < this.x + textX + width && y > this.y && y < this.y + this.textRenderer.fontHeight) {
            return text;
        }
        textX += width;
        for (Text sibling : text.getSiblings()) {
            int siblingWidth = this.textRenderer.getStringWidth(sibling.asFormattedString());
            Text textAtPosition = this.getTextComponentAtPositionInternal(sibling, textX, siblingWidth, x, y);
            if (textAtPosition != null) {
                return textAtPosition;
            }
            textX += siblingWidth;
        }
        return null;
    }

    public int getWidth() {
        return this.textRenderer.getStringWidth(this.text.asFormattedString());
    }
}
