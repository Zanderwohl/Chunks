package com.zanderwohl.util.search.tests;

import com.zanderwohl.util.search.PhraseIndex;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class PhraseIndexTests extends TestCase {
    protected PhraseIndex index;
    private final String searchTerms;
    private final ArrayList<String> expectedResults;
    private final REASONS reason;
    private enum REASONS {
        EXACT_MATCH,
        ABBREVIATED_WORDS,
        SINGLE_WORD,
        EXTRA_WORDS,
        WRONG_PREPOSITIONS,
    }

    @Before
    public void setUp(){
        index = new PhraseIndex();
        index.put("Brave New World");
        index.put("Brave Old World");
        index.put("Brave New World 2");
        index.put("Brave Little Toaster");
        index.put("Fahrenheit 451");
        index.put("Breaking Dawn");
        index.put("Foundation");
        index.put("Foundation and Empire");
        index.put("Second Foundation");
        index.put("On the Origin of the Species by Means of Natural Selection, or the Preservation of Favoured Races in the Struggle for Life");
    }

    public PhraseIndexTests(String searchTerms, ArrayList<String> expectedResults, REASONS reason){
        this.searchTerms = searchTerms;
        this.expectedResults = expectedResults;
        this.reason = reason;
    }

    @Parameterized.Parameters
    public static Collection searchSets(){
        return Arrays.asList(new Object[][] {
                {"Brave New World", new ArrayList<>(Arrays.asList("Brave New World", "Brave New World 2", "Brave Old World", "Brave Little Toaster")), REASONS.EXACT_MATCH},
                {"Brave Old World", new ArrayList<>(Arrays.asList("Brave Old World", "Brave New World", "Brave New World 2", "Brave Little Toaster")), REASONS.EXACT_MATCH},
                {"Foundation", new ArrayList<>(Arrays.asList("Foundation", "Foundation and Empire", "Second Foundation")), REASONS.SINGLE_WORD},
                {"Foundation Four", new ArrayList<>(Arrays.asList("Foundation", "Foundation and Empire", "Second Foundation")), REASONS.EXTRA_WORDS},
                {"Origin Into the Species", new ArrayList<>(Arrays.asList("On the Origin of the Species by Means of Natural Selection, or the Preservation of Favoured Races in the Struggle for Life")), REASONS.WRONG_PREPOSITIONS},
        });
    }

    @Test
    public void searchTests(){
        assertEquals(this.reason.toString(), this.expectedResults, index.search(this.searchTerms));
    }
}
