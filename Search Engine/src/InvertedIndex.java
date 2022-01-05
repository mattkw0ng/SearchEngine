import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * data structure to store elements and print as a pretty json file
 * @author matthew
 *
 */
public class InvertedIndex {
	/**
	 * class to store search result data
	 * @author matthew
	 *
	 */
	public class SearchResult implements Comparable<InvertedIndex.SearchResult> {
		/**
		 * the path to the file
		 */
		private final String where;
		/**
		 * the total matches in the file
		 */
		private int count;
		/**
		 * the total matches/total words in the file
		 */
		private double score;
		/**
		 * Constructor method
		 * @param where the path to the file
		 */
		public SearchResult(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0.0;
			wordCount.get(where);
		}

		/**
		 * updater method to set the count of the object and automatically update score
		 * @param word the word that was found
		 */
		private void update(String word) {
			this.count += invertedIndex.get(word).get(where).size();
			this.score = (Double.valueOf(this.count) / wordCount.get(where));
		}

		/**
		 * get method to retrieve the path name of the search result
		 * @return {@link String} the path name
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * get method to retrieve the count of the search result
		 * @return int the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * get method to retrieve the score of the search result
		 * @return {@link Double} the score
		 */
		public Double getScore() {
			return score;
		}

		@Override
		public int compareTo(InvertedIndex.SearchResult other) {
			int compare = Double.compare(other.getScore(), score);
			if(compare == 0) {
				compare = Integer.compare(other.getCount(), count);
				if(compare == 0) {
					compare = where.compareTo(other.getWhere());
				}
			}
			return compare;
		}
	}

	/**
	 * Data Structure to store everything
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * Stores file names and word counts
	 */
	private final TreeMap<String,Integer> wordCount;

	/**
	 * Class constructor that initializes the inverted index
	 */
	public InvertedIndex(){
		this.invertedIndex = new TreeMap<>();
		this.wordCount = new TreeMap<>();
	}

	/**
	 * @param outputFile where to write to
	 * @throws IOException if IO error occurs
	 */
	public void writeWordCount(Path outputFile) throws IOException {
		SimpleJsonWriter.asDictionary(wordCount, outputFile);
	}

	/**
	 * adds/updates an element to the inverted-index
	 * @param str the word to be added to the index
	 * @param path the path that it was found
	 * @param pos the position if was found in the file
	 */
	public void add(String str, String path, int pos) {
		//add a new string-map element if this is a new string
		invertedIndex.putIfAbsent(str, new TreeMap<String, TreeSet<Integer>>());

		//get the map from the string key
		TreeMap<String, TreeSet<Integer>> pathIndex = invertedIndex.get(str);

		//add a new path - TreeSet element if this is a new path
		pathIndex.putIfAbsent(path, new TreeSet<Integer>());

		//get the TreeSet from the path key
		TreeSet<Integer> indices = pathIndex.get(path);

		//add the position to the TreeSet
		indices.add(pos);

		wordCount.putIfAbsent(path, 0);
		wordCount.put(path, wordCount.get(path)+1);
	}

	/**
	 * adds all information from one index to this current one
	 * @param other the index to add (should be built only from one file)
	 */
	public void addAll(InvertedIndex other) {
		for (String key : other.invertedIndex.keySet()) {
			if (this.invertedIndex.containsKey(key)) {
				TreeMap<String, TreeSet<Integer>> pathIndex = invertedIndex.get(key);
				for (String path : other.invertedIndex.get(key).keySet()) {
					if (pathIndex.containsKey(path)) {
						pathIndex.get(path).addAll(other.invertedIndex.get(key).get(path));
					}
					else {
						pathIndex.put(path, other.invertedIndex.get(key).get(path));
					}
				}
			}
			else {
				this.invertedIndex.put(key, other.invertedIndex.get(key));
			}
		}
		for (String path : other.wordCount.keySet()) {
			this.wordCount.put(path, this.wordCount.getOrDefault(path, 0) + other.wordCount.get(path));
		}


	}


	/**
	 * calls SimpleJsonWriter to write the inverted-index to the given file
	 * @param pathName the path to the output file
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeToFile(String pathName) throws IOException { 
		SimpleJsonWriter.asObject(invertedIndex, Paths.get(pathName));
	}

	/**
	 * returns a map of the wordCount
	 * @return an immutable map that represents the word count
	 */
	public Map<String, Integer> getWordCount() {
		return Collections.unmodifiableMap(new TreeMap<>(wordCount)); 
	}

