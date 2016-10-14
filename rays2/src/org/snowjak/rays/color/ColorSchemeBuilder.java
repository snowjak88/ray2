package org.snowjak.rays.color;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.TransformableBuilder;
import org.snowjak.rays.transform.Transformer;

import javafx.scene.paint.Color;

/**
 * Builds a {@link ColorScheme} object.
 * 
 * @author snowjak88
 *
 */
public class ColorSchemeBuilder implements Builder<ColorScheme>, TransformableBuilder<ColorScheme> {

	private Function<Vector3D, RawColor> colorFunction = Functions.constant(Color.HOTPINK);

	private List<Transformer> transformers = new LinkedList<>();

	/**
	 * @return a new ColorSchemeBuilder
	 */
	public static ColorSchemeBuilder builder() {

		return new ColorSchemeBuilder();
	}

	protected ColorSchemeBuilder() {

	}

	/**
	 * Give the in-progress {@link ColorScheme} a constant color
	 * 
	 * @param color
	 * @return this ColorSchemeBuilder
	 */
	public ColorSchemeBuilder constant(Color color) {

		this.colorFunction = Functions.constant(color);
		return this;
	}

	/**
	 * Give the in-progress {@link ColorScheme} a constant color
	 * 
	 * @param color
	 * @return this ColorSchemeBuilder
	 */
	public ColorSchemeBuilder constant(RawColor color) {

		this.colorFunction = Functions.constant(color);
		return this;
	}

	/**
	 * Give the in-progress {@link ColorScheme} a functional color
	 * 
	 * @param colorFunction
	 * @return this ColorSchemeBuilder
	 */
	public ColorSchemeBuilder function(Function<Vector3D, RawColor> colorFunction) {

		this.colorFunction = colorFunction;
		return this;
	}

	/**
	 * Add a {@link Transformer} to the in-progress {@link ColorScheme}
	 * 
	 * @param transform
	 * @return this ColorSchemeBuilder
	 */
	public ColorSchemeBuilder transform(Transformer transform) {

		this.transformers.add(transform);
		return this;
	}

	@Override
	public ColorScheme build() {

		ColorScheme colorScheme = new FunctionalColorScheme(this.colorFunction);
		colorScheme.getTransformers().addAll(this.transformers);
		return colorScheme;
	}

}
