package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Implements a simple indirect-diffuse-illlumination LightingModel using
 * Monte-Carlo sampling.
 * 
 * @author snowjak88
 *
 */
public class MonteCarloDiffuseIlluminationLightingModel implements LightingModel {

	private Random rnd = new Random();

	private int samplesPerPoint;

	/**
	 * Construct a new {@link MonteCarloDiffuseIlluminationLightingModel},
	 * configured to use the default number of sample-rays per intersection
	 * (i.e., 8).
	 */
	public MonteCarloDiffuseIlluminationLightingModel() {
		this(8);
	}

	/**
	 * Construct a new {@link MonteCarloDiffuseIlluminationLightingModel},
	 * configured to use the specified number of sample-rays per intersection.
	 * 
	 * @param samplesPerPoint
	 */
	public MonteCarloDiffuseIlluminationLightingModel(int samplesPerPoint) {
		this.samplesPerPoint = samplesPerPoint;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		if (ray.getRecursiveLevel() >= World.getSingleton().getMaxRayRecursion())
			return Optional.empty();

		Intersection<Shape> intersect = intersections.get(0);
		RawColor totalDiffuseLight = new RawColor();

		for (int i = 0; i < samplesPerPoint; i++) {

			Vector3D samplingDirection = getVectorInHemisphere(intersect.getNormal());
			Ray samplingRay = new Ray(intersect.getPoint(), samplingDirection, ray.getRecursiveLevel() + 1);

			List<Intersection<Shape>> sampledIntersections = World.getSingleton().getShapeIntersections(samplingRay);
			Optional<LightingResult> sampledLight = World.getSingleton()
					.getLightingModel()
					.determineRayColor(samplingRay, sampledIntersections);
			RawColor sampledColor = sampledLight.orElse(new LightingResult()).getRadiance();
			totalDiffuseLight = totalDiffuseLight
					.add(sampledColor.multiplyScalar(samplingDirection.dotProduct(intersect.getNormal())));

		}

		totalDiffuseLight = totalDiffuseLight.multiplyScalar(1d / (double) samplesPerPoint);

		RawColor surfaceColor = intersect.getDiffuse(intersect.getPoint());

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setNormal(intersect.getNormal());
		result.setPoint(intersect.getPoint());
		result.setRadiance(surfaceColor.multiply(totalDiffuseLight));
		return Optional.of(result);
	}

	private Vector3D getVectorInHemisphere(Vector3D normal) {

		Vector3D i = normal.orthogonal(), j = normal, k = normal.crossProduct(i);
		Vector3D result = Vector3D.ZERO;
		do {

			double x = rnd.nextGaussian(), y = rnd.nextDouble(), z = rnd.nextGaussian();
			result = i.scalarMultiply(x).add(j.scalarMultiply(y)).add(k.scalarMultiply(z));

		} while (Double.compare(result.getNorm(), 0d) == 0);

		return result.normalize();
	}

}
