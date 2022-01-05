import java.util.List;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Class responsible for running this project based on the provided command-line arguments.
 * Stores the database and wordcount map (updated: project2)
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class InvertedIndexBuilder {
	/**
	 * Sets snowball stemmer algorithm
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Builds the inverted index based on the given path. Also stores the word count for each file that is read
	 * @param path the path to the directory of files
	 * @param index the Inverted index object that will be modified
	 *
	 * @throws IOException if IO error occurs
	 */
	public static void build(Path path, InvertedIndex index) throws IOException{
		List<Path> textFiles = TextFileFinder.list(path);
		for(Path file : textFiles) {
			readFile(file, index);
		}
	}

	/**
	 * Reads the given file and stores it to the inverted index. Also stores the total word count for the file.
	 * @param path the path to the directory of files
	 * @param index the Inverted index object that will be modified
	 * 
	 * @throws IOException if IO error occurs
	 * @see #build(Path, InvertedIndex)
	 */
	public static void readFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path,StandardCharsets.UTF_8)){
			Stemmer stemmer = new SnowballStemmer(DEFAULT);
			String line =br.readLine();

			int pos = 1;
			while(line != null) {
				String [] words = TextParser.parse(line);
				for(String word : words) {
					word = stemmer.stem(word).toString();
					index.add(word, path.toString(), pos);
					pos++;
				}
				line = br.readLine();
			}
		}
	}
}
