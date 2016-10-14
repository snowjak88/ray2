package org.snowjak.rays.ui;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.world.World;

/**
 * Represents the raytracer's output at the UI-level. Interfaces with a
 * {@link Camera} to render the current {@link World}.
 * 
 * @author snowjak88
 *
 */
public abstract class BasicScreen implements HasScreenDimensions {

	private int screenMinX, screenMinY, screenMaxX, screenMaxY;

	/**
	 * Create a new Screen with the given extent.
	 * 
	 * @param screenMaxX
	 * @param screenMaxY
	 */
	public BasicScreen(int screenMaxX, int screenMaxY) {
		this(0, 0, screenMaxX, screenMaxY);
	}

	/**
	 * Create a new Screen with the given extent.
	 * 
	 * @param screenMinX
	 * @param screenMinY
	 * @param screenMaxX
	 * @param screemMaxY
	 * @param screenMaxY
	 */
	public BasicScreen(int screenMinX, int screenMinY, int screenMaxX, int screemMaxY) {
		this.screenMinX = screenMinX;
		this.screenMinY = screenMinY;
		this.screenMaxX = screenMaxX;
		this.screenMaxY = screemMaxY;
	}

	@Override
	public int getScreenMinX() {

		return screenMinX;
	}

	@Override
	public int getScreenMinY() {

		return screenMinY;
	}

	@Override
	public int getScreenMaxX() {

		return screenMaxX;
	}

	@Override
	public int getScreenMaxY() {

		return screenMaxY;
	}

}
