package org.snowjak.rays;

/**
 * Represents the raytracer's global context.
 * 
 * @author snowjak88
 *
 */
public class RaytracerContext {

	private static RaytracerContext INSTANCE = null;

	private Renderer currentRenderer = new Renderer();

	private World currentWorld = null;

	protected RaytracerContext() {

	}

	/**
	 * @return the singleton {@link RaytracerContext} instance
	 */
	public static RaytracerContext getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new RaytracerContext();
		return INSTANCE;
	}

	/**
	 * @return the {@link Renderer} currently in-use
	 */
	public Renderer getCurrentRenderer() {

		return currentRenderer;
	}

	/**
	 * Set the {@link Renderer} instance to use
	 * 
	 * @param currentRenderer
	 */
	public void setCurrentRenderer(Renderer currentRenderer) {

		this.currentRenderer = currentRenderer;
	}

	/**
	 * @return the {@link World} instance currently in-use
	 */
	public World getCurrentWorld() {

		return currentWorld;
	}

	/**
	 * Set the {@link World} instance to use
	 * 
	 * @param currentWorld
	 */
	public void setCurrentWorld(World currentWorld) {

		this.currentWorld = currentWorld;
	}
}
