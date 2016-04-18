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
import projectutilities.*;  // and a toolbox for it

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

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
    /**************************************************************************/
    private static final WikipediaSearcherAndPageScraper WSPS = new WikipediaSearcherAndPageScraper();
    private static final WikipediaSpecialExportProcessor WSEP = new WikipediaSpecialExportProcessor();
    private static final StandfordSpeechTaggerAndCounter SSTC = new StandfordSpeechTaggerAndCounter();
    /**************************************************************************/
    private static final Scanner KBIN = new Scanner(System.in); // user keyboard input
    /**************************************************************************/
    private static final String FILES = "_files/"; // root of the packages data files
    //
    private static final String WSEP_DIRECTORY = FILES + "WikipediaSpecialExportProcessor/";
    private static final String EXPORT_FILES = WSEP_DIRECTORY + "SpecialExportFiles/";
    private static final String XMLOUT_FILES = WSEP_DIRECTORY + "XMLOutputFiles/";
    //
    private static final String EXPORT_TEST = EXPORT_FILES + "_SpecialExportTestFile.xml";
    private static final String XMLOUT_TEST = XMLOUT_FILES + "_XMLOutputTest.xml";
    //
    private static final String SSTC_DIRECTORY = FILES + "StandfordSpeechTaggerAndCounter/";
    private static final String TAGGER_TESTING = SSTC_DIRECTORY + "_TagTesting.txt";
    private static final String TESTING_REPORT = SSTC_DIRECTORY + "_TestCount.txt";
    //
    private static final String TAGGED_OBJECTS = SSTC_DIRECTORY + "objects/";
    private static final String TAGGED_WOMEN = SSTC_DIRECTORY + "women/";
    private static final String TAGGED_MEN = SSTC_DIRECTORY + "men/";
    //
    private static final String OBJECTS_REPORT = TAGGED_OBJECTS + "_objects.txt";
    private static final String WOMENS_REPORT = TAGGED_WOMEN + "_women.txt";
    private static final String MENS_REPORT = TAGGED_MEN + "_men.txt";
    /**************************************************************************/
    /**_files/ <-- THE DATA FILE DIRECTORY STRUCTURE FOR THIS PROJECT -->
     * |--> WikipediaSpecialExportProcessor/    WSEP_DIRECTORY
     * |  |--> SpecialExportFiles/              EXPORT_FILES
     * |  |--> XMLOutputFiles/                  XMLOUT_FILES
     * |--> StandfordSpeechTaggerAndCounter/    SSTC_DIRECTORY
     * |  |--> _TagTesting.txt                  TAGGER_TESTING
     * |  |--> _TestCount.txt                   TESTING_REPORT
     * |  |--> objects/                         TAGGED_OBJECTS
     * |  |  |--> _objects.txt                  OBJECTS_REPORT
     * |  |--> women/                           TAGGED_WOMEN
     * |  |  |--> _women.txt                    WOMENS_REPORT
     * |  |--> men/                             TAGGED_MEN
     * |  |  |--> _men.txt                      MENS_REPORT
     * END
     */
    /**************************************************************************/
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //initWikipediaSpecialExportProcessor();
        //parseAndTagProcessedExportTexts();
        // start the training of the machine
        WSPS.searchWikipedia("this is a test");
    }
    
    private static void parseAndTagProcessedExportTexts() {
        String[] processedExports = new File(XMLOUT_FILES).list();
        if (processedExports == null || processedExports.length == 0) {
            // the directory is empty of does not exist, this is a fatal error
            System.err.println("ERR @ parseAndTagProcessedExportTexts");
            System.err.println("ERR: Directory Empty @ " + XMLOUT_FILES);
            System.err.println("FATAL ERROR: Exiting Program !");
            System.exit(3);
        } else if (processedExports.length == 1) {
            if (!XMLOUT_TEST.endsWith(processedExports[0])) {
                // the directory does not contain the export testing file
                System.err.println("ERR @ parseAndTagProcessedExportTexts");
                System.err.println("ERR: File Missing @ " + XMLOUT_TEST);
                System.err.println("FATAL ERROR: Exiting Program !");
                System.exit(4);
            } else {
                System.err.println("Parsing and Tagging @ " + XMLOUT_TEST);
                ArrayList<String> texts = WSEP.getTextsFromProcessedExport(XMLOUT_TEST);
                StringBuilder builder = new StringBuilder();
                for (String text : texts) {
                    builder.append(text);
                    builder.append(' ');
                }
                SSTC.tagTextAndWriteFile(builder.toString(), TAGGER_TESTING);
                SSTC.writeReport(TESTING_REPORT);
                SSTC.resetWordCount(); // clear the report data
                System.err.println("Parsing/Tagging Complete -> " + TAGGER_TESTING);
            }
        } else {
            for (String processed : processedExports) {
                if (!XMLOUT_TEST.endsWith(processed)) {
                    processed = XMLOUT_FILES + processed; // prepend file path
                    System.err.println("Parsing and Tagging @ " + processed);
                    ArrayList<String> texts = WSEP.getTextsFromProcessedExport(processed);
                    String processedType = determineFilesType(processed);
                    switch (processedType) {
                        case "objects": { 
                            tagAndSaveTexts(texts, TAGGED_OBJECTS);
                            SSTC.writeReport(OBJECTS_REPORT);
                            SSTC.resetWordCount();
                            break;
                        }
                        case "women" : {
                            tagAndSaveTexts(texts, TAGGED_WOMEN);
                            SSTC.writeReport(WOMENS_REPORT);
                            SSTC.resetWordCount();
                            break;
                        }
                        case "men": {
                            tagAndSaveTexts(texts, TAGGED_MEN);
                            SSTC.writeReport(MENS_REPORT);
                            SSTC.resetWordCount();
                            break;
                        }
                        default: {
                            System.err.print("ERR: XML File Not Parsed: ");
                            System.err.println(EXPORT_FILES + processed);
                        }
                    }
                }
            }
        }
    }
    
    private static void tagAndSaveTexts(ArrayList<String> texts, String dir) {
        for (int i = 0; i < texts.size(); i++) {
            String fileName = Integer.toString(i); // all filenames same length
            while (fileName.length() < 7) fileName = '0' + fileName;
            fileName = dir + fileName;
            SSTC.tagTextAndWriteFile(texts.get(i), fileName);
        }
    }
    
    private static void initWikipediaSpecialExportProcessor() {
        String[] exportFiles = new File(EXPORT_FILES).list();
        if (exportFiles == null || exportFiles.length == 0) {
            // the directory is empty of does not exist, this is a fatal error
            System.err.println("ERR @ initWikipediaSpecialExportProcessor");
            System.err.println("ERR: Directory Empty @ " + EXPORT_FILES);
            System.err.println("FATAL ERROR: Exiting Program !");
            System.exit(1);
        } else if (exportFiles.length == 1) {
            if (!EXPORT_TEST.endsWith(exportFiles[0])) {
                // the directory does not contain the export testing file
                System.err.println("ERR @ initWikipediaSpecialExportProcessor");
                System.err.println("ERR: File Missing @ " + EXPORT_TEST);
                System.err.println("FATAL ERROR: Exiting Program !");
                System.exit(2);
            } else {
                System.err.println("Processing Export File @ " + EXPORT_TEST);
                WSEP.convertSpecialExport(EXPORT_TEST, XMLOUT_TEST);
                System.err.println("Processing Complete -> " + XMLOUT_TEST);
            }
        } else {
            for (String fileName : exportFiles) {
                if (!EXPORT_TEST.endsWith(fileName)) {
                    String exportType = determineFilesType(fileName);
                    if (exportType.equals("objects") 
                     || exportType.equals("women")
                     || exportType.equals("men")) {
                        String xmlFileName = XMLOUT_FILES + exportType + ".xml";
                        String exportInput = EXPORT_FILES + fileName;
                        System.err.println("Processing Export File @ " + exportInput);
                        WSEP.convertSpecialExport(exportInput, xmlFileName);
                        System.err.println("Processing Complete -> " + xmlFileName);
                    } else {
                        System.err.print("ERR: Export File Not Processed: ");
                        System.err.println(EXPORT_FILES + fileName);
                    }
                }
            }
        }
    }
    
    private static String determineFilesType(String fileName) {
        String objects = "objects";
        String women = "women";
        String men = "men";
        if (fileName.toLowerCase().contains(objects)) return objects;
        if (fileName.toLowerCase().contains(women)) return women;
        if (fileName.toLowerCase().contains(men)) return men;
        return null;
    }
    
}
