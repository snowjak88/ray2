package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		return getChildren().parallelStream()
				.map(lm -> lm.determineRayColor(ray, intersections))
				.filter(o -> o.isPresent())
				.reduce(Optional.of(new RawColor()), (o1, o2) -> Optional.of(o1.get().add(o2.get())));
	}

}
