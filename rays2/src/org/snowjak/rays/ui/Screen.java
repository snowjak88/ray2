package org.snowjak.rays.ui;

import java.util.Optional;

import org.snowjak.rays.World;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

import javafx.scene.paint.Color;

/**
 * Represents the raytracer's output at the UI-level. Interfaces with a
 * {@link Camera} to render the current {@link World}.
 * 
 * @author rr247200
 *
 */
public abstract class Screen {

	private Camera camera;

	private double cameraOffsetX, cameraOffsetY, cameraScaleX, cameraScaleY;

	private int screenMinX, screenMinY, screenMaxX, screenMaxY;

	/**
	 * Create a new Screen with the given extent.
	 * 
	 * @param screenMaxX
	 * @param screenMaxY
	 */
	public Screen(int screenMaxX, int screenMaxY) {
		this(null, screenMaxX, screenMaxY);
	}

	/**
	 * Create a new Screen with the given extent, attached to the given Camera.
	 * 
	 * @param camera
	 * @param screenMaxX
	 * @param screenMaxY
	 */
	public Screen(Camera camera, int screenMaxX, int screenMaxY) {
		this(camera, 0, 0, screenMaxX, screenMaxY);
	}

	/**
	 * Create a new Screen with the given extent, attached to the given Camera.
	 * 
	 * @param camera
	 * @param screenMinX
	 * @param screenMinY
	 * @param screenMaxX
	 * @param screemMaxY
	 * @param screenMaxY
	 */
	public Screen(Camera camera, int screenMinX, int screenMinY, int screenMaxX, int screemMaxY) {
		this.screenMinX = screenMinX;
		this.screenMinY = screenMinY;
		this.screenMaxX = screenMaxX;
		this.screenMaxY = screemMaxY;

		setCamera(camera);
	}

	/**
	 * Draw (or redraw) the entire screen. Every pixel in (screenMinX,
	 * screenMinY) - (screenMaxX, screenMaxY) is iterated across; the Camera is
	 * queried for each, and {@link #drawPixel(int, int, Color)} executed.
	 */
	public void draw() {

		if (camera != null)
			for (int x = screenMinX; x <= screenMaxX; x++)
				for (int y = screenMinY; y <= screenMaxY; y++) {
					Optional<RawColor> color = camera.shootRay(getCameraX(x), getCameraY(y));
					if (color.isPresent())
						drawPixel(x, screenMaxY - y + screenMinY, color.get());
				}

	}

	/**
	 * @param screenX
	 * @return the given pixel X-coordinate translated into an equivalent
	 *         coordinate within the frame of the current {@link Camera}
	 */
	public double getCameraX(int screenX) {

		return ((double) screenX - cameraOffsetX) * cameraScaleX;
	}

	/**
	 * @param screenY
	 * @return the given pixel Y-coordinate translated into an equivalent
	 *         coordinate within the frame of the current {@link Camera}
	 */
	public double getCameraY(int screenY) {

		return ((double) screenY - cameraOffsetY) * cameraScaleY;
	}

	/**
	 * Draw the given Color to the Screen at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public abstract void drawPixel(int x, int y, RawColor color);

	/**
	 * A method-stub, to be called when the application should shut down. Useful
	 * as a hook to shut down any rendering-related tasks -- e.g., killing
	 * render-worker threads.
	 */
	public void shutdown() {

	}

	/**
	 * Set the {@link Camera} this Screen is associated with.
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {

		this.camera = camera;

		if (camera != null) {
			this.cameraOffsetX = (screenMaxX - screenMinX) / 2.0 + screenMinX;
			this.cameraOffsetY = (screenMaxY - screenMinY) / 2.0 + screenMinY;
			this.cameraScaleX = camera.getCameraFrameWidth() / (screenMaxX - screenMinX);
			this.cameraScaleY = camera.getCameraFrameHeight() / (screenMaxY - screenMinY);
		} else {
			this.cameraOffsetX = 0;
			this.cameraOffsetY = 0;
			this.cameraScaleX = 0;
			this.cameraScaleY = 0;
		}
	}

	/**
	 * @return this Screen's current Camera
	 */
	public Camera getCamera() {

		return camera;
	}

	/**
	 * @return the screen's minimum X-coordinate
	 */
	public int getScreenMinX() {

		return screenMinX;
	}

	/**
	 * @return the screen's minimum Y-coordinate
	 */
	public int getScreenMinY() {

		return screenMinY;
	}

	/**
	 * @return the screen's maximum X-coordinate
	 */
	public int getScreenMaxX() {

		return screenMaxX;
	}

	/**
	 * @return the screen's maximum Y-coordinate
	 */
	public int getScreenMaxY() {

		return screenMaxY;
	}

}
