package org.snowjak.rays.builder;

/**
 * An object builder.
 * 
 * @author snowjak88
 *
 * @param <T>
 *            the type of object to build
 */
public interface Builder<T> {

	/**
	 * Finalize the object-build process and return the finished object.
	 * 
	 * @return the finished object
	 */
	public T build();
}
