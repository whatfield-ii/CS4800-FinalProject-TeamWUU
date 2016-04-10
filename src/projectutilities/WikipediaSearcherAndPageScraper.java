/* Copyright (c) 2016 William Hatfield, Utkarshani Jaimini, Uday Sagar Panjala.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details. <-- LICENSE.md -->
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package projectutilities;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.HttpResponse;
import com.jaunt.MultipleFound;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.SearchException;
import com.jaunt.UserAgent;
import java.util.HashMap;

/**
 *
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class WikipediaSearcherAndPageScraper {
    //
    private final String WURL = "https://en.wikipedia.org/w/index.php?search=";
    //
    private final HashMap<String, SearchResult> RESULTS_MAP = new HashMap<>();
    //
    private final UserAgent USER_AGENT = new UserAgent();
    
    
    /**
     * 
     * @param toSearchFor 
     */
    public void searchWikipedia(String toSearchFor) {
        try {
            USER_AGENT.visit(WURL);
            USER_AGENT.doc.fillout("search", toSearchFor);
            USER_AGENT.doc.submit("search");
            this.updateResultsMap();
        } catch (ResponseException ex) {
            HttpResponse R = ex.getResponse();
            if (R != null) {
                System.err.println("Requested URL: " + R.getRequestedUrlMsg());
                System.err.println("Error Message: " + R.getMessage());
                System.err.println("Error Code: " + R.getStatus());
            } else {
                System.err.println("Connection Error - No Response!");
                System.err.println("ResponseException: " + ex.getMessage());
            }
        } catch (MultipleFound ex) {
            System.err.println("MultipleFound: " + ex.getMessage());
        } catch (NotFound ex) {
            System.err.println("NotFound: " + ex.getMessage());
        } catch (SearchException ex) {
            System.err.println("SearchException: " + ex.getMessage());
        }
    }
    
    /**
     * 
     */
    private void updateResultsMap() {
        try {
            RESULTS_MAP.clear();    // clear previous contents of the result map
            String resultListString = "<ul class=\"mw-search-results\">";
            Element resList = USER_AGENT.doc.findFirst(resultListString);
            Elements results = resList.findEvery("<li>"); // get each list item
            for (Element res : results) {
                String title = res.findFirst("<div>").findFirst("<a>").getAt("title");
                String href = res.findFirst("<div>").findFirst("<a>").getAt("href");
                Elements divs = res.findEvery("<div>");
                for (Element text : divs) {
                    System.out.println(text.innerText());
                }
                System.out.println("");
            }
        } catch (NotFound ex) {
            System.err.println("NotFound: " + ex.getMessage());
        }
        
    }
    
    /**
     * 
     */
    private class SearchResult {
        // Strings for the Search Result Title, Hyperlink, and Quick Description
        public final String title, href, text;
        // The Default Search Result Constructor
        public SearchResult(String title, String href, String text) {
            this.title = title;
            this.href = href;
            this.text = text;
        }
    }
}
