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

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

/**
 * WikipediaSpecialExportProcessor - collection of wikipedia data utilities.
 * 
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class WikipediaSpecialExportProcessor {
    
    public ArrayList<WikipediaPage> processSpecialExport(String filename)
            throws ParserConfigurationException, SAXException, IOException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(filename);
        NodeList nodes = document.getElementsByTagName("page");
        
        ArrayList<WikipediaPage> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(new WikipediaPage((Element)nodes.item(i)));
        }
        return list;
    }
    
    public String saveAsXML(ArrayList<WikipediaPage> wikiList, String filename)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerConfigurationException, TransformerException {
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transx = factory.newTransformer();
        Document document = makeXMLDocument(wikiList);
        DOMSource source = new DOMSource(document);
        File xmlFile = new File(filename);
        StreamResult result = new StreamResult(xmlFile);
        transx.transform(source, result);
        
        return "File Location: " + xmlFile.getPath();
    }
    
    private Document makeXMLDocument(ArrayList<WikipediaPage> wikiList)
            throws ParserConfigurationException, SAXException, IOException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("ProcessedSpecialExportData");
        document.appendChild(root);
        StringBuilder sb = null;
        
        for (WikipediaPage wiki : wikiList) {
            //
            Element page = document.createElement("page");
            root.appendChild(page);
            //
            Element title = document.createElement("title");
            title.appendChild(document.createTextNode(wiki.TITLE_OF_PAGE));
            page.appendChild(title);
            //
            Element text = document.createElement("text");
            text.appendChild(document.createTextNode(wiki.PAGE_TOP_TEXT));
            //
            for (int i = 0, s = wiki.categories.size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.categories.get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element categories = document.createElement("categories");
            categories.appendChild(document.createTextNode(sb.toString()));
            //
            for (int i = 0, s = wiki.citations.size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.citations.get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element citations = document.createElement("citations");
            citations.appendChild(document.createTextNode(sb.toString()));
            //
            for (int i = 0, s = wiki.anchors.size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.anchors.get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element anchors = document.createElement("anchors");
            anchors.appendChild(document.createTextNode(sb.toString()));
        }
        return document;
    }
    
    
    
    /** WikipediaPage.
     * 
     */
    public class WikipediaPage {
        
        protected final String TITLE_OF_PAGE;   // the title of the wiki page
        protected final String PAGE_TOP_TEXT;   // the lead section on the page
        
        private ArrayList<String> categories;   // categories listed on the page
        private ArrayList<String> citations;    // citations used on the page
        private ArrayList<String> anchors;      // hyperlinks used on the page
        
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
        
        public WikipediaPage(Element page) {
            TITLE_OF_PAGE = getElementByTag(page, "title").trim();
            char[] pageCharArr = getElementByTag(page, "text").toCharArray();
            categories = initListByType(pageCharArr, "categories");
            citations = initListByType(pageCharArr, "citations");
            anchors = initListByType(pageCharArr, "anchors");
            PAGE_TOP_TEXT = normalizeWikiPageTextForPOSTagging(pageCharArr);
        }
        
        private String getElementByTag(Element page, String tag) {
            return page.getElementsByTagName(tag).item(0).getTextContent();
        }
        
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
} /* EOC - WikipediaSpecialExportProcessor */
