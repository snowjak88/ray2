package org.snowjak.rays.world.importfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.StringTokenizer;

/**
 * The WorldFileScanner is responsible for accepting a Reader of some kind and
 * producing a World object, along with whatever other objects are represented
 * by the content which the Reader reads.
 * <p>
 * The WorldFileScanner expects each .world file to be of the following form:
 * 
 * <pre>
 * type-name {
 *     field-name: literal-value
 *     field-name: literal-value
 *     ...
 *     field-name: type-name { field-name:literal-value field-name:literal-value }
 *     ...
 * }
 * </pre>
 * 
 * Whitespace is ignored by the WorldFileScanner, except where it is used to
 * separate words.
 * </p>
 * <p>
 * Field-names and object type-names are defined in the source-code by the
 * annotation {@link HasName}, and detected at runtime by the
 * {@link NamedBuilderRegistrar}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class WorldFileScanner {

	private Reader worldReader = null;

	/**
	 * Construct a new WorldFileScanner, using the given {@link Reader} as its
	 * input-source.
	 * 
	 * @param worldReader
	 */
	public WorldFileScanner(Reader worldReader) {
		this.worldReader = worldReader;
	}

	/**
	 * Scan the provided {@link BufferedReader} and convert it to a
	 * {@link WorldFileObjectDefinition} tree, if possible.
	 * 
	 * @return the result WorldFileObjectDefinition tree, or nothing if said
	 *         conversion is not possible
	 * @throws IOException
	 */
	public Optional<WorldFileObjectDefinition> scan() throws IOException {

		Iterator<String> worldFileIterator = new TokenizedReaderIterator(worldReader);

		String outerObjectName = worldFileIterator.next();
		if (outerObjectName.length() < 1)
			return Optional.empty();

		String outerBrace = worldFileIterator.next();
		if (!outerBrace.equals("{"))
			return Optional.empty();

		return scanObject(outerObjectName, worldFileIterator);
	}

	private Optional<WorldFileObjectDefinition> scanObject(String typeName, Iterator<String> worldFileIterator) {

		WorldFileObjectDefinition result = new WorldFileObjectDefinition(typeName);

		String currentToken = null, currentName = null;
		boolean skipGettingNextToken = false;
		do {
			if (!skipGettingNextToken)
				currentToken = worldFileIterator.next();
			else
				skipGettingNextToken = false;

			if (currentToken != null) {

				if (currentToken.equals(":") && currentName != null) {
					String valueToken = worldFileIterator.next();
					String followingToken = worldFileIterator.next();

					if (followingToken.equals("{")) {

						Optional<WorldFileObjectDefinition> childObject = scanObject(valueToken, worldFileIterator);

						if (childObject.isPresent())
							result.addChildObject(currentName, childObject.get());

					} else {
						addField(result, currentName, valueToken);

						currentToken = followingToken;
						skipGettingNextToken = true;
					}
				}

				else if (currentToken.equals("{") && currentName != null) {

				} else
					currentName = currentToken;
			}

		} while (currentToken != null && !currentToken.equals("}"));

		return Optional.of(result);
	}

	private void addField(WorldFileObjectDefinition currentObject, String fieldName, String fieldValue) {

		if (fieldValue != null)
			currentObject.addLiteralValue(fieldName, fieldValue);
	}

	/**
	 * Close this WorldFileScanner and release its underlying resources.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {

		worldReader.close();
	}

	@SuppressWarnings("javadoc")
	public static class TokenizedReaderIterator implements Iterator<String> {

		private BufferedReader worldReader = null;

		private Queue<String> tokens = new LinkedList<>();

		public TokenizedReaderIterator(Reader worldReader) {
			this.worldReader = new BufferedReader(worldReader);
		}

		@Override
		public boolean hasNext() {

			try {
				return worldReader.ready();

			} catch (IOException e) {
				return false;
			}
		}

		@Override
		public String next() {

			try {
				if (!worldReader.ready())
					throw new NoSuchElementException();

				while (tokens.size() < 1) {

					String nextLine = worldReader.readLine();
					if (nextLine == null)
						throw new NoSuchElementException();

					StringTokenizer tokenizer = new StringTokenizer(nextLine, " \t\r\n\f:{}", true);
					while (tokenizer.hasMoreTokens()) {
						String currentToken = tokenizer.nextToken();
						if (!currentToken.matches("[ \\t\\r\\n\\f]"))
							tokens.add(currentToken);
					}
				}

				return tokens.remove();

			} catch (Exception e) {
				throw new NoSuchElementException();
			}
		}

	}
}