	/**
	 * return an unmodifiable view of the invertedIndex.keySet()
	 * @return unmodifiable set of keys from the inverted index
	 */
	public Set<String> get() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * returns unmodifiable view of pathnames associated with the given word
	 * @param key the word to search in the index
	 * @return unmodifiable set of pathnames if the word exists, else returns empty set
	 */
	public Set<String> get(String key) {
		if (this.contains(key)) {
			return Collections.unmodifiableSet(invertedIndex.get(key).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * returns unmodifiable view of positions associated with the given word and location
	 * @param key the word to search in the index
	 * @param location the pathname for search for
	 * @return unmodifiable set of pathnames if the word exists at the give location, else returns empty set
	 */
	public Set<Integer> get(String key, String location) {
		if (this.contains(key, location)) {
			return Collections.unmodifiableSet(invertedIndex.get(key).get(location));
		}
		return Collections.emptySet();
	}


	/**
	 * method to determine if a key is in the index
	 * @param key the word to be searched for in the inverted index
	 * @return true if the word is in the index
	 */
	public boolean contains(String key) {
		return invertedIndex.containsKey(key);
	}

	/**
	 * method to determine if the given key was found at the given path name
	 * @param key the word to be searched for in the inverted index
	 * @param pathName a string representing the path to the file
	 * @return true if the key was found at the path 
	 */
	public boolean contains(String key, String pathName) {
		if(invertedIndex.containsKey(key)) {
			return invertedIndex.get(key).containsKey(pathName);
		}
		return false;
	}

	/**
	 * method to determine if the given key was found at the path name at the position given
	 * @param key the word to be searched for in the inverted index
	 * @param pathName a string representing the path to the file
	 * @param pos the position of the word in the file
	 * @return true if the word was found at the path and the position
	 */
	public boolean contains(String key, String pathName, int pos) {
		if(invertedIndex.containsKey(key)) {
			if(invertedIndex.get(key).containsKey(pathName)) {
				return invertedIndex.get(key).get(pathName).contains(pos);
			}
		}
		return false;
	}

	/**
	 * method to return the size of the inverted index
	 * @return int the size of the inverted index
	 */
	public int size() {
		return invertedIndex.size();
	}

	/**
	 * method to return the number of locations associated with the word
	 * @param key the keyword in the inverted index
	 * @return int the number of locations found. If no key exists, returns zero.
	 */
	public int numLocations(String key) {
		if(invertedIndex.containsKey(key)) {
			return invertedIndex.get(key).size();
		}
		return 0;
	}

	/**
	 * method to return the number of positions associated with the given word at the given location
	 * @param key the keyword in the inverted index
	 * @param location the location associated with the word
	 * @return int the number of positions the word was found at the given location
	 */
	public int numPositions(String key, String location) {
		if(this.contains(key, location)) {
			return invertedIndex.get(key).get(location).size();
		}
		return 0;
	}

	@Override
	public String toString() {
		return SimpleJsonWriter.asObject(invertedIndex);
	}

	/**
	 * Searches the inverted index for the query either partially or exact
	 * @param query a collection of cleaned, stemmed, and unique strings
	 * @param exact the type of search to perform
	 * @return a {@link Collection} of SearchResult options, sorted in order of importance
	 */
	public Collection<SearchResult> search(Collection<String> query, boolean exact) {
		if(exact) {
			return exactSearch(query);
		}
		return partialSearch(query);
	}


	/**
	 * exact search function that returns a list of SearchResult objects based on the given query (exact search: returns all matches the <i>exactly</i> match a query word)
	 * @param query a collection of cleaned, stemmed, and unique strings
	 * @return a {@link Collection} of SearchResult options, sorted in order of importance
	 */
	public Collection<SearchResult> exactSearch(Collection<String> query){
		Map<String, SearchResult> searchResults = new HashMap<>();
		List<SearchResult> output = new ArrayList<>();
		// Iterate through each word in the query and perform an exact search on it
		for(String key : query) {
			// search for the word, find all of its locations, and store data into the searchResults collection
			if(invertedIndex.containsKey(key)) { 
				this.addResults(key, searchResults, output);
			}
		}
		Collections.sort(output);
		return output;
	}

	/**
	 * partial search function that returns a list of SearchResult objects based on the given query (partial search: returns all matches that <i>start</i> with a query word)
	 * @param query a collection of cleaned, stemmed, and unique strings
	 * @return a {@link Collection} of SearchResult options, sorted in order of importance
	 */
	public Collection<SearchResult> partialSearch(Collection<String> query){
		Map<String, SearchResult> searchResults = new HashMap<>();
		List<SearchResult> output = new ArrayList<>();

		for(String searchWord : query) {
			Iterator<String> wordIterator = invertedIndex.tailMap(searchWord).keySet().iterator();
			String current;
			while(wordIterator.hasNext()) {
				current = wordIterator.next();
				if(current.startsWith(searchWord)) {
					this.addResults(current, searchResults, output);
				} else {
					break;
				}
			}	
		}
		Collections.sort(output);
		return output;
	}

	/**
	 * modifier method that takes a key and adds all corresponding searchResult data to the searchResult list
	 * @param key the word from the query that was found in the inverted index
	 * @param searchResults the list of SearchResults that will be modified (added upon)
	 * @param output a List of SearchResults to add to
	 */
	private void addResults(String key, Map<String, SearchResult> searchResults, List<SearchResult> output) {
		if(invertedIndex.containsKey(key)) { 
			for (String location : invertedIndex.get(key).keySet()) {
				if(!searchResults.containsKey(location)) {
					SearchResult result = new SearchResult(location);
					searchResults.put(location, result);
					output.add(result);
				}
				searchResults.get(location).update(key);
			}
		}
	}
}