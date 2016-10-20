package org.snowjak.rays.function.builder;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.world.importfile.HasName;

import javafx.scene.paint.Color;

/**
 * Permits convenient building of checkerboard color-functions.
 * 
 * @author snowjak88
 * @see ColorFunctionBuilder
 */
@HasName("checkerboard")
public class CheckerboardColorFunctionBuilder implements ColorFunctionBuilder {

	private ColorScheme color1 = new SimpleColorScheme(Color.BLACK), color2 = new SimpleColorScheme(Color.WHITE);

	/**
	 * @return a new {@link CheckerboardColorFunctionBuilder} instance
	 */
	public static CheckerboardColorFunctionBuilder builder() {

		return new CheckerboardColorFunctionBuilder();
	}

	/**
	 * Set one of the two colors to use in this checkerboard
	 * {@link ColorScheme}.
	 * 
	 * @param color1
	 * @return this Builder, for method-chaining
	 */
	@HasName("color1")
	public CheckerboardColorFunctionBuilder color1(ColorScheme color1) {

		this.color1 = color1;
		return this;
	}

	/**
	 * Set one of the two colors to use in this checkerboard
	 * {@link ColorScheme}.
	 * 
	 * @param color1
	 * @return this Builder, for method-chaining
	 */
	@HasName("color1")
	public CheckerboardColorFunctionBuilder color1(RawColor color1) {

		this.color1 = new SimpleColorScheme(color1);
		return this;
	}

	/**
	 * Set one of the two colors to use in this checkerboard
	 * {@link ColorScheme}.
	 * 
	 * @param color2
	 * @return this Builder, for method-chaining
	 */
	@HasName("color2")
	public CheckerboardColorFunctionBuilder color2(ColorScheme color2) {

		this.color2 = color2;
		return this;
	}

	/**
	 * Set one of the two colors to use in this checkerboard
	 * {@link ColorScheme}.
	 * 
	 * @param color2
	 * @return this Builder, for method-chaining
	 */
	@HasName("color2")
	public CheckerboardColorFunctionBuilder color2(RawColor color2) {

		this.color2 = new SimpleColorScheme(color2);
		return this;
	}

	protected CheckerboardColorFunctionBuilder() {
	}

	@Override
	public Function<Vector3D, RawColor> build() {

		return (v) -> Functions.lerp(color1.getColor(v), color2.getColor(v), Functions.checkerboard(v));
	}

}
