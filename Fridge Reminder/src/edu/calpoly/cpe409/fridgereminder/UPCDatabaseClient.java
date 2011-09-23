package edu.calpoly.cpe409.fridgereminder;

import java.util.HashMap;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcStruct;

public class UPCDatabaseClient {
	static {
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
	}

	public static String lookup(String upc, String format) {
		String text = null;
		String normalizedFormat = format.toUpperCase();

		if (normalizedFormat.startsWith("EAN"))
			format = "ean";
		else if (normalizedFormat.startsWith("UPC"))
			format = "upc";

		try {
			XmlRpcClient client = new XmlRpcClient(
					"http://www.upcdatabase.com/xmlrpc", false);

			HashMap<String, String> callParams = new HashMap<String, String>();
			callParams.put("rpc_key",
					"d558c6c9e9a45d7e732172a344725ec7c700abb1");
			callParams.put(format, upc);

			XmlRpcStruct result = (XmlRpcStruct) client.invoke("lookup",
					new Object[] { callParams });

			if (result.size() > 0
					&& result.get("message").toString()
							.equalsIgnoreCase("Database entry found")) {

				text = result.get("description").toString() + " "
						+ result.get("size").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
}
