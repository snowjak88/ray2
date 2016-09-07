package org.snowjak.rays;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.ui.DrawsEntireScreen;

/**
 * Represents the origination-point for all scene-rendering.
 * 
 * @author rr247200
 *
 */
public interface Renderer {

	/**
	 * @return the {@link DrawsEntireScreen} instance associated with this
	 *         Renderer
	 */
	public DrawsEntireScreen getScreenDrawer();

	/**
	 * Render the {@link World}, as seen by the given {@link Camera}, to the
	 * associated screen.
	 * 
	 * @param camera
	 */
	public default void render(Camera camera) {

		getScreenDrawer().draw(camera);
	}
}
