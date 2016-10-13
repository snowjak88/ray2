package org.snowjak.rays.ui;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

/**
 * Renders a {@link PixelDrawer} on several threads at once.
 */
public class MultithreadedScreenDecorator implements ScreenDrawer {

	/**
	 * If the screen is rendered in regions (see
	 * {@link RenderSplitType#REGION}), then each region will be a square of
	 * {@code ...SIDE_LENGTH} pixels on each side.
	 */
	public static final int REGION_SIDE_LENGTH = 64;

	private PixelDrawer child;

	/**
	 * Create a new {@link MultithreadedScreenDecorator} using the specified
	 * number of rendering threads and splitting the render-job using the
	 * specified paradigm.
	 * 
	 * @param child
	 * @param renderingThreadCount
	 * @param splitType
	 */
	public MultithreadedScreenDecorator(PixelDrawer child) {
		this.child = child;
	}

	@Override
	public void draw(Camera camera) {

		RenderSplitType splitType = RaytracerContext.getSingleton().getSettings().getRenderSplitType();

		switch (splitType) {
		case COLUMN:
			for (int column = child.getScreenMinX(); column <= child.getScreenMaxX(); column++)
				RaytracerContext.getSingleton().getWorkerThreadPool().submit(new ColumnRenderTask(camera, column));
			break;

		case REGION:
			int sizeX = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxX() - child.getScreenMinX());
			int sizeY = FastMath.min(REGION_SIDE_LENGTH, child.getScreenMaxY() - child.getScreenMinY());

			for (int startY = child.getScreenMinY(); startY < child.getScreenMaxY(); startY += sizeY + 1)
				for (int startX = child.getScreenMinX(); startX < child.getScreenMaxX(); startX += sizeX + 1) {

					int extentX = FastMath.min(startX + sizeX, child.getScreenMaxX() - child.getScreenMinX());
					int extentY = FastMath.min(startY + sizeY, child.getScreenMaxY() - child.getScreenMinY());
					RaytracerContext.getSingleton().getWorkerThreadPool().submit(
							new RegionRenderTask(camera, startX, startY, extentX - startX, extentY - startY));
				}
		}
	}

	@Override
	public void shutdown() {

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
	 * @author snowjak88
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

		/**
		 * Convert the given {@link RenderSplitType} value to its String
		 * equivalent.
		 * 
		 * @param value
		 * @return the String equivalent of the given RenderSplitType value
		 */
		public static String toString(RenderSplitType value) {

			return value.toString();
		}

		/**
		 * Convert the given String to its equivalent {@link RenderSplitType}
		 * value, or {@link RenderSplitType#REGION} if no such value can be
		 * found.
		 * <p>
		 * A String is equivalent to a RenderSplitType value if
		 * {@link RenderSplitType#toString()} {@code equalsIgnoreCase(value)}
		 * </p>
		 * 
		 * @param value
		 * @return the RenderSplitType equivalent of the given String value
		 */
		public static RenderSplitType fromString(String value) {

			return Arrays.stream(values())
					.filter(rst -> rst.toString().equalsIgnoreCase(value))
					.findAny()
					.orElse(RenderSplitType.REGION);
		}
	}

}
