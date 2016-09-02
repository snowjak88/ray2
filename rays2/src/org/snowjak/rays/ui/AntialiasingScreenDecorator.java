package org.snowjak.rays.ui;

import java.util.Optional;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

/**
 * Implements a simple antialiasing filter on top of an existing
 * {@link DrawsScreenPixel} implementation.
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
 * @author rr247200
 *
 */
public class AntialiasingScreenDecorator implements DrawsScreenPixel {

	private DrawsScreenPixel child;

	private double coordinateDelta;

	private double filterSpan;

	/**
	 * Create a new AntialiasingScreenDecorator on top of an existing
	 * {@link DrawsScreenPixel} instance.
	 * 
	 * @param decoratedScreen
	 *            the existing screen to decorate
	 * @param filterSpan
	 *            the side-length of the antialiasing box-filter
	 * @param sampleCount
	 *            a count of the points sampled within the antialiasing box.
	 *            Always rounded up to N^2 + 1
	 */
	public AntialiasingScreenDecorator(DrawsScreenPixel decoratedScreen, double filterSpan, int sampleCount) {

		this.child = decoratedScreen;
		this.filterSpan = filterSpan;
		this.coordinateDelta = filterSpan / ((double) FastMath.rint(FastMath.ceil(FastMath.sqrt(sampleCount))) + 1d);
	}

	@Override
	public Optional<RawColor> getRayColor(int screenX, int screenY) {

		double centralX = getCameraX(screenX), centralY = getCameraY(screenY);

		RawColor totalColor = new RawColor();
		double totalScale = 0d;

		for (double dx = -(filterSpan / 2d); dx <= (filterSpan / 2d); dx += coordinateDelta) {
			for (double dy = -(filterSpan / 2d); dy <= (filterSpan / 2d); dy += coordinateDelta) {

				double x = centralX + dx, y = centralY + dy;
				// double dxpi = dx * FastMath.PI, dypi = dy * FastMath.PI;
				double scale = 10d * (FastMath.sin(dx) / (dx)) * (FastMath.sin(dy) / (dy));

				totalScale += scale;
				Optional<RawColor> color = child.getCamera().shootRay(x, y);
				if (color.isPresent())
					totalColor = totalColor.add(color.get().multiplyScalar(scale));
			}
		}

		totalColor = totalColor.multiplyScalar(1d / totalScale);
		return Optional.of(totalColor);
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
	public Camera getCamera() {

		return child.getCamera();
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

}
