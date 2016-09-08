package org.snowjak.rays.ui;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
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
	 * Create a new {@link MultithreadedScreenDecorator} using the specified
	 * number of rendering threads and splitting the render-job using the
	 * specified paradigm.
	 * 
	 * @param child
	 * @param renderingThreadCount
	 * @param splitType
	 */
	public MultithreadedScreenDecorator(DrawsScreenPixel child) {
		this.child = child;
		this.renderingThreadPool = Executors
				.newFixedThreadPool(FastMath.max(Settings.getSingleton().getRenderThreadCount(), 1));
		this.splitType = Settings.getSingleton().getRenderSplitType();
	}

	@Override
	public void draw(Camera camera) {

		switch (splitType) {
		case COLUMN:
			for (int column = child.getScreenMinX(); column <= child.getScreenMaxX(); column++)
				renderingThreadPool.submit(new ColumnRenderTask(camera, column));
			break;

		case REGION:
			int sizeX = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxX() - child.getScreenMinX());
			int sizeY = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxY() - child.getScreenMinY());

			for (int startY = child.getScreenMinY(); startY < child.getScreenMaxY(); startY += sizeY + 1)
				for (int startX = child.getScreenMinX(); startX < child.getScreenMaxX(); startX += sizeX + 1) {

					int extentX = FastMath.min(startX + sizeX, child.getScreenMaxX() - child.getScreenMinX());
					int extentY = FastMath.min(startY + sizeY, child.getScreenMaxY() - child.getScreenMinY());
					renderingThreadPool
							.submit(new RegionRenderTask(camera, startX, startY, extentX - startX, extentY - startY));
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

		private Camera camera;

		public RegionRenderTask(Camera camera, int startX, int startY, int sizeX, int sizeY) {
			this.camera = camera;
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

						Optional<RawColor> color = child.getRayColor(startX + dx, startY + dy, camera);
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

		private Camera camera;

		public ColumnRenderTask(Camera camera, int column) {
			this.camera = camera;
			this.column = column;
		}

		@Override
		public void run() {

			try {
				for (int y = child.getScreenMinY(); y <= child.getScreenMaxY(); y++) {
					if (Thread.interrupted())
						return;

					Optional<RawColor> color = child.getRayColor(column, y, camera);
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
