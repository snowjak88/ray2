package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
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
	public Optional<RawColor> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();
		//
		//
		Optional<RawColor> shapeColor = child.determineRayColor(ray, intersections);
		//
		//
		if (!shapeColor.isPresent())
			return Optional.empty();
		//
		//
		double reflectivity = intersections.get(0)
				.getDiffuseColorScheme()
				.getReflectivity(intersections.get(0).getPoint());
		if (Double.compare(reflectivity, 0d) <= 0)
			return shapeColor;
		//
		//
		RawColor shapeOriginalColor = shapeColor
				.orElse(intersections.get(0).getDiffuseColorScheme().getColor(intersections.get(0).getPoint()));
		RawColor shapeReflectionColor = getReflectiveShapeDiffuseColor(intersections.get(0)).orElse(new RawColor());

		shapeOriginalColor = shapeOriginalColor.multiplyScalar(1d - reflectivity);
		shapeReflectionColor.multiplyScalar(reflectivity);

		return Optional.of(shapeOriginalColor.add(shapeReflectionColor));

	}

	private Optional<RawColor> getReflectiveShapeDiffuseColor(Intersection<Shape> intersection) {

		double shapeReflectivity = intersection.getDiffuseColorScheme().getReflectivity(intersection.getPoint());

		Ray originalRay = intersection.getRay();

		if (originalRay.getRecursiveLevel() >= World.getSingleton().getMaxRayRecursion() || Double.compare(shapeReflectivity, 0d) <= 0)
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
