package edu.utah.ece.async.sboldesigner.sbol;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;

public class WebOfRegistriesUtil {

	public void initRegistries() {
		try {
			setupTrust();

			WebOfRegistry[] wors = sendGETRequest();

			addRegistries(wors);
		} catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
			System.out.println("Fetching Web of Registries failed: please check your internet connection.");
		}
	}

	private void addRegistries(WebOfRegistry[] wors) {
		HashSet<String> set = new HashSet<>();

		Iterator<Registry> it = Registries.get().iterator();
		while (it.hasNext()) {
			Registry r = it.next();
			set.add(r.getLocation());
		}

		for (WebOfRegistry wor : wors) {
			if (!set.contains(wor.instanceUrl)) {
				Registries.get().add(new Registry(wor.name, wor.description, wor.instanceUrl, wor.uriPrefix));
			}
		}

		Registries.get().save();
	}

	private WebOfRegistry[] sendGETRequest() throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL("https://wor.synbiohub.org/instances/");

		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		String json = IOUtils.toString(con.getInputStream());
		Gson gson = new Gson();
		WebOfRegistry[] wors = gson.fromJson(json, WebOfRegistry[].class);

		con.disconnect();

		return wors;
	}

	private void setupTrust() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	private class WebOfRegistry {
		int id;
		String uriPrefix;
		String instanceUrl;
		String description;
		String name;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("id: " + id);
			sb.append(System.lineSeparator());
			sb.append("uriPrefix: " + uriPrefix);
			sb.append(System.lineSeparator());
			sb.append("instanceUrl: " + instanceUrl);
			sb.append(System.lineSeparator());
			sb.append("description: " + description);
			sb.append(System.lineSeparator());
			sb.append("name: " + name);
			sb.append(System.lineSeparator());
			return sb.toString();
		}
	}
}
