package io.github.cottonmc.cotton.gui.client;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import io.github.cottonmc.cotton.gui.impl.client.TextureRegion;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import juuxel.libninepatch.NinePatch;

import java.util.function.Consumer;

/**
 * Background painters are used to paint the background of a widget.
 * The background painter instance of a widget can be changed to customize the look of a widget.
 */
@FunctionalInterface
public interface BackgroundPainter {
	/**
	 * Paint the specified panel to the screen.
	 * @param left The absolute position of the left of the panel, in gui-screen coordinates
	 * @param top The absolute position of the top of the panel, in gui-screen coordinates
	 * @param panel The panel being painted
	 */
	public void paintBackground(MatrixStack matrices, int left, int top, WWidget panel);

	/**
	 * The {@code VANILLA} background painter draws a vanilla-like GUI panel using nine-patch textures.
	 *
	 * <p>This background painter uses {@code libgui:textures/widget/panel_light.png} as the light texture and
	 * {@code libgui:textures/widget/panel_dark.png} as the dark texture.
	 *
	 * <p>This background painter is the default painter for root panels.
	 * 	 * You can override {@link io.github.cottonmc.cotton.gui.GuiDescription#addPainters()} to customize the painter yourself.
	 *
	 * @since 1.5.0
	 */
	public static BackgroundPainter VANILLA = createLightDarkVariants(
			createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_light.png")),
			createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_dark.png"))
	);

	/**
	 * The {@code SLOT} background painter draws item slots or slot-like widgets.
	 *
	 * <p>For {@linkplain WItemSlot item slots}, this painter uses {@link WItemSlot#SLOT_TEXTURE libgui:textures/widget/item_slot.png}.
	 */
	public static BackgroundPainter SLOT = (matrices, left, top, panel) -> {
		if (!(panel instanceof WItemSlot)) {
			ScreenDrawing.drawBeveledPanel(matrices, left-1, top-1, panel.getWidth()+2, panel.getHeight()+2, 0xB8000000, 0x4C000000, 0xB8FFFFFF);
		} else {
			WItemSlot slot = (WItemSlot)panel;
			for(int x = 0; x < slot.getWidth()/18; ++x) {
				for(int y = 0; y < slot.getHeight()/18; ++y) {
					int index = x + y * (slot.getWidth() / 18);
					float px = 1 / 64f;
					if (slot.isBigSlot()) {
						int sx = (x * 18) + left - 4;
						int sy = (y * 18) + top - 4;
						ScreenDrawing.texturedRect(matrices, sx, sy, 26, 26, WItemSlot.SLOT_TEXTURE,
								18 * px, 0, 44 * px, 26 * px, 0xFF_FFFFFF);
						if (slot.getFocusedSlot() == index) {
							ScreenDrawing.texturedRect(matrices, sx, sy, 26, 26, WItemSlot.SLOT_TEXTURE,
									18 * px, 26 * px, 44 * px, 52 * px, 0xFF_FFFFFF);
						}
					} else {
						int sx = (x * 18) + left;
						int sy = (y * 18) + top;
						ScreenDrawing.texturedRect(matrices, sx, sy, 18, 18, WItemSlot.SLOT_TEXTURE,
								0, 0, 18 * px, 18 * px, 0xFF_FFFFFF);
						if (slot.getFocusedSlot() == index) {
							ScreenDrawing.texturedRect(matrices, sx, sy, 18, 18, WItemSlot.SLOT_TEXTURE,
									0, 26 * px, 18 * px, 44 * px, 0xFF_FFFFFF);
						}
					}
				}
			}
		}
	};

	/**
	 * Creates a colorful gui panel painter. This painter paints the panel using the specified color.
	 *
	 * @param panelColor the panel background color
	 * @return a colorful gui panel painter
	 * @see ScreenDrawing#drawGuiPanel(MatrixStack, int, int, int, int, int)
	 */
	public static BackgroundPainter createColorful(int panelColor) {
		return (matrices, left, top, panel) -> {
			ScreenDrawing.drawGuiPanel(matrices, left, top, panel.getWidth(), panel.getHeight(), panelColor);
		};
	}

