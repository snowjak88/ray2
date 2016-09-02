package org.snowjak.rays.ui;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.color.RawColor;

/**
 * Renders a {@link DrawsScreenPixel} on several threads at once.
 */
public class MultithreadedScreenDecorator implements DrawsEntireScreen {

	private DrawsScreenPixel child;

	private ExecutorService renderingThreadPool;

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the default
	 * number of threads (i.e.,
	 * {@code Runtime.getRuntime().availableProcessors() - 2}).
	 * 
	 * @param child
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child) {

		this(child, FastMath.max(Runtime.getRuntime().availableProcessors(), 1));
	}

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the specified
	 * number of rendering threads.
	 * 
	 * @param child
	 * @param renderingThreadCount
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child, int renderingThreadCount) {
		this.child = child;
		this.renderingThreadPool = Executors.newFixedThreadPool(FastMath.max(renderingThreadCount, 1));
	}

	@Override
	public void draw() {

		for (int column = child.getScreenMinX(); column <= child.getScreenMaxX(); column++)
			renderingThreadPool.submit(new ColumnRenderTask(column));
	}

	@Override
	public void shutdown() {

		child.shutdown();
		if (!this.renderingThreadPool.shutdownNow().isEmpty())
			System.out.println("Shutting down rendering tasks ...");
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
				for (int y = child.getScreenMinY(); y <= child.getScreenMaxY(); y++) {
					if (Thread.interrupted())
						return;

					Optional<RawColor> color = child.getRayColor(column, y);
					if (color.isPresent())
						child.drawPixel(column, y, color.get());

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
