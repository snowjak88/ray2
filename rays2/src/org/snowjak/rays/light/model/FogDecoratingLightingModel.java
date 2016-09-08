package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

import javafx.scene.paint.Color;

/**
 * A Decorator-type LightingModel that enhances another LightingModel with
 * rudimentary fog!
 * 
 * @author snowjak88
 *
 */
public class FogDecoratingLightingModel implements LightingModel {

	private double halfFogDistance;

	private RawColor fogColor;

	private LightingModel decoratedModel;

	public FogDecoratingLightingModel(double halfFogDistance, RawColor fogColor, LightingModel modelToDecorate) {
		this.halfFogDistance = halfFogDistance;
		this.fogColor = fogColor;
		this.decoratedModel = modelToDecorate;
	}

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		Optional<RawColor> rayColor = decoratedModel.determineRayColor(ray, intersections);

		RawColor unfoggedColor;
		if (rayColor.isPresent())
			unfoggedColor = rayColor.get();
		else
			unfoggedColor = new RawColor();

		double colorDistance = 0d;
		if (intersections.isEmpty())
			colorDistance = World.WORLD_BOUND;
		else
			colorDistance = intersections.get(0).getDistanceFromRayOrigin();

		double fogStrength = FastMath.pow(0.5, (colorDistance / halfFogDistance));
		RawColor foggedColor = unfoggedColor.multiplyScalar(fogStrength).add(fogColor.multiplyScalar(1d - fogStrength));
		return Optional.of(foggedColor);
	}

}