	/**
	 * Creates a colorful gui panel painter that has a custom contrast between the shadows and highlights.
	 *
	 * @param panelColor the panel background color
	 * @param contrast the contrast between the shadows and highlights
	 * @return a colorful gui panel painter
	 */
	public static BackgroundPainter createColorful(int panelColor, float contrast) {
		return (matrices, left, top, panel) -> {
			int shadowColor = ScreenDrawing.multiplyColor(panelColor, 1.0f - contrast);
			int hilightColor = ScreenDrawing.multiplyColor(panelColor, 1.0f + contrast);
			
			ScreenDrawing.drawGuiPanel(matrices, left, top, panel.getWidth(), panel.getHeight(), shadowColor, panelColor, hilightColor, 0xFF000000);
		};
	}

	/**
	 * Creates a new nine-patch background painter.
	 *
	 * <p>The resulting painter has a corner size of 4 px and a corner UV of 0.25.
	 *
	 * @param texture the background painter texture
	 * @return a new nine-patch background painter
	 * @since 1.5.0
	 * @see NinePatchBackgroundPainter
	 */
	public static NinePatchBackgroundPainter createNinePatch(Identifier texture) {
		return createNinePatch(new Texture(texture), 16, 16, 4, 4,
				builder -> builder.cornerSize(4).tileSize(8));
	}

	/**
	 * Creates a new nine-patch background painter with a custom configuration.
	 *
	 * @param texture      the background painter texture
	 * @param textureWidth width of the full texture in pixels
	 * @param textureHeight height of the full texture in pixels
	 * @param cornerXPixels width of the corner in pixels
	 * @param cornerYPixels height of the corner in pixels
	 * @param configurator a consumer that configures the {@link NinePatch.Builder}
	 * @return the created nine-patch background painter
	 * @since 4.0.0
	 * @see NinePatch
	 * @see NinePatch.Builder
	 * @see NinePatchBackgroundPainter
	 */
	public static NinePatchBackgroundPainter createNinePatch(Texture texture,
															 int textureWidth, int textureHeight,
															 int cornerXPixels, int cornerYPixels,
															 Consumer<NinePatch.Builder<AbstractTexture>> configurator) {
		float cornerWidth = ((float) cornerXPixels) / textureWidth;
		float cornerHeight = ((float) cornerYPixels) / textureHeight;

		float centerWidth = ((float) (textureWidth - (2 * cornerXPixels))) / textureWidth;
		float centerHeight = ((float) (textureHeight - (2 * cornerYPixels))) / textureHeight;

		float x0 = texture.u1();
		float x1 = texture.u1() + cornerWidth;
		float x2 = texture.u2() - cornerWidth;

		float y0 = texture.v1();
		float y1 = texture.v1() + cornerHeight;
		float y2 = texture.v2() - cornerHeight;

		NinePatch.Builder<AbstractTexture> builder = NinePatch.builder(AbstractTexture.class,
				new TextureRegion(texture.image(), x0, y0, cornerWidth, cornerHeight),
				new TextureRegion(texture.image(), x1, y0, centerWidth, cornerHeight),
				new TextureRegion(texture.image(), x2, y0, cornerWidth, cornerHeight),
				new TextureRegion(texture.image(), x0, y1, cornerWidth, centerHeight),
				new TextureRegion(texture.image(), x1, y1, centerWidth, centerHeight),
				new TextureRegion(texture.image(), x2, y1, cornerWidth, centerHeight),
				new TextureRegion(texture.image(), x0, y2, cornerWidth, cornerHeight),
				new TextureRegion(texture.image(), x1, y2, centerWidth, cornerHeight),
				new TextureRegion(texture.image(), x2, y2, cornerWidth, cornerHeight)
		);

		configurator.accept(builder);
		return new NinePatchBackgroundPainter(builder.build());
	}

	/**
	 * Creates a background painter that uses either the {@code light} or the {@code dark} background painter
	 * depending on the current setting.
	 *
	 * @param light the light mode background painter
	 * @param dark the dark mode background painter
	 * @return a new background painter that chooses between the two inputs
	 * @since 1.5.0
	 */
	public static BackgroundPainter createLightDarkVariants(BackgroundPainter light, BackgroundPainter dark) {
		return (matrices, left, top, panel) -> {
			if (LibGui.isDarkMode()) dark.paintBackground(matrices, left, top, panel);
			else light.paintBackground(matrices, left, top, panel);
		};
	}
}
