package org.snowjak.rays;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.model.FlatLightingModel;
import org.snowjak.rays.light.model.LightingModel;
import org.snowjak.rays.shape.Shape;

/**
 * <p>
 * Represents the entire 3D context in which the raytracer operates.
 * </p>
 * <p>
 * A <strong>singleton</strong>, there will only be 1 instance of this object in
 * the JVM.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class World {

	/**
	 * Double values smaller than this will be considered to be "close enough"
	 * to zero.
	 */
	public static final double DOUBLE_ERROR = 1e-10;

	/**
	 * The distance from (0,0,0) after which points will be said to be "too far
	 * away". Establishes "finite infinite distance".
	 */
	public static final double WORLD_BOUND = 1e10;

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 */
	public static final int DEFAULT_MAX_RAY_RECURSION = 4;

	private static World INSTANCE = null;

	private int maxRayRecursion = DEFAULT_MAX_RAY_RECURSION;

	private Camera camera = null;

	private List<Shape> shapes = new LinkedList<>();

	private List<Light> lights = new LinkedList<>();

	private LightingModel lightingModel = new FlatLightingModel();

	protected World() {
	}

	/**
	 * @return the World instance. Instantiates it, too, if it doesnt' exist at
	 *         time of calling
	 */
	public static World getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new World();

		return INSTANCE;
	}

	/**
	 * Check every single {@link Shape} in this world and return a list of every
	 * single {@link Intersection} the given {@link Ray} produces.
	 * 
	 * @param ray
	 *            the ray to use, expressed in global coordinates
	 * @return every single Intersection the given Ray produces, sorted by
	 *         distance from the Ray's origin
	 */
	public List<Intersection<Shape>> getShapeIntersections(Ray ray) {

		List<Intersection<Shape>> intersections = new LinkedList<>();
		for (Shape shape : World.getSingleton().getShapes())
			intersections.addAll(shape.getIntersections(ray));

		return intersections.stream()
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * @return the world's active {@link Camera}
	 */
	public Camera getCamera() {

		return camera;
	}

	/**
	 * Set the currently-active Camera.
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {

		this.camera = camera;
	}

	/**
	 * @return the world's current set of {@link Shape}s
	 */
	public List<Shape> getShapes() {

		return shapes;
	}

	/**
	 * @return the world's current set of {@link Light}s
	 */
	public List<Light> getLights() {

		return lights;
	}

	/**
	 * @return the {@link LightingModel} currently in-use
	 */
	public LightingModel getLightingModel() {

		return lightingModel;
	}

	/**
	 * Set the {@link LightingModel} implementation to use.
	 * 
	 * @param lightingModel
	 */
	public void setLightingModel(LightingModel lightingModel) {

		this.lightingModel = lightingModel;
	}

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 * 
	 * @return allowed depth of ray recursion
	 */
	public int getMaxRayRecursion() {

		return maxRayRecursion;
	}

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 * 
	 * @param maxRayRecursion
	 */
	public void setMaxRayRecursion(int maxRayRecursion) {

		this.maxRayRecursion = maxRayRecursion;
	}

}
