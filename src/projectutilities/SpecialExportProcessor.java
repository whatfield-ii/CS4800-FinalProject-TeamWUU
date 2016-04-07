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
 * SpecialExportProcessor - a collection of some file processing tools.
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
public class SpecialExportProcessor {
    
    /**
     * convertSpecialExport - simple method for converting export file to XML.
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
        return saveAsXML(wikis, outFileName);
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
    public String saveAsXML(ArrayList<WikipediaPage> wikiList, String filename)
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
      
} /* EOC - SpecialExportProcessor */
