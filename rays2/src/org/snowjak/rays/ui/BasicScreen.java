package org.snowjak.rays.ui;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

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

	private ForkJoinPool renderingThreadPool;

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
		this.renderingThreadPool = new ForkJoinPool(4, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
				(t, e) -> System.err.println("Problem in rendering thread [" + t.getName() + "]: " + e.getMessage()),
				false);
	}

	@Override
	public void draw() {

		renderingThreadPool.invoke(new RenderColumnTask(getScreenMinX(), getScreenMaxX()));
	}

	@Override
	public void shutdown() {

		super.shutdown();
		System.out.println("Shutting down rendering tasks ...");
		this.renderingThreadPool.shutdownNow();
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		Platform.runLater(() -> pixels.setColor(x, y, color.toColor()));
	}

	@SuppressWarnings("javadoc")
	public class RenderColumnTask extends RecursiveAction {

		private static final long serialVersionUID = -721352109823271505L;

		private int startColumn, endColumn;

		@Override
		protected void compute() {

			if (endColumn - startColumn > 1) {
				int midColumn = (endColumn - startColumn) / 2 + startColumn;
				renderingThreadPool.invoke(new RenderColumnTask(startColumn, midColumn));
				renderingThreadPool.invoke(new RenderColumnTask(midColumn + 1, endColumn));

			} else if (endColumn - startColumn == 1) {
				renderingThreadPool.invoke(new RenderColumnTask(startColumn, startColumn));
				renderingThreadPool.invoke(new RenderColumnTask(endColumn, endColumn));

			} else {

				try {
					for (int y = getScreenMinY(); y <= getScreenMaxY(); y++) {
						if (Thread.interrupted())
							return;

						Optional<RawColor> color = getCamera().shootRay(getCameraX(startColumn), getCameraY(y));
						if (color.isPresent())
							drawPixel(startColumn, getScreenMaxY() - y + getScreenMinY(), color.get());
					}
				} catch (Throwable t) {
					System.err.println("Problem encountered in render-thread [" + Thread.currentThread().getName()
							+ "]: " + t.getMessage());
					t.printStackTrace(System.err);
					return;
				}

			}
		}

		public RenderColumnTask(int startColumn, int endColumn) {
			this.startColumn = startColumn;
			this.endColumn = endColumn;
		}

	}

}
