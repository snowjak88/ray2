package org.snowjak.rays.antialias;

import java.util.Collection;

/**
 * A SampleSelector is something that will provide a collection of sample-points
 * to use when sampling.
 * 
 * @author snowjak88
 * @param <P>
 *            the sample-point type
 *
 */
@FunctionalInterface
public interface SamplePointSelector<P> {

	/**
	 * Create a collection of sample-points, around the given central
	 * sample-point.
	 * 
	 * @param centralPoint
	 * @return a collection of sample-points
	 */
	public Collection<P> selectAround(P centralPoint);
}
