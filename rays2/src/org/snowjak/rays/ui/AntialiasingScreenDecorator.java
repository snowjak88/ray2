package org.snowjak.rays.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

/**
 * Implements a simple antialiasing filter on top of an existing
 * {@link PixelDrawer} implementation.
 * <p>
 * This Decorator implements a simple box filter. It samples a variety of Rays
 * +/- the central Ray, and combines all the resulting colors into a single
 * color.
 * </p>
 * <p>
 * These colors are combined using the {@code sinc} function:
 * 
 * <pre>
 *    factor = [sin(x) / (x)] * [sin(y) / (y)]
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class AntialiasingScreenDecorator implements PixelDrawer {

	private RealDistribution distribution;

	private PixelDrawer child;

	private SuperSamplingAntialiaser<Vector3D, Optional<RawColor>, Optional<RawColor>> antialiaser;

	/**
	 * Create a new AntialiasingScreenDecorator on top of an existing
	 * {@link PixelDrawer} instance.
	 * 
	 * @param decoratedScreen
	 *            the existing screen to decorate
	 */
	public AntialiasingScreenDecorator(PixelDrawer decoratedScreen) {

		this.child = decoratedScreen;
		this.distribution = new NormalDistribution(0d, 0.5);
		this.antialiaser = new SuperSamplingAntialiaser<>();
	}

	@Override
	public Optional<RawColor> getRayColor(int screenX, int screenY, Camera camera) {

		final AA aaSetting = RaytracerContext.getSingleton().getSettings().getAntialiasing();
		final double filterSpan = 1;
		final double coordinateDelta;
		if (aaSetting != AA.OFF)
			coordinateDelta = filterSpan / ((double) (aaSetting.sampleCount / 2));
		else
			coordinateDelta = 0d;

		return antialiaser.execute(new Vector3D(screenX, screenY, 0d), (v) -> {
			Collection<Vector3D> results = new LinkedList<>();
			if (aaSetting == AA.OFF)
				results.add(new Vector3D(getCameraX(v.getX(), camera), getCameraY(v.getY(), camera), 0d));
			else
				for (double dx = -(filterSpan / 2d); dx <= (filterSpan / 2d); dx += coordinateDelta)
					for (double dy = -(filterSpan / 2d); dy <= (filterSpan / 2d); dy += coordinateDelta)
						results.add(
								new Vector3D(getCameraX(screenX + dx, camera), getCameraY(screenY + dy, camera), 0d));
			return results;

		}, (v) -> {
			return (Optional<RawColor>) camera.shootRay(v.getX(), v.getY()).map(lr -> lr.getRadiance());

		}, (lp) -> {
			if (aaSetting == AA.OFF)
				return lp.stream().findFirst().map(p -> p.getValue()).orElse(Optional.empty());

			double totalScale = 0d;
			RawColor totalColor = new RawColor();
			for (Pair<Vector3D, Optional<RawColor>> pair : lp) {
				Vector3D samplePoint = pair.getKey();
				Optional<RawColor> sample = pair.getValue();
				double scale = distribution.density(
						FastMath.sqrt(FastMath.pow(samplePoint.getX(), 2d) + FastMath.pow(samplePoint.getY(), 2d)));
				totalScale += scale;
				if (sample.isPresent())
					totalColor = totalColor.add(sample.get().multiplyScalar(scale));
			}
			return Optional.of(totalColor.multiplyScalar(1d / totalScale));
		});
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		child.drawPixel(x, y, color);
	}

	@Override
	public void shutdown() {

		child.shutdown();
	}

	@Override
	public int getScreenMinX() {

		return child.getScreenMinX();
	}

	@Override
	public int getScreenMinY() {

		return child.getScreenMinY();
	}

	@Override
	public int getScreenMaxX() {

		return child.getScreenMaxX();
	}

	@Override
	public int getScreenMaxY() {

		return child.getScreenMaxY();
	}

	/**
	 * Defines the number of samples to use when anti-aliasing.
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static enum AA {
		/**
		 * Turn off antialiasing completely.
		 */
		OFF(-1), x2(3), x4(5), x8(9), x16(17), x32(33);

		private int sampleCount;

		AA(int sampleCount) {
			this.sampleCount = sampleCount;
		}

		/**
		 * Convert the given {@link AA} value to its String equivalent.
		 * 
		 * @param value
		 * @return the String equivalent of the given AA value
		 */
		public static String toString(AA value) {

			return value.toString();
		}

		/**
		 * Convert the given String to its equivalent {@link AA} value, or
		 * {@link AA#OFF} if no such value can be found.
		 * <p>
		 * A String is equivalent to an AA value if {@link AA#toString()}
		 * {@code equalsIgnoreCase(value)}
		 * </p>
		 * 
		 * @param value
		 * @return
		 */
		public static AA fromString(String value) {

			return Arrays.stream(values()).filter(aa -> aa.toString().equalsIgnoreCase(value)).findAny().orElse(AA.OFF);
		}

	}

}
