package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.shape.Shape;

/**
 * Implements the Phong specular-reflection model.
 * 
 * @author snowjak88
 *
 */
public class PhongSpecularLightingModel implements LightingModel {

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		//
		//
		// We assume that the list of intersections is sorted by ascending
		// distance.
		// And we only care about the first (closest) intersection that isn't
		// too close.
		Optional<Intersection<Shape>> firstIntersect = intersections.stream()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0).findFirst();
		if (!firstIntersect.isPresent())
			return Optional.empty();
		Intersection<Shape> intersect = firstIntersect.get();
		//
		// What are the configured colors for this shape?
		RawColor intersectSpecularColor = intersect.getSpecular(intersect.getPoint());
		double shininess = intersect.getSpecularColorScheme().getShininess(intersect.getPoint());

		//
		// totalX = total light of type X seen by this Ray
		RawColor totalSpecular = new RawColor();

		//
		//
		// The total light received by this ray is partially a function of all
		// the lights in the world ...
		for (Light light : World.getSingleton().getLights()) {

			//
			// Where does this light lie in relation to the intersection?
			Vector3D lightLocation = light.getLocation();
			Vector3D toLightVector = lightLocation.subtract(intersect.getPoint());
			//
			// toLightRay == ray from the intersection-point to the light.
			Ray toLightRay = new Ray(intersect.getPoint(), toLightVector.normalize());

			//
			//
			// Ambient light, of course, is "ambient" -- it is received
			// throughout the world regardless of
			// other objects.
			// Diffuse and specular light, on the other hand, can be occluded.
			// We need to check for object-occlusion before calculating diffuse
			// and specular light.
			//
			// To check for object-occlusion, we cast a ray from the
			// intersection-point to the light. If we run into any shape along
			// the way, then that light is not visible from the
			// intersection-point.
			//
			boolean lightIsVisible = true;
			List<Intersection<Shape>> toLightIntersections = World.getSingleton().getShapeIntersections(toLightRay);
			//
			// Notice that we deliberately exclude any intersections that are
			// "too close".
			// This is meant to help exclude reported intersections that are
			// really identical with the current intersection, double-reported
			// because of double-value uncertainty.
			if (toLightIntersections.stream()
					.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), toLightVector.getNorm()) < 0)
					.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0))
				lightIsVisible = false;

			if (lightIsVisible) {
				//
				//
				// Calculate the diffuse light the current Light contributes to
				// this ray
				double lightExposure = light.getExposure(intersect);
				if (Double.compare(lightExposure, 0d) > 0) {

					//
					//
					// Calculate the specular light the current Light
					// contributes to this ray

					//
					// toEyeVector == the direction from the eye to the
					// intersection-point.
					Vector3D fromEyeVector = intersect.getRay().getVector();

					//
					// reflectedLightVector = the vector from the light,
					// reflecting off the surface at the intersection-point
					Vector3D reflectedLightVector = getReflection(toLightVector, intersect.getNormal()).normalize();

					double specularDotProduct = reflectedLightVector.dotProduct(fromEyeVector.normalize());
					if (Double.compare(specularDotProduct, 0d) > 0) {
						double specularIntensity = FastMath.pow(specularDotProduct, shininess);
						RawColor lightSpecularIntensity = light.getSpecularIntensity(toLightRay)
								.multiplyScalar(specularIntensity);

						totalSpecular = totalSpecular.add(intersectSpecularColor.multiply(lightSpecularIntensity));
					}
				}

			}
		}

		//
		//
		// Finally, combine all the different kinds of light into a single
		// total.
		RawColor totalColor = totalSpecular;

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setNormal(intersect.getNormal());
		result.setPoint(intersect.getPoint());
		result.setRadiance(totalColor);

		return Optional.of(result);
	}

	private Vector3D getReflection(Vector3D v, Vector3D normal) {

		return v.subtract(normal.scalarMultiply(2d * v.dotProduct(normal)));
	}

}
