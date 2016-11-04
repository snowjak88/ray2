package org.snowjak.rays.util;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.ui.CanBeShutdown;

public class ExecutionTimeTracker implements CanBeShutdown {

	private ExecutorService trackerThreads = null;

	private List<Future<Pair<Map<String, Duration>, Map<String, Long>>>> trackerFutures = null;

	public ExecutionTimeTracker(int trackerThreadCount) {
		trackerThreads = Executors.newFixedThreadPool(trackerThreadCount);

		trackerFutures = IntStream.range(0, trackerThreadCount)
				.mapToObj(i -> trackerThreads.submit(new ExecutionTimeTrackerCallable()))
				.collect(Collectors.toCollection(LinkedList::new));
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
			RaytracerContext.getSingleton().getTimeTrackerQueue().offer(
					new ExecutionRecord(label, Duration.between(start, end)), 50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			if (exceptionConsumer != null)
				exceptionConsumer.accept(e);
		}
	}

	private static class ExecutionTimeTrackerCallable
			implements Callable<Pair<Map<String, Duration>, Map<String, Long>>> {

		private BlockingQueue<ExecutionRecord> inputQueue;

		private Map<String, Duration> durationRecords = new LinkedHashMap<>();

		private Map<String, Long> executionCounts = new LinkedHashMap<>();

		public ExecutionTimeTrackerCallable() {
			this.inputQueue = RaytracerContext.getSingleton().getTimeTrackerQueue();
		}

		@Override
		public Pair<Map<String, Duration>, Map<String, Long>> call() {

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

					if (executionCounts.containsKey(currentRecord.getLabel())) {

						String executionName = currentRecord.getLabel();

						executionCounts.put(executionName, executionCounts.get(executionName) + 1);

					} else {
						executionCounts.put(currentRecord.getLabel(), 1l);
					}

				} catch (InterruptedException e) {
					break;
				}

			}

			return new Pair<>(durationRecords, executionCounts);
		}
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

	@Override
	public void shutdown() {

		trackerThreads.shutdownNow();

		Map<String, Duration> durationRecords = new HashMap<>();
		Map<String, Long> executionCounts = new HashMap<>();

		List<Pair<Map<String, Duration>, Map<String, Long>>> trackedRecords = new LinkedList<>();
		for (Future<Pair<Map<String, Duration>, Map<String, Long>>> f : trackerFutures) {
			try {
				trackedRecords.add(f.get());
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Cannot finish building execution-time tracker results -- " + e.getMessage());
			}
		}

		trackedRecords.parallelStream().forEach(p -> {
			// Combine duration records
			for (String executionName : p.getKey().keySet()) {
				if (durationRecords.containsKey(executionName))
					durationRecords.put(executionName,
							durationRecords.get(executionName).plus(p.getKey().get(executionName)));
				else
					durationRecords.put(executionName, p.getKey().get(executionName));

			}

			// Combine execution-count records
			for (String executionName : p.getValue().keySet()) {
				if (executionCounts.containsKey(executionName))
					executionCounts.put(executionName,
							executionCounts.get(executionName) + p.getValue().get(executionName));
				else
					executionCounts.put(executionName, p.getValue().get(executionName));
			}
		});

		System.out.println("-=-=-=-=-=- Measured execution durations -=-=-=-=-=-");
		for (String executionName : durationRecords.keySet()
				.stream()
				.sorted()
				.collect(Collectors.toCollection(LinkedList::new))) {

			System.out.println(executionName + " (" + executionCounts.getOrDefault(executionName, -1l)
					+ " -=- " + durationRecords.get(executionName).toString() + " (avg " + durationRecords
							.get(executionName).dividedBy(executionCounts.getOrDefault(executionName, 1l)).toString()
					+ ")");

		}
		System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}

}
