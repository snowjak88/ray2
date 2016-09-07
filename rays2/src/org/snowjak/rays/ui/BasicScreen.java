package org.snowjak.rays.ui;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

import javafx.application.Platform;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A basic implementation of Screen which writes to a JavaFX
 * {@link WritableImage}.
 * 
 * @author rr247200
 *
 */
public class BasicScreen extends Screen {

	private PixelWriter pixels;

	/**
	 * Create a new BasicScreen attached to the given {@link WritableImage}.
	 * 
	 * @param screenStage
	 * 
	 * @param image
	 */
	public BasicScreen(WritableImage image) {
		this(image, null);
	}

	/**
	 * Create a new BasicScreen attached to the given {@link WritableImage} and
	 * {@link Camera}.
	 * 
	 * @param image
	 * @param camera
	 */
	public BasicScreen(WritableImage image, Camera camera) {
		super((int) image.getWidth() - 1, (int) image.getHeight() - 1);

		this.pixels = image.getPixelWriter();
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		Platform.runLater(() -> pixels.setColor(x, y, color.toColor()));
	}

}
