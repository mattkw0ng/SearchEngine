import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Interface for the QueryParser and MultiThreadedQueryParser
 * @author matthew
 */
public interface QueryParserInterface {
	
	/**
	 * Default algorithm for the snowball stemmer
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;
	
	/**
	 * Method to parse a file and return a list of queries as collections of strings
	 * @param path the path to the query file
	 * @param exact determines whether to conduct exact or partial search
	 * @throws IOException if IO error occurs
	 */
	public default void parseFile(Path path, boolean exact) throws IOException {
		try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
			String line;
			while((line = reader.readLine()) != null) {
				parseLine(line, exact);
			}
		}
	}
	
	/**
	 * Generates and searches a query
	 * @param line String to create query from
	 * @param exact determines whether to conduct exact or partial search
	 */
	public void parseLine(String line, boolean exact);

	/**
	 * Method to parse a line and return a collection of words
	 * @param line the line of words to parse
	 * @return Collection of cleaned, stemmed, unique, and sorted words
	 */
	public static Collection<String> cleanLine(String line){
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		String [] words = TextParser.parse(line);

		TreeSet<String> set = new TreeSet<>(); 

		for(String word: words) {
			set.add(stemmer.stem(word).toString());
		}
		return set;
	}


	/**
	 * Writes search results to the given file
	 * @param file the file to write to
	 * @throws IOException if an IO error occurs
	 */
	public void writeToFile(Path file) throws IOException;	
}
