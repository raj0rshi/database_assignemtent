package databasemanagement1;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseManagement1 {

    public static void main(String[] args) throws IOException, Exception {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http.client.*").setLevel(Level.OFF);
        Scanner sc = new Scanner(new File("input.txt"));
        File f_table=new File("table.txt");
        f_table.createNewFile();
        PrintWriter pw_table=new PrintWriter(f_table);
        pw_table.println("Name\t Total Person\t Average H-Index");
        while (sc.hasNextLine()) {

            String keyword = sc.nextLine().trim();
            if (keyword.length() == 0) {
                continue;
            }
            WebClient webClient = new WebClient();
            webClient.getOptions().setUseInsecureSSL(true);

            webClient.getOptions().setThrowExceptionOnScriptError(false);

            //firstName + " "+lastName;  //sc.nextLine();
            // HtmlPage page = webClient.getPage("http://google.com");
            // HtmlPage page = webClient.getPage("https://scholar.google.com/scholar?hl=en&q="+URLEncoder.encode(keyword,"UTF-8"));
            // System.out.println(page.asXml());
            // There will be two list. 1) Author list, 2) Publication list
            Boolean flag = true;
            HtmlPage page = null;
            String nextAuthorLink = "";
            int totalAuthor = 0, totalHindex = 0;

            File f_out = new File("output.txt");
            PrintWriter pw_f = new PrintWriter(f_out);
            String page_link = null;
            while (true) {
                try {
                    if (flag) {
                        page_link = "https://scholar.google.com/citations?view_op=search_authors&mauthors=" + URLEncoder.encode(keyword, "UTF-8");
                        page = webClient.getPage(page_link);
                    } else {
                        page_link = "https://scholar.google.com/citations?view_op=search_authors&hl=en&mauthors=" + keyword + "&after_author=" + nextAuthorLink;
                        page = webClient.getPage(page_link);
                    }

                    int userNumber = page.getByXPath("//div[@class='gsc_1usr_photo']").size();
                    for (int i = 0; i < userNumber; i++) {

                        //profile variables
                        String name = null, homepage = null, email = null, h_index = null, areas_of_interests = null, position = null, affiliation = null;
                        final HtmlDivision div = (HtmlDivision) page.getByXPath("//div[@class='gsc_1usr_photo']").get(i);

                        String tem = div.asXml();
                        //System.out.println(tem );      // div of users profile list

                        int j = 0;
                        String link = "";
                        for (String retval : tem.split("href=\"")) { // searching the link
                            ++j;
                            if (j == 2) {
                                link = retval;  // got the value at 2nd attempt
                                break;
                            }

                        }

                        for (String retval : link.split("\"")) {
                            link = retval;
                            break;

                        }
                        //System.out.println("***********************************************");
                        //System.out.println(link);     // personal link of each scholar profile

                        try {
                            // Now, hit on the personal page
                            page_link = "https://scholar.google.com/" + link;
                            HtmlPage page2 = webClient.getPage(page_link);        // visiting profile page

                            final HtmlElement el = page2.getHtmlElementById("gsc_prf_in");
                            String authorName = el.asText();

                            //System.out.println(page2.asXml());
                            totalAuthor++;
                            //  totalHindex             // problem if data missing?

                            Files.createDirectories(Paths.get("profiles"));
                            File f = new File("profiles/" + totalAuthor + ".htm");
                            if (!f.exists()) {
                                f.createNewFile();
                            }
                            PrintWriter pw = new PrintWriter(f);
                            pw.write(page2.asXml());
                            pw.close();

                            HtmlElement citation_table_rows = (HtmlElement) page2.getElementById("gsc_rsb_st").getChildNodes().get(0);

                            boolean skip = true;
                            for (DomNode row : citation_table_rows.getChildren()) {
                                if (skip) {
                                    skip = false;
                                    continue;
                                }
                                DomNode cell = row.getChildNodes().get(0);
                                if ("h-index".equals(cell.asText().trim())) {
                                    h_index = row.getChildNodes().get(1).asText();
                                    
                                    totalHindex+=Integer.parseInt(h_index);
                                }

                            }
                            HtmlElement InfoDiv = (HtmlElement) page2.getElementById("gsc_prf_i");
                            DomNodeList<DomNode> Infos = InfoDiv.getChildNodes();
                            String info_0 = Infos.get(1).asText();
                            String info_1 = Infos.get(2).asText();
                            String info_2 = Infos.get(3).asText();
                            String info_3 = Infos.get(4).asText();

                            position = get_position(info_1);
                            affiliation = get_affiliation(Infos.get(2));
                            name = info_0;
                            //find homepage
                            DomNode HomePageE = (DomNode) Infos.get(4).getChildNodes().get(1);
                            if (HomePageE != null) {
                                homepage = HomePageE.getAttributes().getNamedItem("href").getNodeValue();
                            }
                            //find email
                            Set<String> emails = find_email(info_3, homepage);
                            email = "";
                            for (String em : emails) {
                                email += em + ", ";
                            }

                            //areas_of_interests
                            areas_of_interests = info_2;

                            System.out.println("**************");
                            System.out.println("Name:" + name);
                            System.out.println("Email:" + email);
                            System.out.println("Position: " + position);
                            System.out.println("Affiliation: " + affiliation);
                            System.out.println("Homepage: " + homepage);
                            System.out.println("h index: " + h_index);
                            System.out.println("Area of Interest:" + areas_of_interests);

                            pw_f.println("------------------");
                            pw_f.println("Name:" + name);
                            pw_f.println("Email:" + email);
                            pw_f.println("Position: " + position);
                            pw_f.println("Affiliation: " + affiliation);
                            pw_f.println("Homepage: " + homepage);
                            pw_f.println("h index: " + h_index);
                            pw_f.println("Area of Interest:" + areas_of_interests);
                            pw_f.println("------------------");
                            System.out.println("Info 0:" + info_0);
                            System.out.println("Info 1:" + info_1);
                            System.out.println("Info 2:" + info_2);
                            System.out.println("Info 3:" + info_3);
                            System.out.println("**************");
                            Thread.sleep(2 * 1000);
                        } catch (Exception e) {
                            System.out.println("error: " + page_link);
                            Thread.sleep(120 * 1000);
                        }
                    }// end of 1st 10 authors

                } catch (Exception e) {
                    System.out.println("error: " + page_link);
                    Thread.sleep(120 * 1000);
                }

                pw_f.close();
                String content = page.asXml();
                nextAuthorLink = findingValue(content, "after_author\\x3d", "\\x26astart");

                int firstIndex = content.indexOf("after_author\\x3d");

                if (nextAuthorLink == "NO") {
                    System.out.println("No more pages");
                    break;
                } else {
                    flag = false;  //continue for next page
                }

            }// end of while loop
            
            pw_table.println(keyword+"\t"+ totalAuthor+"\t"+ (double)totalHindex/(double)totalAuthor);
            pw_table.flush();
            System.out.println("Total author: " + totalAuthor);
        }
        
        pw_table.close();
    }// end of main

    static String get_position(String x) {
        x = x.toLowerCase().trim();

        if (x.contains("assistant") && x.contains("professor")) {
            return "assistant professor";
        }
        if (x.contains("chair")) {
            return "chair";
        }
        if (x.contains("student") && (x.contains("ph.d") || x.contains("phd"))) {
            return "phd student";
        }
        if (x.contains("student") && (x.contains("graduate"))) {
            return "phd student";
        }
        if (x.contains("student")) {
            return "student";
        }
        if (x.contains("candidate")) {
            return "candidate";
        }
        if (x.contains("professor")) {
            return "professor";
        }

        if (x.contains("research") && x.contains("scientist")) {
            return "research scientist";
        }
        if (x.contains("research")) {
            return "research";
        }

        if (x.contains("scientist")) {
            return "scientist";
        }
        if (x.contains("company") || x.contains("ltd") || x.contains("limited") || x.contains("inc.")) {
            return "job";
        }
        return null;
    }

    static String findingValue(String content, String fistKey, String secondKey) {
        String result = "NO";
        int firstIndex = content.indexOf(fistKey);
        int wordLengthOfFirstKey = fistKey.length();
        if (firstIndex == - 1) {
            return result;
        } else {

            int lastIndex = content.indexOf(secondKey);

            while (lastIndex <= firstIndex) { // lastIndex must > than firstIndex
                lastIndex = content.indexOf(secondKey, lastIndex + 1);    // taking next lastIndex
            }

            //System.out.println( firstIndex + "       "+ lastIndex);
            result = content.substring(firstIndex + wordLengthOfFirstKey, lastIndex);   // add first word length

            return result;
        }

    }

    static Set<String> find_email(String input, String homepage) {
        String regex = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        regex = "([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Za-z]{2,4})";

        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(input);
        Set<String> emails = new HashSet<String>();
        while (matcher.find()) {
            emails.add(matcher.group());
        }

        if (emails.size() == 0 && (homepage != null && homepage.trim().length() != 0)) {
            try {
                WebClient webClient = new WebClient();
                webClient.getOptions().setUseInsecureSSL(true);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                HtmlPage page = webClient.getPage(homepage);
                input = page.asXml();
                // System.out.println(input);

                matcher = p.matcher(input);
                emails = new HashSet<String>();
                while (matcher.find()) {
                    emails.add(matcher.group());
                }
            } catch (Exception ex) {
            }

        }
        return emails;
    }

    static String get_affiliation(DomNode x) {

        String affiliation = null;
        for (DomNode child : x.getChildNodes()) {
            if ("a".equals(child.getNodeName())) {
                affiliation = child.asText();
            }
            if ("#text".equals(child.getNodeName())) {
                String text = child.asText().toLowerCase();
                boolean isaff = text.contains("inc.")
                        || text.contains("ltd.")
                        || text.contains("institure")
                        || text.contains("organization")
                        || text.contains("limited");
                if (isaff) {
                    affiliation = text;
                }
            }
        }
        return affiliation;
    }

}
