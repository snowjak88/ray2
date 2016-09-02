package org.snowjak.rays.ui;

import java.util.Optional;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

/**
 * Indicates that this object has the capability to determine the color for, and
 * draw, a single pixel on the screen.
 * 
 * @author rr247200
 * @see Screen
 *
 */
public interface DrawsScreenPixel extends CanBeShutdown {

	/**
	 * @param screenX
	 * @param screenY
	 * @return the computed RawColor, if present, for the corresponding screen
	 *         location
	 */
	public Optional<RawColor> getRayColor(int screenX, int screenY);

	/**
	 * Draw the given Color to the Screen at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public void drawPixel(int x, int y, RawColor color);

	/**
	 * @return this Screen's current Camera
	 */
	public Camera getCamera();

	/**
	 * @return the screen's minimum X-coordinate
	 */
	public int getScreenMinX();

	/**
	 * @return the screen's minimum Y-coordinate
	 */
	public int getScreenMinY();

	/**
	 * @return the screen's maximum X-coordinate
	 */
	public int getScreenMaxX();

	/**
	 * @return the screen's maximum Y-coordinate
	 */
	public int getScreenMaxY();

	/**
	 * @param screenX
	 * @return the given pixel X-coordinate translated into an equivalent
	 *         coordinate within the frame of the current {@link Camera}
	 */
	public default double getCameraX(int screenX) {

		double cameraScaleX = getCamera().getCameraFrameSideLength() / (getScreenMaxX() - getScreenMinX());
		return ((double) screenX - (getScreenMaxX() - getScreenMinX()) / 2.0 + getScreenMinX()) * cameraScaleX;
	}

	/**
	 * @param screenY
	 * @return the given pixel Y-coordinate translated into an equivalent
	 *         coordinate within the frame of the current {@link Camera}
	 */
	public default double getCameraY(int screenY) {

		double aspectRatio = (double) (getScreenMaxX() - getScreenMinX())
				/ (double) (getScreenMaxY() - getScreenMinY());
		double cameraScaleY = getCamera().getCameraFrameSideLength() / (getScreenMaxY() - getScreenMinY())
				/ aspectRatio;
		return ((double) screenY - (getScreenMaxY() - getScreenMinY()) / 2.0 + getScreenMinY()) * cameraScaleY;
	}
}
