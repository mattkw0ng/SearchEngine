import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and returns HTML content if
 * possible.
 *
 * @see HttpsFetcher
 */
public class HtmlFetcher {

  /**
   * Returns {@code true} if and only if there is a "Content-Type" header and the first value of
   * that header starts with the value "text/html" (case-insensitive).
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return {@code true} if the headers indicate the content type is HTML
   */
  public static boolean isHtml(Map<String, List<String>> headers) {
	if(headers.containsKey("Content-Type")) {
		if(headers.get("Content-Type").get(0).toLowerCase().startsWith("text/html")) {
			return true;
		}
	}
	return false;
  }

  /**
   * Parses the HTTP status code from the provided HTTP headers, assuming the status line is stored
   * under the {@code null} key.
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return the HTTP status code or -1 if unable to parse for any reasons
   */
  public static int getStatusCode(Map<String, List<String>> headers) {
	String statusCode = headers.get(null).get(0);
	int num = Integer.parseInt(statusCode.split(" ")[1]);
    return num;
  }

  /**
   * Returns {@code true} if and only if the HTTP status code is between 300 and 399 (inclusive) and
   * there is a "Location" header with at least one value.
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return {@code true} if the headers indicate the content type is Redirect
   */
  public static boolean isRedirect(Map<String, List<String>> headers) {
    if(getStatusCode(headers) > 299 && getStatusCode(headers) < 400) {
    	return true;
    }
    return false;
  }

  /**
   * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status code is 200 and the
   * content type is HTML, returns the HTML as a single string. If the status code is a valid
   * redirect, will follow that redirect if the number of redirects is greater than 0. Otherwise,
   * returns {@code null}.
   *
   * @param url the url to fetch
   * @param redirects the number of times to follow redirects
   * @return the html or {@code null} if unable to fetch the resource or the resource is not html
   *
   * @see HttpsFetcher#openConnection(URL)
   * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
   * @see HttpsFetcher#getHeaderFields(BufferedReader)
   * @see HttpsFetcher#getContent(BufferedReader)
   *
   * @see String#join(CharSequence, CharSequence...)
   *
   * @see #isHtml(Map)
   * @see #isRedirect(Map)
   */
  public static String fetch(URL url, int redirects) {
    try {
    	Map<String, List<String>> headers = HttpsFetcher.fetchURL(url);
    	if(isRedirect(headers)) {
	    	for (int i = 0 ; i < redirects ; i ++) {
	    		if(isRedirect(headers)) {
	    			headers = HttpsFetcher.fetchURL(headers.get("Location").get(0));
	    		} else {
	    			break;
	    		}
	    	}
    	}
    	if(getStatusCode(headers) > 199 && getStatusCode(headers) < 300 && isHtml(headers)) {
    		return String.join("\n",headers.get("Content"));
    	}
		
	} catch (UnknownHostException e) {
		System.out.println("Error: Unknown host");
	} catch (IOException e) {
		System.out.println("Error: IO exception");
	}
	return null;
    
  }

  /**
   * Converts the {@link String} url into a {@link URL} object and then calls
   * {@link #fetch(URL, int)}.
   *
   * @param url the url to fetch
   * @param redirects the number of times to follow redirects
   * @return the html or {@code null} if unable to fetch the resource or the resource is not html
   *
   * @see #fetch(URL, int)
   */
  public static String fetch(String url, int redirects) {
    try {
      return fetch(new URL(url), redirects);
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Converts the {@link String} url into a {@link URL} object and then calls
   * {@link #fetch(URL, int)} with 0 redirects.
   *
   * @param url the url to fetch
   * @return the html or {@code null} if unable to fetch the resource or the resource is not html
   *
   * @see #fetch(URL, int)
   */
  public static String fetch(String url) {
    return fetch(url, 0);
  }

  /**
   * Calls {@link #fetch(URL, int)} with 0 redirects.
   *
   * @param url the url to fetch
   * @return the html or {@code null} if unable to fetch the resource or the resource is not html
   */
  public static String fetch(URL url) {
    return fetch(url, 0);
  }
}
