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

	/**
	 * If the screen is rendered in regions (see
	 * {@link RenderSplitType#REGION}), then each region will be a square of
	 * {@code ...SIDE_LENGTH} pixels on each side.
	 */
	public static final int REGION_SIDE_LENGTH = 64;

	private DrawsScreenPixel child;

	private ExecutorService renderingThreadPool;

	private RenderSplitType splitType;

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the default
	 * number of threads (i.e.,
	 * {@code Runtime.getRuntime().availableProcessors() - 1}).
	 * 
	 * @param child
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child) {

		this(child, FastMath.max(Runtime.getRuntime().availableProcessors() - 1, 1));
	}

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the specified
	 * number of rendering threads.
	 * 
	 * @param child
	 * @param renderingThreadCount
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child, int renderingThreadCount) {
		this(child, renderingThreadCount, RenderSplitType.REGION);
	}

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the specified
	 * number of rendering threads and splitting the render-job using the
	 * specified paradigm.
	 * 
	 * @param child
	 * @param renderingThreadCount
	 * @param splitType
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child, int renderingThreadCount, RenderSplitType splitType) {
		this.child = child;
		this.renderingThreadPool = Executors.newFixedThreadPool(FastMath.max(renderingThreadCount, 1));
		this.splitType = splitType;
	}

	@Override
	public void draw() {

		switch (splitType) {
		case COLUMN:
			for (int column = child.getScreenMinX(); column <= child.getScreenMaxX(); column++)
				renderingThreadPool.submit(new ColumnRenderTask(column));
			break;

		case REGION:
			int sizeX = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxX() - child.getScreenMinX());
			int sizeY = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxY() - child.getScreenMinY());

			for (int startY = child.getScreenMinY(); startY < child.getScreenMaxY(); startY += sizeY + 1)
				for (int startX = child.getScreenMinX(); startX < child.getScreenMaxX(); startX += sizeX + 1) {

					int extentX = FastMath.min(startX + sizeX, child.getScreenMaxX() - child.getScreenMinX());
					int extentY = FastMath.min(startY + sizeY, child.getScreenMaxY() - child.getScreenMinY());
					renderingThreadPool
							.submit(new RegionRenderTask(startX, startY, extentX - startX, extentY - startY));
				}
		}
	}

	@Override
	public void shutdown() {

		if (!this.renderingThreadPool.shutdownNow().isEmpty())
			System.out.println("Shutting down rendering tasks ...");
		child.shutdown();
	}

	@SuppressWarnings("javadoc")
	public class RegionRenderTask implements Runnable {

		private int startX, startY, sizeX, sizeY;

		public RegionRenderTask(int startX, int startY, int sizeX, int sizeY) {
			this.startX = startX;
			this.startY = startY;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}

		@Override
		public void run() {

			try {

				for (int dx = 0; dx <= sizeX; dx++)
					for (int dy = 0; dy <= sizeY; dy++) {

						if (Thread.interrupted())
							return;

						Optional<RawColor> color = child.getRayColor(startX + dx, startY + dy);
						if (color.isPresent())
							child.drawPixel(startX + dx, startY + dy, color.get());

					}

			} catch (Throwable t) {
				System.err.println("Problem encountered in render-thread [" + Thread.currentThread().getName() + "]: "
						+ t.getMessage());
				t.printStackTrace(System.err);
				return;
			}
		}
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

	/**
	 * Denotes how a {@link MultithreadedScreenDecorator} will split up the
	 * screen-rendering job.
	 * 
	 * @author rr247200
	 *
	 */
	public static enum RenderSplitType {
		/**
		 * Split up the screen into individual regions.
		 */
		REGION,
		/**
		 * Render the entire screen column by column.
		 */
		COLUMN;
	}

}
