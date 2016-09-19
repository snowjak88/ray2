package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

import javafx.scene.paint.Color;

/**
 * A {@link LightingModel} that implements the Schlick approximation to the
 * Fresnel equations for reflection and refraction for the interface between two
 * materials of differing indices of refraction.
 * <p>
 * The Fresnel equations model the relative proportion of reflected vs.
 * refracted light between two materials of differing indices of refraction. The
 * Schlick approximation simplifies these equations by ignoring the change in
 * phase-angle that reflected and refracted light undergoes.
 * </p>
 * <p>
 * This LightingModel does not model sub-surface scattering, nor does it do an
 * especially rigorous job of handling surface colors. It ignores the effect of
 * intervening materials (i.e., the Material sitting between the ray's origin
 * and the first intersection), and relies on a
 * {@link PhongSpecularLightingModel} to derive the lighting at a point on an
 * object's surface.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FresnelLightingModel implements LightingModel {

	private LightingModel surfaceLightingModel = null;

	/**
	 * Construct a new {@link FresnelLightingModel}, using the specified
	 * {@link LightingModel} to compute the radiance of encountered surfaces.
	 * 
	 * @param surfaceLightingModel
	 */
	public FresnelLightingModel(LightingModel surfaceLightingModel) {
		this.surfaceLightingModel = surfaceLightingModel;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		if (ray.getRecursiveLevel() > World.getSingleton().getMaxRayRecursion())
			return surfaceLightingModel.determineRayColor(ray, intersections);

		//
		//
		//
		Intersection<Shape> intersect = intersections.get(0);
		FresnelResult fresnel = calculateFresnelResult(intersect);
		double reflectance = fresnel.getReflectance();
		double transmittance = fresnel.getTransmittance();

		//
		//
		// Now shoot some rays!
		LightingResult finalResult = new LightingResult();
		finalResult.setEye(ray);
		finalResult.setPoint(intersect.getPoint());
		finalResult.setNormal(intersect.getNormal());

		//
		//
		//
		LightingResult surfaceResult = new LightingResult();
		surfaceResult.setEye(ray);
		surfaceResult.setPoint(intersect.getPoint());
		surfaceResult.setNormal(intersect.getNormal());
		surfaceResult = surfaceLightingModel.determineRayColor(ray, intersections).orElse(surfaceResult);
		RawColor surfaceColor = surfaceResult.getRadiance();

		//
		//
		//
		LightingResult reflectedResult, refractedResult;
		RawColor reflectedColor = new RawColor(), refractedColor = new RawColor();
		double surfaceTransparency = intersect.getEnteringMaterial().getSurfaceTransparency(intersect.getPoint());
		RawColor reflectedTint = new RawColor(Color.WHITE), refractedTint = new RawColor(Color.WHITE);

		if (reflectance > 0d) {
			List<Intersection<Shape>> reflectedIntersections = World.getSingleton()
					.getShapeIntersections(fresnel.getReflectedRay());
			reflectedResult = World.getSingleton()
					.getLightingModel()
					.determineRayColor(fresnel.getReflectedRay(), reflectedIntersections)
					.orElse(new LightingResult());
			reflectedColor = reflectedResult.getRadiance();

			reflectedTint = Functions.lerp(reflectedTint, intersect.getDiffuse(intersect.getPoint()),
					1d - surfaceTransparency);

			finalResult.getContributingResults().add(new Pair<>(reflectedResult, reflectance));

		}
		if (transmittance > 0d) {
			//
			// Get the color of the refracted ray.
			List<Intersection<Shape>> refractedIntersections = World.getSingleton()
					.getShapeIntersections(fresnel.getRefractedRay());
			refractedResult = World.getSingleton()
					.getLightingModel()
					.determineRayColor(fresnel.getRefractedRay(), refractedIntersections)
					.orElse(new LightingResult());
			refractedColor = refractedResult.getRadiance();

			//
			//
			// The refracted color is to be mixed with the surface
			// color, insofar as the surface is transparent

			//
			//
			double finalSurfaceFraction = 1d - surfaceTransparency;
			double finalRefractedFraction = surfaceTransparency;

			RawColor finalRefractedColor = Functions.lerp(surfaceColor, refractedColor, surfaceTransparency);

			finalResult.getContributingResults().add(new Pair<>(refractedResult, finalRefractedFraction));
			finalResult.getContributingResults().add(new Pair<>(surfaceResult, finalSurfaceFraction));
			refractedTint = Functions.lerp(new RawColor(Color.WHITE), intersect.getDiffuse(intersect.getPoint()),
					1d - surfaceTransparency);
			refractedColor = finalRefractedColor;
		}

		finalResult.setRadiance(Functions.lerp(reflectedColor, refractedColor, transmittance));
		finalResult.setTint(Functions.lerp(reflectedTint, refractedTint, transmittance));

		return Optional.of(finalResult);
	}

	/**
	 * Given an Intersection, calculate the Schlick approximation to the Fresnel
	 * equations and return a {@link FresnelResult} encapsulating the results of
	 * that approximation.
	 * 
	 * @param intersection
	 * @return a {@link FresnelResult} describing the resulting reflection &
	 *         refraction
	 */
	public FresnelResult calculateFresnelResult(Intersection<Shape> intersection) {

		Ray ray = intersection.getRay();
		Vector3D point = intersection.getPoint();
		Vector3D i = intersection.getRay().getVector();
		Vector3D n = intersection.getNormal();
		double theta_i = Vector3D.angle(i.negate(), n);

		//
		//
		//
		double n1 = intersection.getLeavingMaterial().getRefractiveIndex(point),
				n2 = intersection.getEnteringMaterial().getRefractiveIndex(point);

		//
		//
		// Determine reflected ray
		Vector3D reflectedVector = getTangentPart(i, n).subtract(getNormalPart(i, n));
		Ray reflectedRay = new Ray(point, reflectedVector, ray.getRecursiveLevel() + 1);

		//
		//
		// Determine refracted ray
		double sin2_theta_t = pow(n1 / n2, 2d) * (1d - pow(cos(theta_i), 2d));
		Vector3D refractedVector = i.scalarMultiply(n1 / n2)
				.add(n.scalarMultiply((n1 / n2) * cos(theta_i) - sqrt(1d - sin2_theta_t)));
		Ray refractedRay = new Ray(point, refractedVector, ray.getRecursiveLevel() + 1);

		//
		//
		// Calculate reflectance and transmittance fractions
		double reflectance = 1d;
		//
		//
		// Is this *not* a case of Total-Internal Reflection?
		if (sin2_theta_t <= 1d) {
			//
			double cos_theta_t = sqrt(1d - sin2_theta_t);
			double r_normal = pow((n1 * cos(theta_i) - n2 * cos_theta_t) / (n1 * cos(theta_i) + n2 * cos_theta_t), 2d);
			double r_tangent = pow((n2 * cos(theta_i) - n1 * cos_theta_t) / (n2 * cos(theta_i) + n1 * cos_theta_t), 2d);

			reflectance = (r_normal + r_tangent) / 2d;
		}

		return new FresnelResult(reflectance, 1d - reflectance, reflectedRay, refractedRay);
	}

	/**
	 * A data-bean describing the results of a Fresnel interaction: the results
	 * for Fresnel reflectance and transmittance, plus a reflected and a
	 * refracted Ray
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class FresnelResult {

		public FresnelResult(double reflectance, double transmittance, Ray reflectedRay, Ray refractedRay) {
			this.reflectance = reflectance;
			this.transmittance = transmittance;
			this.reflectedRay = reflectedRay;
			this.refractedRay = refractedRay;
		}

		private double reflectance, transmittance;

		private Ray reflectedRay, refractedRay;

		public double getReflectance() {

			return reflectance;
		}

		public double getTransmittance() {

			return transmittance;
		}

		public Ray getReflectedRay() {

			return reflectedRay;
		}

		public Ray getRefractedRay() {

			return refractedRay;
		}
	}

	private Vector3D getNormalPart(Vector3D v, Vector3D normal) {

		return normal.scalarMultiply(normal.dotProduct(v));
	}

	private Vector3D getTangentPart(Vector3D v, Vector3D normal) {

		return v.subtract(getNormalPart(v, normal));
	}

}
