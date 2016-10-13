package org.snowjak.rays.ui;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.snowjak.rays.camera.Camera;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Decorator for an existing ScreenDrawer implementation, that sets and updates
 * the render-window title with the total time-elapsed over the render.
 * 
 * @author snowjak88
 *
 */
@Deprecated
public class TotalTimeElapsedScreenDecorator implements ScreenDrawer {

	private ScreenDrawer child;

	private Stage screenStage;

	private int timeUpdateInterval;

	private TimeUnit intervalUnit;

	private ScheduledExecutorService timeUpdateThread;

	/**
	 * Create a new TotalTimeElapsedScreenDecorator, with the default
	 * update-interval of 1 second.
	 * 
	 * @param screenStage
	 *            the {@link Stage} containing the render screen
	 * @param decoratedScreen
	 *            the {@link ScreenDrawer} implementation to decorate
	 */
	public TotalTimeElapsedScreenDecorator(Stage screenStage, ScreenDrawer decoratedScreen) {
		this(screenStage, decoratedScreen, 1, TimeUnit.SECONDS);
	}

	/**
	 * Create a new TotalTimeElapsedScreenDecorator.
	 * 
	 * @param screenStage
	 *            the {@link Stage} containing the render screen
	 * @param decoratedScreen
	 *            the {@link ScreenDrawer} implementation to decorate
	 * @param timeUpdateInterval
	 *            the number of time-units to wait between timer-display updates
	 * @param intervalUnit
	 *            the {@link TimeUnit} in which the above interval is expressed
	 */
	public TotalTimeElapsedScreenDecorator(Stage screenStage, ScreenDrawer decoratedScreen, int timeUpdateInterval,
			TimeUnit intervalUnit) {

		this.child = decoratedScreen;
		this.screenStage = screenStage;
		this.timeUpdateInterval = timeUpdateInterval;
		this.intervalUnit = intervalUnit;
		this.timeUpdateThread = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void draw(Camera camera) {

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

		}, timeUpdateInterval, timeUpdateInterval, intervalUnit);

		child.draw(camera);

		timeUpdateThread.shutdown();
	}

	@Override
	public void shutdown() {

		timeUpdateThread.shutdownNow();
		child.shutdown();
	}

}
