package org.snowjak.rays.ui;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private ExecutorService renderingThreadPool;

	/**
	 * Create a new BasicScreen attached to the given {@link WritableImage}.
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
		super(camera, (int) image.getWidth() - 1, (int) image.getHeight() - 1);

		this.pixels = image.getPixelWriter();
		this.renderingThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
	}

	@Override
	public void draw() {

		for (int column = getScreenMinX(); column <= getScreenMaxX(); column++)
			renderingThreadPool.submit(new ColumnRenderTask(column));

	}

	@Override
	public void shutdown() {

		super.shutdown();
		if (!this.renderingThreadPool.shutdownNow().isEmpty())
			System.out.println("Shutting down rendering tasks ...");
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		Platform.runLater(() -> pixels.setColor(x, y, color.toColor()));
	}

	@SuppressWarnings("javadoc")
	public class ColumnRenderTask implements Runnable {

		private int column;

		public ColumnRenderTask(int column) {
			this.column = column;
		}

		@Override
		public void run() {

			try {
				for (int y = getScreenMinY(); y <= getScreenMaxY(); y++) {
					if (Thread.interrupted())
						return;

					Optional<RawColor> color = getCamera().shootRay(getCameraX(column), getCameraY(y));
					if (color.isPresent())
						drawPixel(column, getScreenMaxY() - y + getScreenMinY(), color.get());
				}
			} catch (Throwable t) {
				System.err.println("Problem encountered in render-thread [" + Thread.currentThread().getName() + "]: "
						+ t.getMessage());
				t.printStackTrace(System.err);
				return;
			}

		}

	}

}
