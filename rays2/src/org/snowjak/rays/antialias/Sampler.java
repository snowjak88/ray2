package org.snowjak.rays.antialias;

import org.snowjak.rays.Ray;
import org.snowjak.rays.shape.Shape;

/**
 * A Sampler is something that will sample some domain, based on the given
 * sample-point (or -vector, or -descriptor).
 * <p>
 * For example: you might define a Sampler that samples a RawColor from the
 * world, given a {@link Ray}, a collection of {@link Shape}s, and a
 * {@link LightingModel}.
 * </p>
 * 
 * @author snowjak88
 *
 * @param <P>
 *            the sample-point type
 * @param <S>
 *            the resulting sample type
 */
@FunctionalInterface
public interface Sampler<P, S> {

	/**
	 * Sample the target domain using the given sample-point
	 * 
	 * @param samplePoint
	 * @return the resulting sample
	 */
	public S sample(P samplePoint);
}
