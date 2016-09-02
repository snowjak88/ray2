package org.snowjak.rays.ui;

/**
 * Indicates that this object has resources -- open files, background threads,
 * etc. -- which should be retired when the application is shutting down.
 * 
 * @author rr247200
 *
 */
public interface CanBeShutdown {

	/**
	 * To be called when the application is being shut down. Useful as a hook to
	 * shut down any rendering-related tasks -- e.g., killing render-worker
	 * threads.
	 */
	public void shutdown();
}
