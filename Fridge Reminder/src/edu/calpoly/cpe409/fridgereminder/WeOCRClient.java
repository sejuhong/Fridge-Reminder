package edu.calpoly.cpe409.fridgereminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.util.Log;

// Adapted from Wordsnap-OCR (Spiros Papadimitriou <spapadim@cs.cmu.edu>)
public class WeOCRClient {
	private static final String TAG = WeOCRClient.class.getSimpleName();

	private static final int HTTP_TIMEOUT = 6000; // in msec

	private static final String USER_AGENT_STRING = "fridgereminder/0.1"
			+ " (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ")"
			+ " Apache-HttpClient/UNAVAILABLE";

	private String mEndpoint;
	private DefaultHttpClient mHttpClient;

	public WeOCRClient(String endpoint) {
		mEndpoint = endpoint;
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, USER_AGENT_STRING);
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		mHttpClient = new DefaultHttpClient(params);
	}

	public String doOCR(Bitmap img) throws IOException {
		Log.i(TAG, "Sending OCR request to " + mEndpoint);
		HttpPost post = new HttpPost(mEndpoint);
		post.setEntity(new WeOCRFormEntity(img));

		// Send request and obtain response
		BufferedReader r = null;
		try {
			HttpResponse resp = mHttpClient.execute(post);
			r = new BufferedReader(new InputStreamReader(resp.getEntity()
					.getContent(), "utf-8"));
		} catch (NullPointerException npe) {
			Log.e(TAG, "Null entity?", npe);
			throw new IOException("HTTP request failed");
		} catch (HttpResponseException re) {
			Log.e(TAG, "HTTP response exception", re);
			throw new IOException("HTTP request failed");
		}

		// Parse response
		String status = r.readLine();
		if (status.length() != 0) {

			for (String line = r.readLine(); line != null; line = r.readLine()) {
				status += line;
			}

			throw new IOException("WeOCR failed with status: " + status);
		}
		StringBuilder sb = new StringBuilder(); // XXX just use string?
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString().trim();
	}
}
