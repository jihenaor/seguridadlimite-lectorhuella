package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ClienteRest {

  private static final String URL_SEG = "http://seguridadallimite.com:8080/seguridadallimite-1.0/api/";

  public static String sendPost(String json, String nombreWebService) throws Exception {
        String s;
        int resp;
        String r = "";

        s = URL_SEG + nombreWebService;

        try {
          URL url = new URL(s);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setDoOutput(true);
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type", "application/json");

          OutputStream os = conn.getOutputStream();
          os.write(json.getBytes());
          os.flush();

          resp = conn.getResponseCode();

          BufferedReader br = new BufferedReader(new InputStreamReader(
         (conn.getInputStream())));

          String output;
          System.out.println("Output from Server .... \n");
          while ((output = br.readLine()) != null) {
            r += output;
          }

          conn.disconnect();

          if (resp == 200) {
            return "ok";
          } else {
            return r;
          }
        } catch (MalformedURLException e) {

          e.printStackTrace();
          throw e;
        } catch (IOException e) {
          e.printStackTrace();
          throw e;
        }
    }
    
    public static String getText(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(URL_SEG + url).openConnection();

        //add headers to the connection, or check the status if desired..

        // handle error response code it occurs
        int responseCode = connection.getResponseCode();
        InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                inputStream));

        StringBuilder response = new StringBuilder();
        String currentLine;

        while ((currentLine = in.readLine()) != null) 
            response.append(currentLine);

        in.close();

        return response.toString();
    }
}
