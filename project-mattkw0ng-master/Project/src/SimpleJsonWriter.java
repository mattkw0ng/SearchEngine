import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Modified to write output for project1. Asobject() now takes in a Map<String, Map<String,TreeSet<Integer>>>
 * Added asDictioary() for project 2
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class SimpleJsonWriter {

	/**
	 * Writes elements in the correct format for a SearchResult
	 * @param elements {@linkplain TreeMap} that links a query search to all it's search results 
	 * @param path the path to the output file
	 * @throws IOException if an IO exception occurs
	 */
	public static void asSearchResults(TreeMap<String, Collection<InvertedIndex.SearchResult>> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asSearchResults(elements, writer);
		}
	}

	/**
	 * 
	 * @param elements {@linkplain TreeMap} that links a query search to all it's search results 
	 * @param writer a Writer to write to the file
	 * @throws IOException if an IO exception occurs
	 */
	public static void asSearchResults(TreeMap<String, Collection<InvertedIndex.SearchResult>> elements, Writer writer) throws IOException {
		if(!elements.isEmpty()) {
			Iterator<String> it = elements.keySet().iterator();
			String current = it.next();

			writer.write("{\n");
			quote(current, writer, 1);
			writer.write(": [\n");
			writeMultipleResults(elements.get(current), writer, 2);
			writer.write("  ]"); //newline, single indent, closing bracket 
			while(it.hasNext()) {
				writer.write(",\n");
				current = it.next();
				quote(current, writer, 1);
				writer.write(": [\n");
				writeMultipleResults(elements.get(current), writer, 2);
				writer.write("  ]");
			}
			writer.write("\n}");
		}
	}  

	/**
	 * Writes multiple SearchResults with {@link #writeSearchResult(InvertedIndex.SearchResult, Writer, int)} method
	 * 
	 * @param results a {@linkplain Collection} of SearchResult objects
	 * @param writer the Writer to write to the file
	 * @param level the level of indentation
	 * 
	 * @throws IOException if IO exception occurs
	 */
	public static void writeMultipleResults(Collection<InvertedIndex.SearchResult> results, Writer writer, int level) throws IOException {
		if(!results.isEmpty()) {
			Iterator<InvertedIndex.SearchResult> it = results.iterator();
			InvertedIndex.SearchResult current = it.next();
			writeSearchResult(current, writer, level);
			while (it.hasNext()) {
				current = it.next();
				writer.write(",\n");
				writeSearchResult(current, writer, level);
			}
			writer.write("\n");
		}
	}

	/**
	 * Writes the item as a SearchResult.
	 *
	 * @param item the SearchResult to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * 
	 * @see #writeMultipleResults(Collection, Writer, int)
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResult(InvertedIndex.SearchResult item, Writer writer, int level) throws IOException{
		indent("{\n", writer, level);
		indent("\"where\": ", writer, level+1);
		quote(item.getWhere(), writer);
		writer.write(",\n");

		indent("\"count\": ", writer, level+1);
		writer.write(""+item.getCount());
		writer.write(",\n");

		indent("\"score\": ", writer, level+1);
		writer.write(String.format("%.8f", item.getScore()));
		writer.write("\n");
		indent("}", writer, level);

	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level)
			throws IOException {
		if(!elements.isEmpty()) {
			Iterator<Integer> it = elements.iterator();
			Integer current = it.next();

			writer.write("[\n");
			indent(current, writer, level + 1);
			while(it.hasNext()) {
				current = it.next();
				writer.write(",\n");
				indent(current, writer, level + 1);
			}
			writer.write("\n");
			indent("]", writer, level);
		}
	}

	/**
	 * Writes elements as pretty JSON array
	 * 
	 * @param elements the elements to write
	 * @param path the path to the output file
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Writes elements as pretty JSON array
	 * 
	 * @param elements the elements to write
	 * 
	 * @return {@link String} object
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON dictionary. 
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asDictionary(Map<String, Integer> elements, Writer writer, int level)
			throws IOException {
		if(!elements.isEmpty()){ //if the inverted index is empty, do not print anything
			Iterator<String> it = elements.keySet().iterator();
			indent("{\n  ",writer,level);

			String current = it.next();
			quote(current, writer, level);
			writer.write(": " + elements.get(current));

			while(it.hasNext()) {
				current = it.next();
				writer.write(",\n  ");
				quote(current, writer, level);
				writer.write(": " + elements.get(current));
			}

			writer.write("\n}\n");
		}
	}

	/**
	 * Writes the elements as a pretty JSON dictionary to the given path. 
	 * @param elements a map of string-int pairs
	 * @param path the path to the output file
	 * @throws IOException if an IO error occurs
	 */
	public static void asDictionary(Map<String, Integer> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asDictionary(elements, writer, 0);
		}
	}

	/**
	 *  Writes the elements as a pretty JSON dictionary. 
	 * @param elements a map of string-int pairs
	 * @return String to be printed to console or System for debugging
	 */
	public static String asDictionary(Map<String, Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asDictionary(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}


	/**
	 * Writes the elements as a pretty JSON object. (modified to include nested array)
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int level)
			throws IOException {
		if(writer==null) {
			throw new IOException();
		}else if (elements.isEmpty()){ //if the inverted index is empty, do not print anything
			writer.write("");
		}else {
			Iterator<String> it = elements.keySet().iterator();
			indent("{\n  ",writer,level);

			String current = it.next();
			quote(current,writer,level);
			writer.write(": ");
			asNestedArray(elements.get(current),writer,1);

			while(it.hasNext()) {
				current = it.next();
				writer.write(",\n  ");
				quote(current,writer,level);
				writer.write(": ");
				asNestedArray(elements.get(current),writer,1);
			}

			writer.write("\n}\n");
		}
	}

	/**
	 * Writes the elements as a pretty JSON object (modified to include nested array)
	 * @param elements the elements to write
	 * @param path the path to the output file
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 *  Writes the elements as a pretty JSON object (modified to include nested array)
	 * @param elements the elements to write
	 * @return String output as a string
	 */
	public static String asObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic notation used
	 * allows this method to be used for any type of map with any type of nested collection of integer
	 * objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements,
			Writer writer, int level) throws IOException {
		if(writer == null) {
			throw new IOException();
		}else {
			writer.write("{\n");
			Iterator<String> it = elements.keySet().iterator();
			String key = it.next();
			quote(key,writer,level+1);
			writer.write(": ");
			asArray(elements.get(key),writer,level+1);


			while(it.hasNext()) {
				writer.write(",\n");
				key = it.next();
				quote(key,writer,level+1);
				writer.write(": ");
				asArray(elements.get(key),writer,level+1);
			}
			writer.write("\n");
			indent("}",writer,level);
		}

	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array.
	 * @param elements the elements to write
	 * @param path the path to the output file
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array.
	 * @param elements the elements to write
	 * @return String output as a string
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents using 2 spaces by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times the number of times to write a tab symbol
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		for (int i = 0; i < times; i++) {
			writer.write(' ');
			writer.write(' ');
		}
	}

	/**
	 * Indents elements
	 * @param element the element to indent
	 * @param writer the writer to use
	 * @param times the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents elements
	 * @param element the element to indent
	 * @param writer the writer to use
	 * @param times the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Quotes elements
	 * @param element the element to quote
	 * @param writer the writer to use
	 * @param times the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}
}
