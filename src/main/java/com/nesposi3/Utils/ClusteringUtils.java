package com.nesposi3.Utils;

import com.nesposi3.BTree;
import com.nesposi3.Cluster;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static com.nesposi3.Utils.BTreeUtils.BTREE_FOLDER_NAME;
import static org.apache.commons.codec.digest.DigestUtils.md5;

public class ClusteringUtils {
    private static final int NUM_CLUSTERS= 9;
    private static final int SWAP_ITERATION = 50;
    /**
     * Convert a string into a long (64 bit) hash code
     * Takes the lower 64 bits of a md5 hash
     * @param input
     * @return
     */
    public static long stringHash64(String input){
        byte[] totalMd5 = md5(input);
        byte[] longMd5 = new byte[8];
        for (int i = 0; i <8 ; i++) {
            longMd5[i] = totalMd5[i];
        }
        ByteBuffer buffer = ByteBuffer.wrap(longMd5);
        return buffer.getLong();
    }

    /**
     * This function reforms a k-medioids analysis on the cached btrees
     * @throws IOException
     */
    public static void kMedioids()throws IOException {
        HashMap<BTree, String> bTreeMap = new HashMap<>();
        // Get all btrees from the folder, add them to the map with String name
        File dir = new File(BTREE_FOLDER_NAME);
        File[] files = dir.listFiles();
        Cluster[] clusters = new Cluster[NUM_CLUSTERS];
        for (int i = 0; i < files.length ; i++) {
            BTree tree = new BTree(BTREE_FOLDER_NAME  + files[i].getName());
            bTreeMap.put(tree,files[i].getName());
        }

        // create blank cluster with given id
        for (int i = 0; i <NUM_CLUSTERS ; i++) {
            Cluster cluster = new Cluster(i);
            clusters[i] = cluster;
        }

        // Assign initial medioids to the clusters
        for (int i = 0; i <NUM_CLUSTERS ; i++) {
            clusters[i].setMedioid(files[i*10].getName());
            System.out.println("Initital Mediod: " + files[i*10].getName() );
        }
        // Assign members to these clusters
        for(BTree x:bTreeMap.keySet()){
            int closestIndex = 0;
            double closestValue = Double.MAX_VALUE;
            for (int i = 0; i < clusters.length; i++) {
                Cluster cluster = clusters[i];
                BTree medioid = new BTree(BTREE_FOLDER_NAME+cluster.medioid);
                double distance = x.computeEuclideanDistance(medioid);
                if(distance<closestValue){
                    closestValue = distance;
                    closestIndex =i;
                }
            }
            Cluster closestCluster = clusters[closestIndex];
            closestCluster.addMember(bTreeMap.get(x));
        }
        // Try to swap and make total cost for n iterations
        for (int i = 0; i < clusters.length; i++) {
            Cluster currentCluster = clusters[i];
            String[] members = currentCluster.getMembers();
            double totalcost=0;
            BTree medtree =  new BTree(BTREE_FOLDER_NAME + currentCluster.getMedioid());
            for(String member:members){
                BTree tree = new BTree(BTREE_FOLDER_NAME + member);
                totalcost += medtree.computeEuclideanDistance(tree);
            }
            int j = 0;
            while(j<SWAP_ITERATION && j<members.length){
                double trialCost = 0;
                BTree newMed = new BTree(BTREE_FOLDER_NAME + members[j]);
                for (String member: members) {
                    BTree tree = new BTree(BTREE_FOLDER_NAME + member);
                    trialCost += newMed.computeEuclideanDistance(tree);
                }
                if(trialCost<totalcost){
                    String oldMedioid = currentCluster.getMedioid();
                    currentCluster.setMedioid(members[j]);
                    currentCluster.removeMember(members[j]);
                    currentCluster.addMember(oldMedioid);
                }
                j++;
            }
            currentCluster.writeToDisk();
            System.out.println(currentCluster.toString());
        }
    }

}
