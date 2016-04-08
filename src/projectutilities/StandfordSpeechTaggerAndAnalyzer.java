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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.Map.Entry;
import java.util.HashMap;

/**
 * SimpleStanfordPOSTagging.
 *
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class StandfordSpeechTaggerAndAnalyzer {
    
    private static final HashMap<String, Integer> MAP = new HashMap<>();
    private final String MODELFILE = "tagger/english-left3words-distsim.tagger";
    private final MaxentTagger TAGGER = new MaxentTagger(MODELFILE);
    
    /**
     * tagNormalizedString - takes a single String argument, that should have
     * been normalized by the WikipediaSpecialExportProcessor, then completes
     * the Part-of-Speech Tagging using Stanford Maximum Entropy Tagger, after
     * which the terms are counted and added to a hash map to keep count.
     * 
     * @param toTag
     * @return 
     */
    public String tagNormalizedString(String toTag) {
        String taggedString = TAGGER.tagString(toTag);
        
        String[] taggedStringArray = taggedString.split(" ");
        for (String toCheck : taggedStringArray) {
            Integer wordCount = MAP.get(toCheck);
            wordCount = (wordCount != null) ? wordCount + 1 : 1;
            MAP.put(toCheck, wordCount);
        }
        
        return taggedString;
    }
    
    /**
     * getMostCommonWord - returns the string with the highest count from all
     * previously processed strings, until the word count has been reset.
     * @return - the most common word from processed strings
     */
    public String getMostCommonWord() {
        String mostCommonWord = "";
        Integer highestCount = -1;
        Integer currentCount;
        for (Entry<String, Integer> entry : MAP.entrySet()) {
            currentCount = entry.getValue();
            if (highestCount.equals(currentCount)) {
                mostCommonWord = mostCommonWord + entry.getKey();
            } else if (currentCount > highestCount) {
                mostCommonWord = entry.getKey();
                highestCount = currentCount;
            }
        }
        return mostCommonWord;
    }
    
    /**
     * getWordCountOf - returns the word count of the string or -1 if no string.
     * 
     * @param word - word to return the count of
     * @return - the count of word or -1
     */
    public int getWordCountOf(String word) {
        Integer wordCount = MAP.get(word);
        return (wordCount > 0) ? wordCount : -1;
    }
    
    
    
}
