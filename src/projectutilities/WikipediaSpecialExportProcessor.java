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
 * WikipediaSpecialExportProcessor - a collection of some file processing tools.
 * 
 * Developed to work with wikipedias special export files that can be retrieved
 * from the https://en.wikipedia.org/wiki/Special:Export web page. This suite
 * makes heavy use of the Document Object Model (DOM) for parsing the export and
 * also for writing the processed data to disk. A simple wrapper class for the
 * pages is also included to provide programmatic access to data at runtime.
 * 
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class WikipediaSpecialExportProcessor {
    
    /**
     * convertSpecialExport - simple method for converting export file to XML.
     * 
     * Output Directory: _files/WikipediaSpecialExportProcessor/XMLOutputFiles/
     * 
     * @param inFileName - [path and] filename of special export file from web.
     * @param outFileName - the desired output file name
     * @return - the path to the completed output file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException 
     */
    public String convertSpecialExport(String inFileName, String outFileName)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerException {
        ArrayList<WikipediaPage> wikis = processSpecialExport(inFileName);
        String dir = "_files/WikipediaSpecialExportProcessor/XMLOutputFiles/";
        return saveAsXML(wikis, dir + outFileName);
    }
    
    /**
     * processSpecialExport - takes a String as the only argument that contains
     * the [path and] filename to the Wikipedia Special Export data file, which
     * is then parsed by the org.w3c.dom.Document on tag name "page" iteratively
     * and then creates a new WikipediaPage object for each page element in the
     * Document Tree adding them to a list which is returned when complete.
     * 
     * @param filename - the [path and] filename to the Special Export File
     * @return - an ArrayList of WikipediaPage objects
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    private ArrayList<WikipediaPage> processSpecialExport(String filename)
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
    
    /**
     * Simple method to save an ArrayList<WikipediaPage> object to XML format.
     * 
     * @param wikiList - the ArrayList<WikipediaPage> to save
     * @param filename - the desired output filename
     * @return - the path to the saved output file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException 
     */
    private String saveAsXML(ArrayList<WikipediaPage> wikiList, String filename)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerException {
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transx = factory.newTransformer();
        Document document = makeXMLDocument(wikiList);
        DOMSource source = new DOMSource(document);
        File xmlFile = new File(filename);
        StreamResult result = new StreamResult(xmlFile);
        transx.transform(source, result);
        
        return "File Location: " + xmlFile.getPath();
    }
    
    /**
     * Helper method that converts the processed Special Export file, which is
     * represented by a ArrayList<WikipediaPage>, into an org.w3c.dom.Document.
     * 
     * @param wikiList - the processed data structure to be converted
     * @return - a Document object populated by the wikiList argument
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
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
            for (int i = 0, s = wiki.getCategories().size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.getCategories().get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element categories = document.createElement("categories");
            categories.appendChild(document.createTextNode(sb.toString()));
            //
            for (int i = 0, s = wiki.getCitations().size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.getCitations().get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element citations = document.createElement("citations");
            citations.appendChild(document.createTextNode(sb.toString()));
            //
            for (int i = 0, s = wiki.getAnchors().size(); i < s; i++) {
                if (i == 0) sb = new StringBuilder();
                sb.append(wiki.getAnchors().get(i));
                if (i < s - 1) sb.append(" ");
            }
            Element anchors = document.createElement("anchors");
            anchors.appendChild(document.createTextNode(sb.toString()));
        }
        return document;
    }
    
    /**
     * WikipediaPage - wrapper class for wikipedias special export page data.
     */
    private class WikipediaPage {

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
        public String getTitle() { return TITLE_OF_PAGE; }
        public String getText() { return PAGE_TOP_TEXT; }

        // methods for accessing the arraylists of wiki page parsed data
        public ArrayList<String> getCategories() { return categories; }
        public ArrayList<String> getCitations() { return citations; }
        public ArrayList<String> getAnchors() { return anchors; }

        // methods for adding new data to the wiki page array lists
        public void pushCategory(String category) { categories.add(category); }
        public void pushCitation(String citation) { citations.add(citation); }
        public void pushAnchor(String anchor) { anchors.add(anchor); }

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
      
} /* EOC - WikipediaSpecialExportProcessor */
