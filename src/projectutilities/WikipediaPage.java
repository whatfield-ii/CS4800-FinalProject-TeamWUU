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

import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 * WikipediaPage - wrapper class for wikipedias special export page data.
 */
public class WikipediaPage {

    protected final String TITLE_OF_PAGE;   // the title of the wiki page
    protected final String PAGE_TOP_TEXT;   // the lead section on the page

    private final ArrayList<String> categories; // categories listed on the page
    private final ArrayList<String> citations;  // citations used on the page
    private final ArrayList<String> anchors;    // hyperlinks used on the page

    /**
     * WikipediaPage - String Constructor: takes two string arguments that will
     * be assigned as the page title and text of the WikipediaPage object while
     * the three ArrayLists of type String are initialized and left empty.
     * 
     * @param title - string to be assigned as page title
     * @param text - string to be assigned as page text
     */
    public WikipediaPage(String title, String text) {
        categories = new ArrayList<>();
        citations = new ArrayList<>();
        anchors = new ArrayList<>();
        TITLE_OF_PAGE = title;
        PAGE_TOP_TEXT = text;
    }
    
    // methods for retrieving the title and text of a wiki page
    public String getTitle() { return this.TITLE_OF_PAGE; }
    public String getText() { return this.PAGE_TOP_TEXT; }
    
    // methods for accessing the arraylists of wiki page parsed data
    public ArrayList<String> getCategories() { return this.categories; }
    public ArrayList<String> getCitations() { return this.citations; }
    public ArrayList<String> getAnchors() { return this.anchors; }
    
    // methods for adding new data to the wiki page array lists
    public void pushCategory(String category) { this.categories.add(category); }
    public void pushCitation(String citation) { this.citations.add(citation); }
    public void pushAnchor(String anchor) { this.anchors.add(anchor); }

    /**
     * WikipediaPage - Element Constructor: takes an org.w3c.dom.Element as the
     * only argument, one which has been parsed from a Wikipedia Special Export
     * data dump, retrieved from https://en.wikipedia.org/wiki/Special:Export ,
     * where the parsing was done by the org.w3c.dom.Document member function 
     * getElementsByTagName(String tag) and the tag value is "page".
     * 
     * @param page - the DOM Element parsed by the DOM Document on a "page" tag
     */
    public WikipediaPage(Element page) {
        TITLE_OF_PAGE = getElementByTag(page, "title").trim();
        char[] pageCharArr = getElementByTag(page, "text").toCharArray();
        categories = initListByType(pageCharArr, "categories");
        citations = initListByType(pageCharArr, "citations");
        anchors = initListByType(pageCharArr, "anchors");
        PAGE_TOP_TEXT = normalizeWikiPageTextForPOSTagging(pageCharArr);
    }

    /**
     * Returns the text of the first child element of page with matching tag.
     * 
     * @param page - the parent element of the sought after tag text content
     * @param tag - the tag name of the child text content to retrieve
     * @return - text of pages first child with matching tag name
     */
    private String getElementByTag(Element page, String tag) {
        return page.getElementsByTagName(tag).item(0).getTextContent();
    }

    /**
     * Crawls the char[] symbols and adds Strings to an ArrayList<String> where,
     * the String being added is determined by the String type variable.
     * 
     * @param symbols - the character array to crawl for Strings
     * @param type - "categories" or "citations" or "anchors"
     * @return - list of all terms matching desired type
     */
    private ArrayList<String> initListByType(char[] symbols, String type) {

        ArrayList<String> list = new ArrayList<>();
        StringBuilder sb = null;
        boolean reading = false;
        int braceCount = 0;
        char current, next;

        for (int i = 0; i < symbols.length - 1; i++) {
            next = symbols[i + 1];  // hence length - 1
            current = symbols[i];
            if (current == '{' && !reading) braceCount++;
            if (current == '}' && !reading) braceCount--;
            if (braceCount > 0) continue;
            if (current == '[' && next == '[') {
                sb = new StringBuilder();
                reading = true;
                i++; // step over second brace
            } else if (current == ']' && next == ']') {
                String parsedTerm = parseTermByType(sb.toString(), type);
                if (!parsedTerm.isEmpty()) list.add(parsedTerm);
                reading = false;
                i++; // step over second brace
            } else if (reading) {
                sb.append(current);
            }
        }
        return list;
    }

    /**
     * Helper method for initListByType(char[] symbols, String type) that parses
     * the data from the String term based on the String type argument.
     * 
     * @param term - the String possibly containing a desired term
     * @param type - "categories" or "citations" or "anchors"
     * @return - successfully parsed String or empty String
     */
    private String parseTermByType(String term, String type) {

        String prefix = (type.equals("citations")) ? "cite" : "Category:";
        switch (type) {
            case "categories":
                if (term.startsWith(prefix)) {
                    return term.substring(prefix.length());
                }
                break;
            case "citations":
                if (term.startsWith(prefix)) {
                    String front = "title", back = "|";
                    int begin = term.indexOf(front) + front.length();
                    int end = term.indexOf(back, begin);
                    if (begin > 0 && end > 0) {
                        return term.substring(begin, end);
                    }
                }
                break;
            case "anchors":
                if (term.startsWith(prefix)) return "";
                int bar = term.indexOf('|');
                if (bar > 0) {
                    return term.substring(0, bar);
                } else {
                    return term.substring(0);
            }
        }
        return "";
    }

    /**
     * This method converts the char[] argument symbols into a single String,
     * which is made up of only Letters, Digits, or Spaces in preparation for
     * the Part-of-Speech tagging done by the Stanford NLP package.
     * 
     * @param symbols - the char[] to build a POS Tagging ready String from
     * @return - a String that can be efficiently POS Tagged
     */
    private String normalizeWikiPageTextForPOSTagging(char[] symbols) {

        StringBuilder sb = new StringBuilder();
        int braceCount = 0;
        char current, next;

        for (int i = 0; i < symbols.length - 1; i++) {
            next = symbols[i + 1];  // hence length - 1
            current = symbols[i];
            if (current == '=' && next == '=') break;
            if (current == '{') braceCount++;
            if (current == '}') braceCount--;
            if (braceCount > 0) continue;
            boolean letter = Character.isAlphabetic(current);
            boolean white = Character.isWhitespace(current);
            boolean digit = Character.isDigit(current);
            if (letter || digit) {
                sb.append(current);
            } else if (white) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

} /* EOC - WikipediaPage */  
