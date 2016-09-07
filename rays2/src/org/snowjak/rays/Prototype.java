package org.snowjak.rays;

/**
 * Signifies that an object of this type can act as a prototype: i.e., can be
 * instantiated, configured, and then copied an infinite number of times while
 * retaining that configuration.
 * 
 * @author rr247200
 *
 * @param <T>
 */
public interface Prototype<T> {

	/**
	 * Copy this object, replicating its configuration into the new instance.
	 * 
	 * @return a copy of this object
	 */
	public T copy();
}
