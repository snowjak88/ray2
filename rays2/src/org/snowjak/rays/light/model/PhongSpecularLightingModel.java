package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * Implements the Phong specular-reflection model.
 * 
 * @author snowjak88
 *
 */
public class PhongSpecularLightingModel implements LightingModel {

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		//
		//
		// We assume that the list of intersections is sorted by ascending
		// distance.
		// And we only care about the first (closest) intersection that isn't
		// too close.
		Intersection<Shape> intersect = intersection.get();
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
		for (Shape emissiveShape : RaytracerContext.getSingleton().getCurrentWorld().getEmissiveShapes()) {

			//
			// Where does this light lie in relation to the intersection?
			Vector3D emissiveLocation = emissiveShape.getLocation();
			Vector3D toEmissiveVector = emissiveLocation.subtract(intersect.getPoint());
			//
			// toLightRay == ray from the intersection-point to the light.
			Ray toLightRay = new Ray(intersect.getPoint(), toEmissiveVector.normalize());

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
			List<Intersection<Shape>> occludingIntersections = RaytracerContext.getSingleton()
					.getCurrentWorld()
					.getShapeIntersections(toLightRay);
			//
			// Notice that we deliberately exclude any intersections that are
			// "too close".
			// This is meant to help exclude reported intersections that are
			// really identical with the current intersection, double-reported
			// because of double-value uncertainty.
			if (occludingIntersections.parallelStream()
					.filter(i -> i.getIntersected() != emissiveShape)
					.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), toEmissiveVector.getNorm()) < 0)
					.anyMatch(i -> Double.compare(i.getDistanceFromRayOrigin(), World.NEARLY_ZERO) >= 0))
				lightIsVisible = false;

			if (lightIsVisible) {

				Optional<Intersection<Shape>> emissiveSurfaceIntersection = occludingIntersections.parallelStream()
						.filter(i -> i.getIntersected() == emissiveShape)
						.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(),
								i2.getDistanceFromRayOrigin()))
						.findFirst();

				if (emissiveSurfaceIntersection.isPresent()) {

					//
					//
					// Calculate the diffuse light the current Light contributes
					// to
					// this ray
					double emissiveExposure = emissiveSurfaceIntersection.get()
							.getPoint()
							.subtract(intersect.getPoint())
							.normalize()
							.dotProduct(intersect.getNormal());
					if (Double.compare(emissiveExposure, 0d) > 0) {

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
						Vector3D reflectedLightVector = getReflection(toEmissiveVector, intersect.getNormal())
								.normalize();

						double specularDotProduct = reflectedLightVector.dotProduct(fromEyeVector.normalize());

						if (Double.compare(specularDotProduct, 0d) > 0) {
							double specularIntensity = FastMath.pow(specularDotProduct, shininess);

							double falloff = 1d / (4d * FastMath.PI
									* intersect.getPoint().distance(emissiveSurfaceIntersection.get().getPoint()));

							RawColor lightSpecularIntensity = emissiveSurfaceIntersection.get()
									.getEmissive(emissiveSurfaceIntersection.get().getPoint())
									.orElse(new RawColor())
									.multiplyScalar(specularIntensity)
									.multiplyScalar(falloff);

							totalSpecular = totalSpecular.add(intersectSpecularColor.multiply(lightSpecularIntensity));
						}
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
