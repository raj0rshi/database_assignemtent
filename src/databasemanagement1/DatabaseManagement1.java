/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databasemanagement1;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Level;

/**
 *
 * @author rajor
 */
public class DatabaseManagement1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);

        webClient.getOptions().setThrowExceptionOnScriptError(false);
        Scanner sc = new Scanner(System.in);
        String keyword = sc.nextLine();

       // HtmlPage page = webClient.getPage("http://google.com");
       HtmlPage page = webClient.getPage("https://scholar.google.com/scholar?hl=en&q="+URLEncoder.encode(keyword,"UTF-8"));
        System.out.println(page.asXml());
    }


}
