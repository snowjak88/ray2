package org.snowjak.rays.world.importfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Defines a single object definition within a .world file.
 * <p>
 * An object definition is of the form:
 * 
 * <pre>
 * ...
 *    object-name {
 *        field-1:value
 *        field-2:value
 *        ...
 *        field-N: child-object-name { ... }
 *        ...
 *    }
 * ...
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class WorldFileObjectDefinition {

	private String objectName;

	private Map<String, Collection<String>> literalValues = new HashMap<>();

	private Map<String, Collection<WorldFileObjectDefinition>> childObjects = new HashMap<>();

	/**
	 * Construct a new WorldFileObjectDefinition instance.
	 * 
	 * @param objectName
	 */
	public WorldFileObjectDefinition(String objectName) {
		this.objectName = objectName;
	}

	/**
	 * @return this object-definition's name
	 */
	public String getObjectName() {

		return objectName;
	}

	/**
	 * Add a new literal value to this object definition
	 * 
	 * @param literalName
	 * @param literalValue
	 */
	public void addLiteralValue(String literalName, String literalValue) {

		if (literalValues.get(literalName) == null)
			literalValues.put(literalName, new LinkedList<>());
		literalValues.get(literalName).add(literalValue);
	}

	/**
	 * Add a new child object definition to this object definition
	 * 
	 * @param childName
	 * @param childObject
	 */
	public void addChildObject(String childName, WorldFileObjectDefinition childObject) {

		if (childObjects.get(childName) == null)
			childObjects.put(childName, new LinkedList<>());
		childObjects.get(childName).add(childObject);
	}

	/**
	 * @param literalName
	 * @return all literal values associated with the given name, if any exist
	 */
	public Optional<Collection<String>> getLiteralValues(String literalName) {

		return Optional.ofNullable(literalValues.get(literalName));
	}

	/**
	 * @param childName
	 * @return all child WorldFileObjectDefinitions associated with the given
	 *         name, if any exist
	 */
	public Optional<Collection<WorldFileObjectDefinition>> getChildObjects(String childName) {

		return Optional.ofNullable(childObjects.get(childName));
	}

	/**
	 * @return a Map of this object-definition's literal values by name
	 */
	public Map<String, Collection<String>> getAllLiteralValues() {

		return literalValues;
	}

	/**
	 * @return a Map of this object-definition's child-objects by name
	 */
	public Map<String, Collection<WorldFileObjectDefinition>> getAllChildObjects() {

		return childObjects;
	}
}
