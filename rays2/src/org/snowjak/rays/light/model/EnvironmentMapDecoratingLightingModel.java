package org.snowjak.rays.light.model;

import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * A {@link LightingModel} that implements an environment map at "infinite"
 * distance. Adds environment-mapping capabilities to another
 * {@link LightingModel} implementation.
 * 
 * @author snowjak88
 * @see EnvironmentMap
 *
 */
public class EnvironmentMapDecoratingLightingModel implements LightingModel {

	private LightingModel decoratedLightingModel;

	private EnvironmentMap environmentMap;

	/**
	 * Construct a new {@link EnvironmentMapDecoratingLightingModel}, using the
	 * given {@link EnvironmentMap} and {@link LightingModel} instances.
	 * 
	 * @param environmentMap
	 * @param decoratedLightingModel
	 */
	public EnvironmentMapDecoratingLightingModel(EnvironmentMap environmentMap, LightingModel decoratedLightingModel) {
		this.environmentMap = environmentMap;
		this.decoratedLightingModel = decoratedLightingModel;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		Optional<LightingResult> decoratedColor = decoratedLightingModel.determineRayColor(ray, intersection);

		if (decoratedColor.isPresent())
			return decoratedColor;

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setNormal(ray.getVector().negate());
		result.setPoint(ray.getVector().scalarMultiply(2d * World.FAR_AWAY));
		result.setRadiance(environmentMap.getColorAt(environmentMap.convert(ray.getVector())));

		return Optional.of(result);
	}

	/**
	 * An environment map is an image that represents the incoming light from
	 * "infinite" distance. The 2D image coordinate system (U/V) is capable of
	 * being converted to/from 3D world coordinates (in X/Y/Z).
	 * <p>
	 * Image coordinates are expressed in U/V vectors, where each term is in
	 * [0,1]. World coordinates are expressed as unit-vectors, where each term
	 * -- X,Y,Z -- is in [-1,1].
	 * </p>
	 * 
	 * @author snowjak88
	 *
	 */
	public static interface EnvironmentMap {

		/**
		 * Convert the provided look-vector (in world coordinates) to the
		 * corresponding image location.
		 * 
		 * @param worldVector
		 * @return the equivalent image location
		 */
		public Vector2D convert(Vector3D worldVector);

		/**
		 * Query the environment map and return the color for the given location
		 * 
		 * @param uv
		 * @return the color at the given location
		 */
		public default RawColor getColorAt(Vector2D uv) {

			return getColorAt(uv.getX(), uv.getY());
		}

		/**
		 * Query the environment map and return the color for the given location
		 * 
		 * @param u
		 * @param v
		 * @return the color at the given location
		 */
		public RawColor getColorAt(double u, double v);
	}

	/**
	 * @return the LightingModel which this
	 *         {@link EnvironmentMapDecoratingLightingModel} decorates
	 */
	public LightingModel getDecoratedLightingModel() {

		return decoratedLightingModel;
	}

	/**
	 * @return the {@link EnvironmentMap} instance associated with this
	 *         {@link EnvironmentMapDecoratingLightingModel}
	 */
	public EnvironmentMap getEnvironmentMap() {

		return environmentMap;
	}
}
