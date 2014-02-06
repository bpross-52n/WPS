/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.conflation.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedHttpConnector {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticatedHttpConnector.class);
	private static String XML_PRE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static String proxyServer;
	private static boolean noProxyDefined;


	private URI serviceURL;
	private DefaultHttpClient serverConnection;
	private Authentication authentication;

	public AuthenticatedHttpConnector(URI host, Authentication auth) {
		this(host);
		this.authentication = auth;
	}

	public AuthenticatedHttpConnector(URI host) {
		this.serviceURL = host;
		this.serverConnection = new DefaultHttpClient();
	}

	public HttpResponse executeHttpPost(String content) throws IOException {
		HttpPost httpPost = createHttpPost(content);

		BasicHttpContext authContext = createAuthContext();

		return serverConnection.execute(httpPost, authContext);
	}



	public HttpResponse executeHttpGet(String query) throws IOException {
		String getUrl = this.serviceURL.toString() + query;
		if (useProxyServer()) {
			getUrl = getProxyServerQuery(getUrl);
		}

		HttpGet get = new HttpGet(getUrl);

		BasicHttpContext authContext = createAuthContext();

		return serverConnection.execute(get, authContext);
	}


	private static synchronized boolean useProxyServer() {
//		if (proxyServer == null && !noProxyDefined) {
//			Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(LocalAlgorithmRepository.class.getCanonicalName());
//			for (Property property : propertyArray){
//				if (!property.getActive()) continue;
//				if (property.getName().equalsIgnoreCase("proxy-wfs-server")){
//					proxyServer = property.getStringValue();
//				}
//			}
//
//			if (proxyServer == null) {
//				noProxyDefined = true;
//			}
//		}

		return false;
	}

	private static String getProxyServerQuery(String proxyRequest) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(proxyServer);
		sb.append("?request=");
		sb.append(URLEncoder.encode(proxyRequest, "UTF-8"));
		return sb.toString();
	}

	private BasicHttpContext createAuthContext() {
		BasicHttpContext authContext = null;
		if (authentication != null) {
			authContext = new BasicHttpContext();
			BasicCredentialsProvider credProvider = new BasicCredentialsProvider();
			credProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(authentication.getUser(), authentication.getPassword()));
			authContext.setAttribute(ClientContext.CREDS_PROVIDER, credProvider);	
		}
		return authContext;
	}

	private HttpPost createHttpPost(String requestString) {
		HttpPost post = new HttpPost(this.serviceURL);
		try {
			StringEntity se = createStringEntity(requestString);
			post.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
		return post;
	}

	protected StringEntity createStringEntity(String contents) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(XML_PRE);
		sb.append(contents);
		
		StringEntity result = new StringEntity(sb.toString());
		result.setContentType("text/xml; charset=utf-8");
		return result;
	}

	public static void downloadRemoteFile(URI uri, File targetFile) throws IOException {
		AuthenticatedHttpConnector client = new AuthenticatedHttpConnector(uri);
		HttpResponse resp = client.executeHttpGet("");

		if (resp.getEntity() == null || resp.getStatusLine().getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
			throw new IOException("Could not download file at "+uri);
		}

		ReadableByteChannel rbc = Channels.newChannel(resp.getEntity().getContent());
		FileOutputStream fos = new FileOutputStream(targetFile);
		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		fos.flush();
		fos.close();		
	}

}
