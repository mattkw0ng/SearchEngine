import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Class to clean and parse a query line and return a collection
 * Includes methods to remove duplicates, and sort alphabetically
 * @author matthew
 *
 */
public class QueryParser implements QueryParserInterface{
	/**
	 * collection of all the search results
	 */
	private final TreeMap<String, Collection<InvertedIndex.SearchResult>> allResults;
	/**
	 * collection of all queries
	 */
	private final InvertedIndex index;

	/**
	 * Initializes QueryParser
	 * @param index The inverted index to use
	 */
	public QueryParser(InvertedIndex index) {
		this.allResults = new TreeMap<>();
		this.index = index;
	}

	/**
	 * Generates and searches a query
	 * @param line String to create query from
	 * @param exact determines whether to conduct exact or partial search
	 */
	@Override
	public void parseLine(String line, boolean exact) {
		Collection<String> query = QueryParserInterface.cleanLine(line);
		String joined = String.join(" ", query);
		if(joined.length() > 0) { //avoids adding empty strings
			if (allResults.containsKey(joined)) {
				return;
			}
			allResults.put(joined, index.search(query, exact));
		}
	}

	/**
	 * Writes search results to the given file
	 * @param file the file to write to
	 * @throws IOException if an IO error occurs
	 */
	@Override
	public void writeToFile(Path file) throws IOException {
		SimpleJsonWriter.asSearchResults(allResults, file);
	}
}
