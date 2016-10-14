package org.snowjak.rays.light;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.TransformableBuilder;
import org.snowjak.rays.transform.Transformer;
import org.snowjak.rays.world.HasName;

/**
 * A convenient interface for building {@link Light}s.
 * 
 * @author snowjak88
 * @param <T>
 *            any subtype of Light
 *
 */
public abstract class LightBuilder<T extends Light> implements Builder<T>, TransformableBuilder<T> {

	private RawColor ambient = Light.DEFAULT_AMBIENT, diffuse = Light.DEFAULT_DIFFUSE,
			specular = Light.DEFAULT_SPECULAR;

	private Function<Vector3D, Double> intensity = Light.DEFAULT_INTENSITY;

	private BiFunction<Light, Vector3D, Double> falloff = Light.DEFAULT_FALLOFF;

	private Optional<Double> radius = Light.DEFAULT_RADIUS;

	private List<Transformer> transformers = new LinkedList<>();

	/**
	 * Configures this in-progress Light to have the given ambient radiance.
	 * 
	 * @param ambientRadiance
	 * @return this Builder, for method-chaining
	 */
	@HasName("ambient")
	public LightBuilder<T> ambient(RawColor ambientRadiance) {

		this.ambient = ambientRadiance;
		return this;
	}

	/**
	 * Configures this in-progress Light to have the given diffuse radiance.
	 * 
	 * @param diffuseRadiance
	 * @return this Builder, for method-chaining
	 */
	@HasName("diffuse")
	public LightBuilder<T> diffuse(RawColor diffuseRadiance) {

		this.diffuse = diffuseRadiance;
		return this;
	}

	/**
	 * Configures this in-progress Light to have the given specular radiance.
	 * 
	 * @param specularRadiance
	 * @return this Builder, for method-chaining
	 */
	@HasName("specular")
	public LightBuilder<T> specular(RawColor specularRadiance) {

		this.specular = specularRadiance;
		return this;
	}

	/**
	 * Configures this in-progress Light to use the specified constant
	 * intensity.
	 * 
	 * @param intensity
	 * @return this Builder, for method-chaining
	 */
	@HasName("intensity")
	public LightBuilder<T> intensity(double intensity) {

		this.intensity = Functions.constant(intensity);
		return this;
	}

	/**
	 * Configures this in-progress Light to use the specified intensity
	 * function.
	 * 
	 * @param intensityFunction
	 * @return this Builder, for method-chaining
	 */
	public LightBuilder<T> intensity(Function<Vector3D, Double> intensityFunction) {

		this.intensity = intensityFunction;
		return this;
	}

	/**
	 * Configures this in-progress Light to use the specified falloff-function.
	 * 
	 * @param falloffFunction
	 * @return this Builder, for method-chaining
	 * @see Light#DEFAULT_FALLOFF_FUNCTION()
	 */
	public LightBuilder<T> falloff(BiFunction<Light, Vector3D, Double> falloffFunction) {

		this.falloff = falloffFunction;
		return this;
	}

	/**
	 * Configures this in-progress Light to have the specified radius.
	 * 
	 * @param radius
	 * @return this Builder, for method-chaining
	 */
	@HasName("radius")
	public LightBuilder<T> radius(double radius) {

		this.radius = (Double.compare(radius, 0d) <= 0) ? Optional.empty() : Optional.of(radius);
		return this;
	}

	@HasName("transform")
	@Override
	public LightBuilder<T> transform(Transformer transformer) {

		this.transformers.add(transformer);

		return this;
	}

	/**
	 * Create a new instance of a Light subtype. For example,
	 * 
	 * <pre>
	 * 
	 * protected PointLight createNewLightInstance() {
	 * 
	 * 	return new PointLight();
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	protected abstract T createNewLightInstance();

	@Override
	public T build() {

		T newLight = createNewLightInstance();
		newLight.setAmbientColor(ambient);
		newLight.setDiffuseColor(diffuse);
		newLight.setSpecularColor(specular);
		newLight.setIntensityFunction(intensity);
		newLight.setFalloffFunction(falloff);
		newLight.setRadius(radius);

		newLight.getTransformers().addAll(transformers);

		newLight = performTypeSpecificInitialization(newLight);

		return newLight;
	}

	/**
	 * This method is automatically called by {@link #build()}. Use this to
	 * implement any type-specific initialization -- e.g., a
	 * {@link DirectionalLight}'s direction.
	 * 
	 * @param newLightInstance
	 *            the in-progress Light subtype
	 * @return the in-progress Light subtype after type-specific initialization
	 */
	protected abstract T performTypeSpecificInitialization(T newLightInstance);

}
