package org.snowjak.rays.world;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for instantiating a {@link World} instance.
 * 
 * @author snowjak88
 *
 */
@HasName("world")
public class WorldBuilder implements Builder<World> {

	private Camera camera = new Camera(1d, 75d);

	private List<Shape> shapes = new LinkedList<>();

	private RawColor ambientRadiance = new RawColor();

	/**
	 * @return a new WorldBuilder instance
	 */
	public static WorldBuilder builder() {

		return new WorldBuilder();
	}

	protected WorldBuilder() {

	}

	/**
	 * Set the active Camera for this in-progress World.
	 * 
	 * @param camera
	 * @return this Builder, for method-chaining
	 */
	@HasName("camera")
	public WorldBuilder camera(Camera camera) {

		this.camera = camera;
		return this;
	}

	/**
	 * Add a new Shape to this in-progress World.
	 * 
	 * @param shape
	 * @return this Builder, for method-chaining
	 */
	@HasName("shape")
	public WorldBuilder shape(Shape shape) {

		this.shapes.add(shape);
		return this;
	}

	/**
	 * Sets the World's ambient-radiance to the given value.
	 * 
	 * @param ambientRadiance
	 * @return this Builder, for method-chaining
	 */
	@HasName("ambient")
	public WorldBuilder ambient(RawColor ambientRadiance) {

		this.ambientRadiance = ambientRadiance;
		return this;
	}

	@Override
	public World build() {

		World world = new World();

		world.setCamera(camera);
		world.getShapes().addAll(shapes);
		world.setAmbientRadiance(ambientRadiance);

		return world;
	}

}
