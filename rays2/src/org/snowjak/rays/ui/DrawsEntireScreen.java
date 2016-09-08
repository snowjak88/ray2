package org.snowjak.rays.ui;

import org.snowjak.rays.camera.Camera;

import javafx.scene.paint.Color;

/**
 * Indicates that this object has the capability to draw the entire screen.
 * 
 * @see BasicScreen
 * @author snowjak88
 *
 */
public interface DrawsEntireScreen extends CanBeShutdown {

	/**
	 * Draw (or redraw) the entire screen. Every pixel in (screenMinX,
	 * screenMinY) - (screenMaxX, screenMaxY) is iterated across; the Camera is
	 * queried for each, and {@link #drawPixel(int, int, Color)} executed.
	 * 
	 * @param camera
	 */
	public void draw(Camera camera);
}
