package org.snowjak.rays.transform;

import org.snowjak.rays.builder.Builder;

/**
 * Denotes that a Builder can build an object that itself implements
 * {@link Transformable}
 * 
 * @author snowjak
 *
 * @param <T>
 *            any type that implements Transformable
 */
public interface TransformableBuilder<T extends Transformable> extends Builder<T> {

	/**
	 * Add a {@link Transformer} to this in-progress object
	 * 
	 * @param transformer
	 * @return this Builder, for method-chaining
	 */
	public TransformableBuilder<T> transform(Transformer transformer);
}
