package org.snowjak.rays;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.ui.AntialiasingScreenDecorator;
import org.snowjak.rays.ui.AntialiasingScreenDecorator.AA;
import org.snowjak.rays.ui.MultithreadedScreenDecorator.RenderSplitType;
import org.snowjak.rays.ui.CanBeShutdown;
import org.snowjak.rays.ui.DrawsEntireScreen;
import org.snowjak.rays.ui.DrawsScreenPixel;
import org.snowjak.rays.ui.MultithreadedScreenDecorator;

/**
 * Represents the origination-point for all scene-rendering.
 * 
 * @author snowjak88
 *
 */
public class Renderer implements CanBeShutdown {

	private DrawsEntireScreen rootScreenDrawer;

	/**
	 * Create a new Renderer, associated with the specified
	 * {@link DrawsScreenPixel} implementation.
	 * <p>
	 * You must supply the implementation-specific pixel-drawer. This Renderer
	 * will construct the rest of the screen-drawing toolchain automatically.
	 * (See {@link #getDefaultScreenDrawer(DrawsScreenPixel)}).
	 * </p>
	 * 
	 * @param pixelDrawerImpl
	 */
	public Renderer(DrawsScreenPixel pixelDrawerImpl) {
		this.rootScreenDrawer = getDefaultScreenDrawer(pixelDrawerImpl);
	}

	/**
	 * Create a new Renderer using only the specified screen-drawing toolchain.
	 * <p>
	 * Unlike {@link #Renderer(DrawsScreenPixel)}, this constructor will add
	 * none of the default drawing toolchain to your supplied drawing-instance.
	 * Use this constructor if you need to specify an non-default drawing
	 * toolchain.
	 * </p>
	 * 
	 * @param drawToolchain
	 */
	public Renderer(DrawsEntireScreen drawToolchain) {
		this.rootScreenDrawer = drawToolchain;
	}

	/**
	 * Render the {@link World}, as seen by the given {@link Camera}, to the
	 * associated screen.
	 * 
	 * @param camera
	 */
	public void render(Camera camera) {

		rootScreenDrawer.draw(camera);
	}

	@Override
	public void shutdown() {

		rootScreenDrawer.shutdown();
	}

	/**
	 * Construct the default screen-drawing "drawchain":
	 * <ol>
	 * <li>{@link MultithreadedScreenDecorator}</li>
	 * <li>{@link AntialiasingScreenDecorator}</li>
	 * <li>(your {@link DrawsScreenPixel} implementation)</li>
	 * </ol>
	 * 
	 * @param pixelDrawerImpl
	 * @return the constructed drawchain
	 */
	public static DrawsEntireScreen getDefaultScreenDrawer(DrawsScreenPixel pixelDrawerImpl) {

		return new MultithreadedScreenDecorator(new AntialiasingScreenDecorator(pixelDrawerImpl));
	}

	/**
	 * A central repository for settings associated with all {@link Renderer}
	 * instances.
	 * <p>
	 * There are several "preset" methods available to quickly configure the
	 * Settings to alternative configurations. See {@link #presetFast()},
	 * {@link #presetDetailed()}.
	 * </p>
	 * <p>
	 * By default, the Settings singleton will use the "fast" preset.
	 * </p>
	 * 
	 * @author snowjak88
	 *
	 */
	public static class Settings {

		private static Settings INSTANCE = null;

		private int imageWidth, imageHeight;

		private AntialiasingScreenDecorator.AA antialiasing;

		private int renderThreadCount;

		private RenderSplitType renderSplitType;

		/**
		 * @return the Settings singleton instance
		 */
		public static Settings getSingleton() {

			if (INSTANCE == null)
				presetFast();
			return INSTANCE;
		}

		protected Settings() {

			presetFast();
		}

		protected Settings(int imageWidth, int imageHeight, AntialiasingScreenDecorator.AA antialiasing,
				int renderThreadCount, RenderSplitType renderSplitType) {
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
			this.antialiasing = antialiasing;
			this.renderThreadCount = renderThreadCount;
			this.renderSplitType = renderSplitType;
		}

		/**
		 * Configures the Settings singleton to use a "fast-render" preset:
		 * <ul>
		 * <li>Image width/height: 400x250</li>
		 * <li>Antialiasing: OFF</li>
		 * <li>Render-thread count: {@link #getDefaultRenderThreadCount()}</li>
		 * </ul>
		 * 
		 * @return the reconfigured Settings instance
		 */
		public static Settings presetFast() {

			INSTANCE = new Settings(400, 250, AA.OFF, getDefaultRenderThreadCount(), RenderSplitType.REGION);
			return INSTANCE;
		}

		/**
		 * Configures the Settings singleton to use a "detailed-render" preset:
		 * <ul>
		 * <li>Image width/height: 800x500</li>
		 * <li>Antialiasing: 8x</li>
		 * <li>Render-thread count: {@link #getDefaultRenderThreadCount()}</li>
		 * </ul>
		 * 
		 * @return the reconfigured Settings instance
		 */
		public static Settings presetDetailed() {

			INSTANCE = new Settings(800, 500, AA.x8, getDefaultRenderThreadCount(), RenderSplitType.REGION);
			return INSTANCE;
		}

		/**
		 * @return the default number of render-threads to use. Equal to
		 *         {@code Runtime#availableProcessors() - 1}
		 */
		public static int getDefaultRenderThreadCount() {

			return Runtime.getRuntime().availableProcessors() - 1;
		}

		/**
		 * @return the width of the rendered image
		 */
		public int getImageWidth() {

			return imageWidth;
		}

		/**
		 * Set the width of the to-be-rendered image
		 * 
		 * @param imageWidth
		 */
		public void setImageWidth(int imageWidth) {

			this.imageWidth = imageWidth;
		}

		/**
		 * @return the height of the rendered image
		 */
		public int getImageHeight() {

			return imageHeight;
		}

		/**
		 * Set the height of the to-be-rendered image
		 * 
		 * @param imageHeight
		 */
		public void setImageHeight(int imageHeight) {

			this.imageHeight = imageHeight;
		}

		/**
		 * @return the selected antialiasing strength
		 */
		public AntialiasingScreenDecorator.AA getAntialiasing() {

			return antialiasing;
		}

		/**
		 * Set the desired antialiasing strength to use when rendering
		 * 
		 * @param antialiasing
		 */
		public void setAntialiasing(AntialiasingScreenDecorator.AA antialiasing) {

			this.antialiasing = antialiasing;
		}

		/**
		 * @return the selected number of worker-threads to use while rendering
		 */
		public int getRenderThreadCount() {

			return renderThreadCount;
		}

		/**
		 * Set the desired number of worker-threads to use while rendering
		 * 
		 * @param renderThreadCount
		 */
		public void setRenderThreadCount(int renderThreadCount) {

			this.renderThreadCount = renderThreadCount;
		}

		/**
		 * @return the selected {@link RenderSplitType} to be used when
		 *         rendering
		 */
		public RenderSplitType getRenderSplitType() {

			return renderSplitType;
		}

		/**
		 * Set the desired {@link RenderSplitType} to be used when rendering
		 * 
		 * @param renderSplitType
		 */
		public void setRenderSplitType(RenderSplitType renderSplitType) {

			this.renderSplitType = renderSplitType;
		}

	}
}
