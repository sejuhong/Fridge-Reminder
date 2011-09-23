package edu.calpoly.cpe409.fridgereminder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Scanner;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class ExpirationDatabase {
	public static int lookUp(String name, Context context)
			throws UnknownHostException {
		int toReturn = -1;
		Scanner in = null;

		try {
			URL url = new URL(context.getResources().getString(
					R.string.expiration_database_url)
					+ "?name=" + URLEncoder.encode(name, "UTF-8"));
			in = new Scanner(new InputStreamReader(url.openStream()))
					.useDelimiter("\n");
			if (in.hasNext())
				toReturn = Integer.parseInt(in.next().trim());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// Do nothing
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return toReturn;
	}
}
