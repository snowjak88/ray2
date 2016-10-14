package org.snowjak.rays;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.light.model.FlatLightingModel;
import org.snowjak.rays.light.model.LightingModel;
import org.snowjak.rays.ui.AntialiasingScreenDecorator;
import org.snowjak.rays.ui.CanBeShutdown;
import org.snowjak.rays.ui.MultithreadedScreenDecorator;
import org.snowjak.rays.ui.PixelDrawer;
import org.snowjak.rays.ui.ScreenDrawer;
import org.snowjak.rays.world.World;

/**
 * Represents the origination-point for all scene-rendering.
 * 
 * @author snowjak88
 *
 */
public class Renderer implements CanBeShutdown {

	private ScreenDrawer rootScreenDrawer = null;

	private LightingModel lightingModel = new FlatLightingModel();

	/**
	 * Create a new Renderer that does <strong>nothing</strong> -- a "data
	 * sink", if you will.
	 */
	public Renderer() {
	}

	/**
	 * Create a new Renderer, associated with the specified {@link PixelDrawer}
	 * implementation.
	 * <p>
	 * You must supply the implementation-specific pixel-drawer. This Renderer
	 * will construct the rest of the screen-drawing toolchain automatically.
	 * (See {@link #getDefaultScreenDrawer(PixelDrawer)}).
	 * </p>
	 * 
	 * @param pixelDrawerImpl
	 * @param settings
	 */
	public Renderer(PixelDrawer pixelDrawerImpl) {
		this.rootScreenDrawer = getDefaultScreenDrawer(pixelDrawerImpl);
	}

	/**
	 * Create a new Renderer using only the specified screen-drawing toolchain.
	 * <p>
	 * Unlike {@link #Renderer(PixelDrawer, Settings)}, this constructor will
	 * add none of the default drawing toolchain to your supplied
	 * drawing-instance. Use this constructor if you need to specify an
	 * non-default drawing toolchain.
	 * </p>
	 * 
	 * @param drawToolchain
	 * @param settings
	 */
	public Renderer(ScreenDrawer drawToolchain) {
		this.rootScreenDrawer = drawToolchain;
	}

	/**
	 * @return the {@link LightingModel} currently in-use
	 */
	public LightingModel getLightingModel() {

		return lightingModel;
	}

	/**
	 * Set the {@link LightingModel} implementation to use.
	 * 
	 * @param lightingModel
	 */
	public void setLightingModel(LightingModel lightingModel) {

		this.lightingModel = lightingModel;
	}

	/**
	 * Render the {@link World}, as seen by the given {@link Camera}, to the
	 * associated screen.
	 * 
	 * @param camera
	 */
	public void render(Camera camera) {

		if (rootScreenDrawer != null)
			rootScreenDrawer.draw(camera);
	}

	@Override
	public void shutdown() {

		if (rootScreenDrawer != null)
			rootScreenDrawer.shutdown();
	}

	/**
	 * Construct the default screen-drawing "drawchain":
	 * <ol>
	 * <li>{@link MultithreadedScreenDecorator}</li>
	 * <li>{@link AntialiasingScreenDecorator}</li>
	 * <li>(your {@link PixelDrawer} implementation)</li>
	 * </ol>
	 * 
	 * @param pixelDrawerImpl
	 * @return the constructed drawchain
	 */
	public static ScreenDrawer getDefaultScreenDrawer(PixelDrawer pixelDrawerImpl) {

		return new MultithreadedScreenDecorator(new AntialiasingScreenDecorator(pixelDrawerImpl));
	}

}
