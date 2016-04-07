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

    private ArrayList<String> categories;   // categories listed on the page
    private ArrayList<String> citations;    // citations used on the page
    private ArrayList<String> anchors;      // hyperlinks used on the page

    /**
     * 
     * @param title
     * @param text 
     */
    public WikipediaPage(String title, String text) {
        categories = new ArrayList<>();
        citations = new ArrayList<>();
        anchors = new ArrayList<>();
        TITLE_OF_PAGE = title;
        PAGE_TOP_TEXT = text;
    }

    public ArrayList<String> getCategories() { return this.categories; }
    public ArrayList<String> getCitations() { return this.citations; }
    public ArrayList<String> getAnchors() { return this.anchors; }

    /**
     * 
     * @param page 
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
     * 
     * @param page
     * @param tag
     * @return 
     */
    private String getElementByTag(Element page, String tag) {
        return page.getElementsByTagName(tag).item(0).getTextContent();
    }

    /**
     * 
     * @param symbols
     * @param type
     * @return 
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
     * 
     * @param term
     * @param type
     * @return 
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
     * 
     * @param symbols
     * @return 
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
