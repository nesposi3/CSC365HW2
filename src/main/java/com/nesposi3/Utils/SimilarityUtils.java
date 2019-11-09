package com.nesposi3.Utils;


import com.nesposi3.BTree;
import com.nesposi3.Node;

import java.util.HashMap;
import java.util.Map;

import static com.nesposi3.Utils.BTreeUtils.K;
import static com.nesposi3.Utils.BTreeUtils.NULL;

public class SimilarityUtils {
    //The hashtable for the entered url is an n-dimensional vector that we apply tf-idf to, and cosine simiarity
    // We must go through each word on each document and apply these functions

    /**
     * TF Algorithm gives us word frequency in a document
     * @param key The word to check frequency, String
     * @param doc The document with which to check, Represented by a HashTable
     * @return The frequency of key
     */
    private static double TF(long key, BTree doc){

        double top = doc.search(key);
        double bottom = doc.totalWordCount();
        return top / (bottom);

    }

    /**
     * IDF Algorithm helps determine how rare a word is in a corpus, with more rarity yielding a higher value
     * @param key The word to check, a String
     * @param docList The corpus, represented as an array of HashTables
     * @return
     */
    private static double IDF(long key, BTree[] docList){
        int numWithKey = 0;
        for (int i = 0; i <docList.length ; i++) {
            if(docList[i].search(key)!=0){
                numWithKey++;
            }
        }
        if(numWithKey>0){
            double quotient = ((double) docList.length )/ (numWithKey);
            return Math.log(quotient);
        }else{
            return 0;
        }

    }

    /**
     * This method produces the most similar document of a corpus to the entered document
     * Uses TF and IDF
     * @param enteredDoc The Document that is being compared to, a  BTree
     * @return A string with the title of the most similar webpage
     */
    public static String findMostSimilarTfIdf(BTree enteredDoc,HashMap<BTree,String> map){
        BTree[] cachedDocs = map.keySet().toArray(new BTree[map.keySet().size()]);
        //This hashmap stores tf-idf vectors for each word in the entered document
        HashMap<Long, Double[]> cachedDocumentVectors = new HashMap<>();
        //This hashmap represents the tfidf vector of the query
        HashMap<Long,Double> queryVector = new HashMap<>();

        HashMap<Long,Integer> newdocMap = enteredDoc.getKeyFreqMap();
        for (Map.Entry<Long,Integer> entry: newdocMap.entrySet()){
            Double queryTF = TF(entry.getKey(),enteredDoc);
            Double queryIDF = IDF(entry.getKey(),cachedDocs);
            Double queryTFIDF = queryTF * queryIDF;
            queryVector.put(entry.getKey(),queryTFIDF);
            Double[] wordVector = new Double[enteredDoc.totalNumKeys()];
            for (int i = 0; i <cachedDocs.length ; i++) {
                double tf = TF(entry.getKey(),cachedDocs[i]);
                double idf = IDF(entry.getKey(),cachedDocs);
                wordVector[i] = tf * idf;
                cachedDocumentVectors.put(entry.getKey(),wordVector);
            }
        }
        //Cosine similarity for each document
        double maxSimilarity =0;
        String closestSite = "";
        for (int i = 0; i < cachedDocs.length; i++) {
            BTree doc = cachedDocs[i];
            double top = 0;
            double queryBottom = 0;
            double cachedBottom = 0;
            for(Map.Entry<Long,Double> entry:queryVector.entrySet()){
                double tfidfQuery = entry.getValue();
                double cachedTFIDF = cachedDocumentVectors.get(entry.getKey())[i];
                queryBottom += tfidfQuery * (tfidfQuery);
                cachedBottom += cachedTFIDF * cachedTFIDF;
                double dotProductPart = tfidfQuery * cachedTFIDF;
                top += dotProductPart;
            }
            double bottom = Math.sqrt(queryBottom) + Math.sqrt(cachedBottom);
            double similarity = top/bottom;
            if(similarity>maxSimilarity){
                maxSimilarity = similarity;
                closestSite = map.get(doc);
            }
        }
        return closestSite;

    }
    public static String findClosest(BTree enteredDoc,HashMap<BTree,String> map){
        BTree[] cachedDocs = map.keySet().toArray(new BTree[map.keySet().size()]);
        double closestNum = Double.MIN_VALUE;
        int closestIndex = -1;
        for (int i = 0; i <cachedDocs.length ; i++) {
            BTree test = cachedDocs[i];
            double result = enteredDoc.cosineSimilarity(test);
            if(result>closestNum){
                closestNum = result;
                closestIndex = i;
            }
        }
        BTree winner = cachedDocs[closestIndex];
        return map.get(winner);
    }

}
