package org.snowjak.rays.camera;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.transform.TransformableBuilder;
import org.snowjak.rays.transform.Transformer;
import org.snowjak.rays.world.importfile.HasName;

/**
 * Provides a convenient interface for building {@link Camera} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("camera")
public class CameraBuilder implements Builder<Camera>, TransformableBuilder<Camera> {

	private double cameraFrameWidth = 1d, cameraFieldOfView = 75d;

	private List<Transformer> transformers = new LinkedList<>();

	/**
	 * @return an instance of CameraBuilder
	 */
	public static CameraBuilder builder() {

		return new CameraBuilder();
	}

	protected CameraBuilder() {

	}

	/**
	 * Set this in-progress Camera's frame-width (in world units).
	 * 
	 * @param frameWidth
	 * @return this Builder, for method-chaining
	 */
	@HasName("frame-width")
	public CameraBuilder frameWidth(double frameWidth) {

		this.cameraFrameWidth = FastMath.max(frameWidth, 0d);
		return this;
	}

	/**
	 * Set this in-progress Camera's field-of-view (in degrees).
	 * 
	 * @param fieldOfView
	 * @return this Builder, for method-chaining
	 */
	@HasName("field-of-view")
	public CameraBuilder fieldOfView(double fieldOfView) {

		this.cameraFieldOfView = fieldOfView;
		return this;
	}

	@HasName("transform")
	@Override
	public TransformableBuilder<Camera> transform(Transformer transformer) {

		transformers.add(transformer);
		return this;
	}

	@Override
	public Camera build() {

		Camera newCamera = new Camera(cameraFrameWidth, cameraFieldOfView);
		newCamera.getTransformers().addAll(transformers);

		return newCamera;
	}

}
