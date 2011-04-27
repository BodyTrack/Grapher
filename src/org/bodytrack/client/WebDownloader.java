package org.bodytrack.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * A class providing static methods to simplify server communication.
 */
public final class WebDownloader {
	// Ensures that we don't actually initialize WebDownloader objects
	private WebDownloader() { }

	/**
	 * Runs a GET request asynchronously, calling the appropriate
	 * callback method whenever the server responds.
	 *
	 * @param url
	 * 		the URL from which to request information
	 * @param callback
	 * 		the object whose {@link DownloadAlertable#onSuccess(String)}
	 * 		method will be called if the request succeeds, and whose
	 * 		{@link DownloadAlertable#onFailure(Request)} method will
	 * 		be called if the request fails
	 * @throws NullPointerException
	 * 		if either url or callback is <tt>null</tt>
	 */
	public static void doGet(String url, final DownloadAlertable callback) {
		if (url == null)
			throw new NullPointerException("Cannot request from a null URL");
		if (callback == null)
			throw new NullPointerException("Cannot use a null callback");

		// Send request to server and catch any errors.
		RequestBuilder builder =
			new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onError(Request request,
						Throwable exception) {
					callback.onFailure(request);
				}

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					if (isSuccessful(response))
						callback.onSuccess(response.getText());
					else
						callback.onFailure(request);
				}
			});
		} catch (RequestException e) {
			callback.onFailure(null);
		}
	}

	/**
	 * Returns <tt>true</tt> if and only if response should be
	 * considered a success.
	 *
	 * @param response
	 * 		the response to check
	 * @return
	 * 		<tt>true</tt> if and only if response should be considered
	 * 		to be successful and thus a container of useful content
	 * @throws NullPointerException
	 * 		if response is <tt>null</tt>
	 */
	public static boolean isSuccessful(Response response) {
		if (response == null)
			throw new NullPointerException("Can't check a null response");

		int sc = response.getStatusCode();

		// Anything in the 200 range, or a 304, is considered a success
		return (sc >= 200 && sc < 300) || sc == 304;
	}

	/**
	 * An interface defining the callbacks which classes must implement
	 * in order to use the methods of this class.
	 */
	public interface DownloadAlertable {
		/**
		 * Called whenever there is a successful load of the data.
		 *
		 * @param response
		 * 		the non-<tt>null</tt> body of the response, as a string
		 */
		public void onSuccess(String response);

		/**
		 * Called whenever there is some failure in attempting to load
		 * the data.
		 *
		 * <p>Our notion of failure also includes an HTTP status code
		 * which does not indicate success (e.g. a 404).</p>
		 *
		 * @param failed
		 * 		the response that generated the error.  In some cases,
		 * 		this may be <tt>null</tt>
		 */
		// TODO: Possibly have a custom object that gives more
		// information than this does
		public void onFailure(Request failed);
	}
}
