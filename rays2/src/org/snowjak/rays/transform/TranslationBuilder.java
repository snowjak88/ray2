package org.snowjak.rays.transform;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for building {@link Translation}s.
 * 
 * @author snowjak88
 *
 */
@HasName("translate")
public class TranslationBuilder implements Builder<Translation> {

	private double tx = 0d, ty = 0d, tz = 0d;

	/**
	 * @return a new TranslationBuilder instance
	 */
	public static TranslationBuilder builder() {

		return new TranslationBuilder();
	}

	protected TranslationBuilder() {

	}

	/**
	 * Configure this {@link Translation} to translate by a given distance along
	 * the X-axis
	 * 
	 * @param tx
	 * @return this Builder, for method-chaining
	 */
	@HasName("x")
	public TranslationBuilder x(double tx) {

		this.tx = tx;
		return this;
	}

	/**
	 * Configure this {@link Translation} to translate by a given distance along
	 * the Y-axis
	 * 
	 * @param ty
	 * @return this Builder, for method-chaining
	 */
	@HasName("y")
	public TranslationBuilder y(double ty) {

		this.ty = ty;
		return this;
	}

	/**
	 * Configure this {@link Translation} to translate by a given distance along
	 * the Z-axis
	 * 
	 * @param tz
	 * @return this Builder, for method-chaining
	 */
	@HasName("z")
	public TranslationBuilder z(double tz) {

		this.tz = tz;
		return this;
	}

	@Override
	public Translation build() {

		return new Translation(tx, ty, tz);
	}

}
