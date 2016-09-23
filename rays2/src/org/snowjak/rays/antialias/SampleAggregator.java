package org.snowjak.rays.antialias;

import java.util.Collection;

/**
 * A SampleAggregator is something that can take a collection of samples and
 * produce a single aggregated result.
 * 
 * @author snowjak88
 * @param <P>
 *            the sample-point type
 * @param <S>
 *            the sample type
 * @param <R>
 *            the result type
 *
 */
@FunctionalInterface
public interface SampleAggregator<P, S, R> {

	/**
	 * Given a collection of sample-points and another of samples, aggregate the
	 * samples into a single result.
	 * 
	 * @param samplePoints
	 * @param samples
	 * @return the aggregated samples
	 */
	public R aggregate(Collection<P> samplePoints, Collection<S> samples);
}
