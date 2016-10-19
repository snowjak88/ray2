package org.snowjak.rays.color;

import org.snowjak.rays.builder.Builder;

/**
 * Denotes that a Builder can build any type that itself implements
 * {@link HasColorScheme}.
 * 
 * @author snowjak88
 *
 * @param <T>
 *            any type that implements {@link HasColorScheme}
 */
public interface HasColorSchemeBuilder<T extends HasColorScheme> extends Builder<T> {

	/**
	 * Add a diffuse {@link ColorScheme} to this in-progress object.
	 * 
	 * @param diffuseColor
	 * @return this Builder, for method-chaining
	 */
	public HasColorSchemeBuilder<T> diffuse(ColorScheme diffuseColor);

	/**
	 * Add a specular {@link ColorScheme} to this in-progress object.
	 * 
	 * @param specularColor
	 * @return this Builder, for method-chaining
	 */
	public HasColorSchemeBuilder<T> specular(ColorScheme specularColor);

}
