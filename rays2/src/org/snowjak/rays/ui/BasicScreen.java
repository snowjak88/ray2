package org.snowjak.rays.ui;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

import javafx.application.Platform;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

/**
 * A basic implementation of Screen which writes to a JavaFX
 * {@link WritableImage}.
 * 
 * @author rr247200
 *
 */
public class BasicScreen extends Screen {

	private Stage screenStage;

	private PixelWriter pixels;

	private ExecutorService renderingThreadPool;

	private ScheduledExecutorService timeUpdateThread;

	/**
	 * Create a new BasicScreen attached to the given {@link WritableImage}.
	 * 
	 * @param screenStage
	 * 
	 * @param image
	 */
	public BasicScreen(Stage screenStage, WritableImage image) {
		this(screenStage, image, null);
	}

	/**
	 * Create a new BasicScreen attached to the given {@link WritableImage} and
	 * {@link Camera}.
	 * 
	 * @param screenStage
	 * @param image
	 * @param camera
	 */
	public BasicScreen(Stage screenStage, WritableImage image, Camera camera) {
		super(camera, (int) image.getWidth() - 1, (int) image.getHeight() - 1);

		this.screenStage = screenStage;
		this.pixels = image.getPixelWriter();
		this.renderingThreadPool = Executors
				.newFixedThreadPool(FastMath.max(Runtime.getRuntime().availableProcessors() - 2, 1));
		this.timeUpdateThread = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void draw() {

		AtomicReference<Instant> startedRunning = new AtomicReference<Instant>(Instant.now());

		timeUpdateThread.scheduleAtFixedRate(() -> {

			final Instant endInstant = Instant.now();
			final long totalSeconds = Duration.between(startedRunning.get(), endInstant).getSeconds();
			final long hours = totalSeconds / 3600;
			final long minutes = (totalSeconds % 3600) / 60;
			final long seconds = totalSeconds % 60;
			final String newTitle = String.format("Render: %d:%02d:%02d", hours, minutes, seconds);

			Platform.runLater(() -> {
				screenStage.setTitle(newTitle);
			});

		}, 1, 1, TimeUnit.SECONDS);

		for (int column = getScreenMinX(); column <= getScreenMaxX(); column++)
			renderingThreadPool.submit(new ColumnRenderTask(column));

	}

	@Override
	public void shutdown() {

		super.shutdown();
		timeUpdateThread.shutdownNow();
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
