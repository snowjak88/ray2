package org.snowjak.rays;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.ui.CanBeShutdown;

/**
 * Represents the raytracer's global context.
 * 
 * @author snowjak88
 *
 */
public class RaytracerContext implements CanBeShutdown {

	private static RaytracerContext INSTANCE = null;

	private Renderer currentRenderer = new Renderer();

	private World currentWorld = null;

	private int workerThreadCount = FastMath.max(Runtime.getRuntime().availableProcessors() - 1, 1);

	private ThreadPoolExecutor workerThreadPool = null;

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

	/**
	 * @return the selected number of worker-threads to use while rendering
	 */
	public int getWorkerThreadCount() {

		return workerThreadCount;
	}

	/**
	 * Set the desired number of worker-threads to use
	 * 
	 * @param workerThreadCount
	 */
	public void setWorkerThreadCount(int workerThreadCount) {

		this.workerThreadCount = workerThreadCount;
	}

	/**
	 * @return the world's pool of available worker-threads
	 */
	public ThreadPoolExecutor getWorkerThreadPool() {

		if (workerThreadPool == null)
			this.workerThreadPool = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(FastMath.max(workerThreadCount, 1));

		return workerThreadPool;
	}

	@Override
	public void shutdown() {

		currentRenderer.shutdown();

		if (!this.workerThreadPool.shutdownNow().isEmpty())
			System.out.println("Shutting down worker threads ...");
	}
}
