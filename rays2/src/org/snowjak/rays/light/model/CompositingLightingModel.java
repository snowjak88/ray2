package org.snowjak.rays.light.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A {@link LightingModel} that aggregates the results of several other
 * LightingModels into a single result.
 * 
 * @author snowjak88
 *
 */
public abstract class CompositingLightingModel implements LightingModel {

	private Collection<LightingModel> children = new LinkedList<>();

	/**
	 * Create a new {@link CompositingLightingModel} with no initial children.
	 */
	public CompositingLightingModel() {
		this(Collections.emptyList());
	}

	/**
	 * Create a new {@link CompositingLightingModel} with the given initial list
	 * of children.
	 * 
	 * @param children
	 */
	public CompositingLightingModel(LightingModel... children) {
		this(Arrays.asList(children));
	}

	/**
	 * Create a new {@link CompositingLightingModel} with the given initial list
	 * of children.
	 * 
	 * @param children
	 */
	public CompositingLightingModel(Collection<LightingModel> children) {
		this.children.addAll(children);
	}

	/**
	 * @return the list of {@link LightingModel}s contributing to this
	 *         {@link CompositingLightingModel}'s result
	 */
	public Collection<LightingModel> getChildren() {

		return children;
	}

}
