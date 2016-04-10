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
package cs4800finalproject; // this project package
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import projectutilities.*;  // and a toolbox for it


import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Project Interface - provide user with a netbeans and command line interface.
 * 
 * Project Overview: given the first section of a wikipedia article, we will be
 * attempting to first determine if that article references a person or not, and
 * then attempt to determine to sex (M/F) of the person the article references.
 * 
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class ProjectInterface {
    
    private static WikipediaSearcherAndPageScraper WSPS;
    private static WikipediaSpecialExportProcessor WSEP;
    private static StandfordSpeechTaggerAndCounter SSTC;
    
    /**
     * @param args the command line arguments
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     */
    public static void main(String[] args)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerException, ResponseException, JauntException {
        // TODO code application logic here


        WSPS = new WikipediaSearcherAndPageScraper();
        
        WSPS.searchWikipedia("William Wallace");
        
        System.out.println("Hello World!");
        WSEP = new WikipediaSpecialExportProcessor();
        SSTC = new StandfordSpeechTaggerAndCounter();
        
        
        System.out.println("New Success");
        String dir = "_files/WikipediaSpecialExportProcessor/SpecialExportFiles/";
        String inputFile = "SpecialExportTestFile.xml";
        String outputFile = "ConversionTest.xml";
        String loc = WSEP.convertSpecialExport(dir + inputFile, outputFile);
        System.out.println(loc);
        
        
        
    }
    
}
