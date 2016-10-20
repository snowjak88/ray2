package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * A simple implementation of {@link CompositingLightingModel} that evaluates
 * each of its child {@link LightingModel}s and aggregates each result with
 * addition.
 * 
 * @author snowjak88
 *
 */
public class AdditiveCompositingLightingModel extends CompositingLightingModel {

	/**
	 * Create a new {@link AdditiveCompositingLightingModel} with no initial
	 * children.
	 */
	public AdditiveCompositingLightingModel() {
		super();
	}

	/**
	 * Create a new {@link AdditiveCompositingLightingModel} with the initial
	 * list of children.
	 * 
	 * @param children
	 */
	public AdditiveCompositingLightingModel(Collection<LightingModel> children) {
		super(children);
	}

	/**
	 * Create a new {@link AdditiveCompositingLightingModel} with the initial
	 * list of children.
	 * 
	 * @param children
	 */
	public AdditiveCompositingLightingModel(LightingModel... children) {
		super(children);
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		List<LightingResult> childResults = getChildren().parallelStream()
				.map(lm -> lm.determineRayColor(ray, intersection))
				.filter(o -> o.isPresent())
				.map(o -> o.get())
				.collect(Collectors.toCollection(LinkedList::new));

		if (childResults.isEmpty())
			return Optional.empty();

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setPoint(childResults.get(0).getPoint());
		result.setNormal(childResults.get(0).getNormal());
		result.setRadiance(childResults.parallelStream().map(lr -> lr.getRadiance()).reduce(new RawColor(),
				(c1, c2) -> c1.add(c2)));
		result.getContributingResults().addAll(childResults.parallelStream()
				.map(lr -> new Pair<>(lr, 1d))
				.collect(Collectors.toCollection(LinkedList::new)));

		return Optional.of(result);

	}

}
