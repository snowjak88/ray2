package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Adds functionality to do some simple recursive reflecting. Uses the shape's
 * diffuse {@link ColorScheme#getReflectivity()} to retrieve the appropriate
 * <strong>reflectivity-fraction</strong>, which in turn determines how much of
 * the reflected light is kept vs. the decorated LightingModel's light.
 * 
 * @author snowjak88
 *
 */
public class ReflectionsDecoratingLightingModel implements LightingModel {

	private LightingModel child;

	/**
	 * Construct a new {@link ReflectionsDecoratingLightingModel}, decorating
	 * the specified LightingModel
	 * 
	 * @param decoratedLightingModel
	 */
	public ReflectionsDecoratingLightingModel(LightingModel decoratedLightingModel) {
		this.child = decoratedLightingModel;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();
		//
		//
		Optional<LightingResult> decoratedResult = child.determineRayColor(ray, intersections);
		//
		//
		if (!decoratedResult.isPresent())
			return Optional.empty();
		//
		//
		double reflectivity = intersections.get(0).getDiffuseColorScheme()
				.getReflectivity(intersections.get(0).getPoint());
		if (Double.compare(reflectivity, 0d) <= 0)
			return decoratedResult;
		//
		//
		LightingResult finalResult = new LightingResult();
		finalResult.setPoint(intersections.get(0).getPoint());
		finalResult.setNormal(intersections.get(0).getNormal());
		finalResult.setEye(intersections.get(0).getRay().getVector());

		finalResult.getContributingResults().add(new Pair<>(decoratedResult.get(), 1d - reflectivity));
		finalResult.setRadiance(decoratedResult.get().getRadiance().multiplyScalar(1d - reflectivity));

		Optional<LightingResult> reflectedResult = getReflectiveShapeDiffuseColor(intersections.get(0));
		if (reflectedResult.isPresent()) {
			finalResult.getContributingResults().add(new Pair<>(reflectedResult.get(), reflectivity));
			finalResult.setRadiance(
					finalResult.getRadiance().add(reflectedResult.get().getRadiance().multiplyScalar(reflectivity)));
		}

		return Optional.of(finalResult);

	}

	private Optional<LightingResult> getReflectiveShapeDiffuseColor(Intersection<Shape> intersection) {

		double shapeReflectivity = intersection.getDiffuseColorScheme().getReflectivity(intersection.getPoint());

		Ray originalRay = intersection.getRay();

		if (originalRay.getRecursiveLevel() >= World.getSingleton().getMaxRayRecursion()
				|| Double.compare(shapeReflectivity, 0d) <= 0)
			return Optional.empty();

		Vector3D intersectPoint = intersection.getPoint();
		Vector3D eyeVector = intersection.getRay().getOrigin().subtract(intersectPoint);
		Vector3D reflectedEyeVector = getReflection(eyeVector.negate().normalize(), intersection.getNormal())
				.normalize();

		Ray reflectedRay = new Ray(intersectPoint, reflectedEyeVector, originalRay.getRecursiveLevel() + 1);
		List<Intersection<Shape>> intersections = World.getSingleton().getShapeIntersections(reflectedRay);
		return World.getSingleton().getLightingModel().determineRayColor(reflectedRay, intersections);

	}

	private Vector3D getReflection(Vector3D v, Vector3D normal) {

		return v.subtract(normal.scalarMultiply(2d * v.dotProduct(normal)));
	}

}
