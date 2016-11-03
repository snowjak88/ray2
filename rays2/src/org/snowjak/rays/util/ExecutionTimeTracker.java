package org.snowjak.rays.util;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import org.snowjak.rays.RaytracerContext;

public class ExecutionTimeTracker implements Runnable {

	private BlockingQueue<ExecutionRecord> inputQueue;

	private Map<String, Duration> durationRecords = new LinkedHashMap<>();

	private Map<String, Long> executionCount = new LinkedHashMap<>();

	public ExecutionTimeTracker() {
		this.inputQueue = RaytracerContext.getSingleton().getTimeTrackerQueue();
	}

	/**
	 * Construct and log a new {@link ExecutionRecord}. If this method is
	 * interrupted while waiting for the input queue to become available,
	 * execute the given {@code exceptionConsumer} (which you may also omit).
	 * 
	 * @param label
	 * @param start
	 * @param end
	 * @param exceptionConsumer
	 *            if <code>null</code>, signifies no such consumer
	 */
	public static void logExecutionRecord(String label, Instant start, Instant end,
			Consumer<InterruptedException> exceptionConsumer) {

		try {
			RaytracerContext.getSingleton()
					.getTimeTrackerQueue()
					.put(new ExecutionRecord(label, Duration.between(start, end)));
		} catch (InterruptedException e) {
			if (exceptionConsumer != null)
				exceptionConsumer.accept(e);
		}
	}

	@Override
	public void run() {

		while (!Thread.interrupted()) {

			try {
				ExecutionRecord currentRecord = inputQueue.take();

				if (durationRecords.containsKey(currentRecord.getLabel())) {

					String executionName = currentRecord.getLabel();
					Duration executionDuration = currentRecord.getDuration();

					durationRecords.put(executionName, durationRecords.get(executionName).plus(executionDuration));

				} else {
					durationRecords.put(currentRecord.getLabel(), currentRecord.getDuration());
				}

				if (executionCount.containsKey(currentRecord.getLabel())) {

					String executionName = currentRecord.getLabel();

					executionCount.put(executionName, executionCount.get(executionName) + 1);

				} else {
					executionCount.put(currentRecord.getLabel(), 1l);
				}

			} catch (InterruptedException e) {
				break;
			}

		}

		System.out.println("-=-=-=-=-=- Measured execution durations -=-=-=-=-=-");
		for (String executionName : durationRecords.keySet()) {

			System.out.println(executionName + " (" + executionCount.getOrDefault(executionName, -1l)
					+ " -=- " + durationRecords.get(executionName).toString() + " (avg " + durationRecords
							.get(executionName).dividedBy(executionCount.getOrDefault(executionName, 1l)).toString()
					+ ")");

		}
		System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}

	/**
	 * A databean used to record execution records -- e.g., "procedure call X
	 * lasted Y milliseconds"
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class ExecutionRecord {

		private String label;

		private Duration duration;

		public ExecutionRecord(String label, Duration duration) {
			this.label = label;
			this.duration = duration;
		}

		public String getLabel() {

			return label;
		}

		public Duration getDuration() {

			return duration;
		}
	}

}
