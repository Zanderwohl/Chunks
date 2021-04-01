package com.zanderwohl.util.search;

import com.sun.source.tree.Tree;

import java.lang.reflect.Array;
import java.util.*;

public class PhraseIndex {

    protected HashMap index;
    private int minimumSearchTerm;

    public PhraseIndex(){
        index = new HashMap<String, ArrayList<String>>();
        this.minimumSearchTerm = 2;
    }

    public PhraseIndex(int minimumSearchTerm){
        index = new HashMap<String, ArrayList<String>>();
        this.minimumSearchTerm = minimumSearchTerm >= 2 ? minimumSearchTerm : 2;
    }

    public void put(String key){
        createIndex(key);
    }

    private void createIndex(String value){
        String[] words = value.split("\\s");
        for(String word: words){
            for(int i = 1; i < word.length() + 1; i++){
                String subkey = word.substring(0, i).toUpperCase(Locale.ROOT);
                if(!index.containsKey(subkey)){
                    index.put(subkey, new ArrayList<String>());
                }
                ((ArrayList<String>) index.get(subkey)).add(value);
            }
        }
    }

    public Set<String> keys(){
        return index.keySet();
    }

    public ArrayList<String> values(String key){
        return (ArrayList<String>) index.get(key);
    }

    /* Oh God in heaven, hear my plea,
    * and do not place the blame one me.
    * I wanted something short and sweet,
    * a method that was clean and neat.
    * I didn't mean to make this mess,
    * or cause my future-me distress.
    * I blame the MUMPS, its trees and such,
    * that made me sin so very much.
    * Like Icarus, I went too high,
    * I stumbled and it went awry.
    * One last thing that I request,
    * before I lay me down to rest:
    * whoever fix this mess of tree
    * would not track down and murder me.
    * */
    public ArrayList<String> search(String searchTerms){
        String[] searchTermsSplit = searchTerms.split("\\s");
        String[] foundTerms = new String[searchTermsSplit.length];
        int foundTermsIndex = 0;
        for(String term: searchTermsSplit){
            if(index.containsKey(term.toUpperCase(Locale.ROOT))){
                foundTerms[foundTermsIndex] = term.toUpperCase(Locale.ROOT);
                foundTermsIndex++;
            }
        }
        TreeMap<String, Integer> resultsByRelevance = new TreeMap<String, Integer>();
        for(int i = 0; i < foundTermsIndex; i++){
            String term = foundTerms[i];
            ArrayList<String> resultsForTerm = (ArrayList<String>) index.get(term);
            for(String result: resultsForTerm){
                if(resultsByRelevance.containsKey(result)){
                    resultsByRelevance.put(result, resultsByRelevance.get(result) + 1);
                } else {
                    resultsByRelevance.put(result, 1);
                }
            }
        }
        TreeMap<Integer, ArrayList<String>> relevanceRankedResults = new TreeMap<>(Collections.reverseOrder());
        for(Map.Entry<String, Integer> entry: resultsByRelevance.entrySet()){
            if(!relevanceRankedResults.containsKey(entry.getValue())){
                relevanceRankedResults.put(entry.getValue(), new ArrayList<String>());
            }
            relevanceRankedResults.get(entry.getValue()).add(entry.getKey());
        }
        ArrayList<String> results = new ArrayList<>();
        Set set = relevanceRankedResults.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            ArrayList<String> list = (ArrayList<String>) me.getValue();
            results.addAll(list);
        }
        return results;
    }

    //private

    public static void main(String[] args){
        PhraseIndex pi = new PhraseIndex();
        pi.put("Brave New World");
        pi.put("Brave Old World");
        pi.put("Brave New World 2");
        pi.put("Brave Little Toaster");
        pi.put("Fahrenheit 451");
        pi.put("Breaking Dawn");
        pi.put("Foundation");
        pi.put("Foundation and Empire");
        pi.put("Second Foundation");
        pi.put("On the Origin of the Species by Means of Natural Selection, or the Preservation of Favoured Races in the Struggle for Life");
        /*for (String key: pi.keys()){
            System.out.println(key + "|" + pi.values(key));
        }
        System.out.println(pi.keys());
        System.out.println(pi.search("br"));
        System.out.println(pi.search("bre"));
        System.out.println(pi.search("br n"));
        System.out.println(pi.search("br world"));*/
        Scanner scan = new Scanner(System.in);
        String term = scan.nextLine();
        System.out.println(pi.search(term));
    }
}
