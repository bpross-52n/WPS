
package org.n52.wps.server.algorithm.conflation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostClient {

    public static String sendRequest(String targetURL, String payload) throws IOException {
        // Construct data

        // Send data
        URL url = new URL(targetURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payload);
        wr.flush();

        // Get the response
        StringBuffer response = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ( (line = rd.readLine()) != null) {
            response = response.append(line + "\n");
        }
        wr.close();
        rd.close();

        String responseString = response.toString();
        return responseString;
    }
    
    public static InputStream sendRequestForInputStream(String targetURL, String payload) throws IOException {
        // Construct data

        // Send data
        URL url = new URL(targetURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        wr.write(payload);
        wr.flush();

        // Get the response
        return conn.getInputStream();
    }
}
