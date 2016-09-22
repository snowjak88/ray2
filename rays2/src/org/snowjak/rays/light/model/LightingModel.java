package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.shape.Shape;

import javafx.scene.paint.Color;

/**
 * Represents an algorithm which performs lighting-calculations to determine the
 * color resulting from a single Ray. Depending on the complexity of the
 * underlying algorithm, this LightingModel may be recursive, spawning
 * additional Rays to compute reflection, refraction, etc.
 * 
 * @author snowjak88
 *
 */
public interface LightingModel {

	/**
	 * Determine the color resulting from a {@link Ray} and a given set of
	 * {@link Intersection}s produced by it.
	 * 
	 * @param ray
	 * @param intersection
	 * @return the resulting Color, if any
	 */
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection);

	/**
	 * Describes the outcome of executing this LightingModel.
	 * 
	 * @author snowjak
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class LightingResult {

		private Vector3D point = Vector3D.ZERO, normal = Vector3D.ZERO;

		private Ray eye = new Ray(Vector3D.ZERO, Vector3D.ZERO);

		private Collection<Light> visibleLights = new LinkedList<>();

		private RawColor radiance = new RawColor(), tint = new RawColor(Color.WHITE);

		private Collection<Pair<LightingResult, Double>> contributingResults = new LinkedList<>();

		public Vector3D getPoint() {

			return point;
		}

		public void setPoint(Vector3D point) {

			this.point = point;
		}

		public Vector3D getNormal() {

			return normal;
		}

		public void setNormal(Vector3D normal) {

			this.normal = normal;
		}

		public Ray getEye() {

			return eye;
		}

		public void setEye(Ray eye) {

			this.eye = eye;
		}

		public Collection<Light> getVisibleLights() {

			return visibleLights;
		}

		public RawColor getRadiance() {

			return radiance;
		}

		public void setRadiance(RawColor radiance) {

			this.radiance = radiance;
		}

		public Collection<Pair<LightingResult, Double>> getContributingResults() {

			return contributingResults;
		}

		public RawColor getTint() {

			return tint;
		}

		public void setTint(RawColor tint) {

			this.tint = tint;
		}

	}
}
