/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ebookdownloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author David
 */
public class Updater extends Thread{
    int count;
    MainFrame r;
    String dl = "book/";
    String path;
    int limit;
    boolean running;
    Updater(int c, MainFrame ref, String p, int l) {
        count = c;
        r = ref;
        path = p + "\\";
        limit = l;
        running = true;
    }
    
    public void terminate(){
        running = false;        
    }

    private void DescargaArchivo(int actual){
        URL url;
        String document = "";
        String linkD = "";
        String bookT = "";
        try {
            url = new URL("http://it-ebooks.info/" + dl + actual);
            r.UpdateStatus("http://it-ebooks.info/" + dl + actual);
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                document += inputLine;
            }
            in.close();
            r.UpdateStatus("URL responded successfully, parsing document");

            Document doc = Jsoup.parse(document);
            Elements h1Tags = doc.select("h1");
            int element = 0;
            try {
                r.UpdateStatus("Found title: " + h1Tags.get(0).text());
                bookT = h1Tags.get(0).text();
                Elements links = doc.select("a[href]");
                while (element < links.size()) {
                    r.UpdateStatus("Found link: " + links.get(element).text() + ": " + links.get(element).absUrl("href"));
                    if (links.get(element).absUrl("href").matches(".*filepi.*")) {
                        linkD = links.get(element).absUrl("href");
                    }
                    element++;
                }

                r.UpdateStatus("Found download link: " + linkD);
                r.UpdateStatus("Downloading " + linkD);

                String fileName = path; //The file that will be saved on your computer

                String fileURL = linkD;
                String saveDir = fileName;
                try {
                    HttpDownloadUtility.downloadFile(fileURL, saveDir, r);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IndexOutOfBoundsException e) {
                r.UpdateStatus("Book not available or doesn't exists, skipping...");
                r.UpdateCount(false);
            }
        } catch (MalformedURLException ex) {
            //Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            r.ShowErrorDialog("There was a problem parsing the response");
        } catch (IOException ex) {
            //Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            r.ShowErrorDialog("URL structure may be wrong, please check");
        }    
    }
    
    @Override
    public void run() {
        for (int i = count; i < (count + limit); i++) {
            if (running) {
                DescargaArchivo(i);
            } else {
                break;
            }
        }
    }
}
