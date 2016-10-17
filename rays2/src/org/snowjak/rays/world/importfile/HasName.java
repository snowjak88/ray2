package org.snowjak.rays.world.importfile;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that this element has a textual name by which it can be referenced
 * from a .world file.
 * 
 * @author snowjak88
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface HasName {

	/**
	 * the name associated with this element
	 */
	String value();
}
