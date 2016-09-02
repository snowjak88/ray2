package org.snowjak.rays.ui;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.snowjak.rays.color.RawColor;

import javafx.application.Platform;
import javafx.stage.Stage;

public class TotalTimeElapsedScreenDecorator implements DrawsEntireScreen {

	private DrawsEntireScreen child;

	private Stage screenStage;

	private int timeUpdateInterval;

	private ScheduledExecutorService timeUpdateThread;

	public TotalTimeElapsedScreenDecorator(Stage screenStage, DrawsEntireScreen decoratedScreen,
			int timeUpdateInterval) {

		this.child = decoratedScreen;
		this.screenStage = screenStage;
		this.timeUpdateInterval = timeUpdateInterval;
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

		}, timeUpdateInterval, timeUpdateInterval, TimeUnit.SECONDS);

		child.draw();

	}

	@Override
	public void shutdown() {

		timeUpdateThread.shutdownNow();
		child.shutdown();
	}

}
