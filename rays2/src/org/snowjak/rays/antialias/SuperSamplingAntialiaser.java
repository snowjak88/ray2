package org.snowjak.rays.antialias;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * An Antialiaser is an algorithm for performing super-sampling antialiasing.
 * <p>
 * Super-sampling antialiasing is composed of 3 separate pieces:
 * <ol>
 * <li>something to sample</li>
 * <li>a collection of samples</li>
 * <li>an aggregated and smoothed result</li>
 * </ol>
 * Accordingly, such antialiaising consists of using 3 different components:
 * <ol>
 * <li>a sample-point selector</li>
 * <li>a sampler</li>
 * <li>a sample-aggregator</li>
 * </ol>
 * </p>
 * <p>
 * There are at most 3 different types to be used when antialiasing:
 * <ol>
 * <li>the sample-point type -- the type defining where/how each sample is
 * taken</li>
 * <li>the sample type -- the type for the values produced by the sampler</li>
 * <li>the result type -- the type for the end-result produced by the
 * antialiaser</li>
 * </ol>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class SuperSamplingAntialiaser {

	/**
	 * Execute this {@link SuperSamplingAntialiaser} for the given central
	 * sample-point:
	 * <ol>
	 * <li>Create a list of sample-points to use (via the provided
	 * {@link SamplePointSelector})</li>
	 * <li>Determine the consequent list of samples ({@link Sampler})</li>
	 * <li>Aggregate these samples into a single result
	 * ({@link SampleAggregator})</li>
	 * </ol>
	 * 
	 * @param centralSamplePoint
	 * @param sampleSelector
	 * @param sampler
	 * @param sampleAggregator
	 * @return the antialiased result
	 */
	public static <P, S, R> R execute(P centralSamplePoint, SamplePointSelector<P> sampleSelector,
			Sampler<P, S> sampler, SampleAggregator<P, S, R> sampleAggregator) {

		Collection<P> samplePoints = sampleSelector.selectAround(centralSamplePoint);
		Collection<S> samples = samplePoints.parallelStream()
				.map(p -> sampler.sample(p))
				.collect(Collectors.toCollection(LinkedList::new));
		return sampleAggregator.aggregate(samplePoints, samples);
	}
}
