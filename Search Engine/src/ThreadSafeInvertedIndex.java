import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * @author matthew
 * Thread safe version of InvertedIndex
 */
public class ThreadSafeInvertedIndex extends InvertedIndex{
	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;

	/**
	 * Class constructor that initializes the inverted index
	 */
	public ThreadSafeInvertedIndex(){
		this.lock = new SimpleReadWriteLock();
	}

	/**
	 * @param outputFile where to write to
	 * @throws IOException if IO error occurs
	 */
	@Override
	public void writeWordCount(Path outputFile) throws IOException {
		lock.readLock().lock();

		try {
			super.writeWordCount(outputFile);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * adds/updates an element to the inverted-index
	 * @param str the word to be added to the index
	 * @param path the path that it was found
	 * @param pos the position if was found in the file
	 */
	@Override
	public void add(String str, String path, int pos) {
		lock.writeLock().lock();

		try {
			super.add(str, path, pos);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * adds all information from one index to this current one
	 * @param other the index to add (should be built only from one file)
	 */
	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();

		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * calls SimpleJsonWriter to write the inverted-index to the given file
	 * @param pathName the path to the output file
	 * 
	 * @throws IOException if an IO error occurs
	 */
	@Override
	public void writeToFile(String pathName) throws IOException { 
		lock.readLock().lock();

		try {
			super.writeToFile(pathName);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Map<String, Integer> getWordCount() {
		lock.readLock().lock();

		try {
			return super.getWordCount();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * return an unmodifiable view of the invertedIndex.keySet()
	 * @return unmodifiable set of keys from the inverted index
	 */
	@Override
	public Set<String> get() {
		lock.readLock().lock();

		try {
			return super.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * returns unmodifiable view of pathnames associated with the given word
	 * @param key the word to search in the index
	 * @return unmodifiable set of pathnames if the word exists, else returns empty set
	 */
	@Override
	public Set<String> get(String key) {
		lock.readLock().lock();

		try {
			return super.get(key);
		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * returns unmodifiable view of positions associated with the given word and location
	 * @param key the word to search in the index
	 * @param location the pathname for search for
	 * @return unmodifiable set of pathnames if the word exists at the give location, else returns empty set
	 */
	@Override
	public Set<Integer> get(String key, String location) {
		lock.readLock().lock();

		try {
			return super.get(key, location);
		} finally {
			lock.readLock().unlock();
		}


	}


	/**
	 * method to determine if a key is in the index
	 * @param key the word to be searched for in the inverted index
	 * @return true if the word is in the index
	 */
	@Override
	public boolean contains(String key) {
		lock.readLock().lock();

		try {
			return super.contains(key);
		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * method to determine if the given key was found at the given path name
	 * @param key the word to be searched for in the inverted index
	 * @param pathName a string representing the path to the file
	 * @return true if the key was found at the path 
	 */
	@Override
	public boolean contains(String key, String pathName) {
		lock.readLock().lock();

		try {
			return super.contains(key, pathName);
		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * method to determine if the given key was found at the path name at the position given
	 * @param key the word to be searched for in the inverted index
	 * @param pathName a string representing the path to the file
	 * @param pos the position of the word in the file
	 * @return true if the word was found at the path and the position
	 */
	@Override
	public boolean contains(String key, String pathName, int pos) {
		lock.readLock().lock();

		try {
			return super.contains(key, pathName, pos);
		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * method to return the size of the inverted index
	 * @return int the size of the inverted index
	 */
	@Override
	public int size() {
		lock.readLock().lock();

		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * method to return the number of locations associated with the word
	 * @param key the keyword in the inverted index
	 * @return int the number of locations found. If no key exists, returns zero.
	 */
	@Override
	public int numLocations(String key) {
		lock.readLock().lock();

		try {
			return super.numLocations(key);
		} finally {
			lock.readLock().unlock();
		}


	}

	/**
	 * method to return the number of positions associated with the given word at the given location
	 * @param key the keyword in the inverted index
	 * @param location the location associated with the word
	 * @return int the number of positions the word was found at the given location
	 */
	@Override
	public int numPositions(String key, String location) {
		lock.readLock().lock();

		try {
			return super.numPositions(key, location);
		} finally {
			lock.readLock().unlock();
		}


	}

	@Override
	public String toString() {
		lock.readLock().lock();

		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}


	/**
	 * exact search function that returns a list of SearchResult objects based on the given query (exact search: returns all matches the <i>exactly</i> match a query word)
	 * @param query a collection of cleaned, stemmed, and unique strings
	 * @return a {@link Collection} of SearchResult options, sorted in order of importance
	 */
	@Override
	public Collection<SearchResult> exactSearch(Collection<String> query){
		lock.readLock().lock();

		try {
			return super.exactSearch(query);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * partial search function that returns a list of SearchResult objects based on the given query (partial search: returns all matches that <i>start</i> with a query word)
	 * @param query a collection of cleaned, stemmed, and unique strings
	 * @return a {@link Collection} of SearchResult options, sorted in order of importance
	 */
	@Override
	public Collection<InvertedIndex.SearchResult> partialSearch(Collection<String> query){
		lock.readLock().lock();

		try {
			return super.partialSearch(query);
		} finally {
			lock.readLock().unlock();
		}
	}
}
