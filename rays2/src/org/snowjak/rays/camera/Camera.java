package org.snowjak.rays.camera;

import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.toRadians;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.model.LightingModel.LightingResult;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;
import org.snowjak.rays.ui.BasicScreen;
import org.snowjak.rays.world.World;

/**
 * Represents a virtual window onto the {@link World}. A {@link BasicScreen}
 * requires at least one Camera in order to have a viewpoint to render on the
 * UI.
 * 
 * <p>
 * This specification makes no assumptions about the capabilities of Camera
 * implementations. Complicated sub-types may implement anti-aliasing, or
 * depth-of-field, or other effects.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Camera implements Transformable {

	private double cameraFrameSideLength, cameraDepth, cameraFieldOfView;

	private final Vector3D eyeLocation;

	private Deque<Transformer> transformers = new LinkedList<>();

	/**
	 * Create a new Camera of the given dimensions (in world units).
	 * 
	 * @param cameraFrameWidth
	 * @param fieldOfView
	 *            camera FOV, expressed in degrees
	 */
	public Camera(double cameraFrameWidth, double fieldOfView) {
		this.cameraFrameSideLength = cameraFrameWidth;
		this.cameraFieldOfView = fieldOfView;
		this.cameraDepth = (cameraFrameSideLength / 2d) / sin(toRadians(fieldOfView));

		this.eyeLocation = new Vector3D(0.0, 0.0, -cameraDepth);
	}

	/**
	 * Calculate the color of light reaching this particular point on the
	 * camera.
	 * 
	 * @param cameraX
	 * @param cameraY
	 * @return the amount of light reaching the camera at this point
	 */
	public Optional<LightingResult> shootRay(double cameraX, double cameraY) {

		Vector3D location = new Vector3D(cameraX, cameraY, 0.0);
		Vector3D direction = location.subtract(getEyeLocation()).normalize();

		Ray ray = localToWorld(new Ray(location, direction));
		Optional<Intersection<Shape>> intersection = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getClosestShapeIntersection(ray);

		return RaytracerContext.getSingleton().getCurrentRenderer().getLightingModel().determineRayColor(ray,
				intersection);

	}

	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	/**
	 * @return this Camera's size (in world units)
	 */
	public double getCameraFrameSideLength() {

		return cameraFrameSideLength;
	}

	/**
	 * @return this Camera's field of view (in degrees)
	 */
	public double getCameraFieldOfView() {

		return cameraFieldOfView;
	}

	/**
	 * @return this Camera's depth (in world units) -- a measure of how wide the
	 *         FOV is.
	 */
	public Vector3D getEyeLocation() {

		return eyeLocation;
	}

}
