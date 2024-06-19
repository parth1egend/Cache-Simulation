import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class javaproject {
    private int cacheSize; // Cache size in kilobytes
    private int associativity; // Associativity of the cache
    private int blockSize = 64; // Block size in bytes
    private int setSize; // Number of sets in the cache
    private int setIndexBits; // Number of set index bits (log2(cachesize))
    private int offsetBits = 6; // Number of offset bits (log2(blockSize))

    private int[][] cache; // 2D array representing the cache
    int[][] lruCounter; // Array to keep track of LRU information
    
    public void initialisetoMaxvalue(){
        for(int i=0; i<setSize ; i++)
        {
            for(int j=0; j<associativity ; j++)
            {
                lruCounter[i][j]=associativity+1;
            }
        }
    }
    

    private int totalMisses;
    private int totalHits;
    private int[] setMisses;
    private int[] setHits;

    public javaproject(int cacheSize, int associativity, String traceFilePath) {
        this.cacheSize = cacheSize;
        this.associativity = associativity;

        // Calculate the number of sets in the cache
        setSize = (cacheSize) / (associativity * blockSize);
        setIndexBits = (int) (Math.log(setSize) / Math.log(2));

        // Initialize cache and LRU counter
        cache = new int[setSize][associativity];
        lruCounter = new int[setSize][associativity];

        // Initialize counters
        totalMisses = 0;
        totalHits = 0;
        setMisses = new int[setSize];
        setHits = new int[setSize];

        initialisetoMaxvalue();
        // Process the trace file
        processTraceFile(traceFilePath);
    }

    int hextoInt(Character a)
    {
        switch(a)
        {
            case 'A': return 10;
            case 'B': return 11;
            case 'C': return 12;
            case 'D': return 13;
            case 'E': return 14;
            case 'F': return 15;
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            default: return -1;
        }
    }
    private void processTraceFile(String traceFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(traceFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                int address = 0;
                for(int i = 7 ; i>=0 ;i--)
                {
                    address = address + hextoInt(line.charAt(i))*((int)Math.pow(16,7 - i));
                }

                String binary = Integer.toBinaryString(address);
                while (binary.length() != 32)
                {
                    binary = "0" + binary;
                }

            
                String setindex = binary.substring(32 - 6 - setIndexBits, 32 - 6);
                int setIndex =0;
                try
                {
                    setIndex = Integer.parseInt(setindex,2);
                }
                catch(NumberFormatException e)
                {
                    setIndex = 0;
                }
                
                
                // int setIndex = (address >> offsetBits) & ((1 << setIndexBits) - 1);
                int tag = address >> (offsetBits + setIndexBits);

                boolean hit = false;

                // Search for the block in the set
                for (int way = 0; way < associativity; way++) {
                    if (cache[setIndex][way] == tag) {
                        hit = true;
                        setHits[setIndex]++;
                        totalHits++;
                        updateLRUCounter(setIndex, way);
                        break;
                    }
                }

                if (!hit) {
                    totalMisses++;
                    setMisses[setIndex]++;

                    // Find the LRU block in the set
                    int lruWay = findLRUWay(setIndex);

                    // Replace the LRU block with the new block
                    cache[setIndex][lruWay] = tag;
                    updateLRUCounter(setIndex, lruWay);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLRUCounter(int setIndex, int way) {
        
        for (int i = 0; i < associativity; i++) {
            if (i==way) {
                lruCounter[setIndex][i] = 1;
            }
            else if(i!=way){
                lruCounter[setIndex][i]++;
            }
        }
        
    }

    private int findLRUWay(int setIndex) {  
        int lruvalue =lruCounter[setIndex][0];
        int lruWay = 0;
        // Find the way with the highest LRU value
        for (int way = 0; way < associativity; way++) {
            if (lruCounter[setIndex][way] >= lruvalue) {
                lruvalue = lruCounter[setIndex][way];
                lruWay = way;
            }
        }

        return lruWay;
    }

    private void printResults() {
        System.out.println("Total Misses: " + totalMisses);
        System.out.println("Total Hits: " + totalHits);
        System.out.println("Set-wise Misses:");
        for (int i = 0; i < setSize; i++) {
            System.out.println("Set " + i + ": " + setMisses[i]);
        }
        System.out.println("Set-wise Hits:");
        for (int i = 0; i < setSize; i++) {
            System.out.println("Set " + i + ": " + setHits[i]);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java javaproject <cacheSize>{powers of 2}{in Kb} <associativity> <traceFilePath>");
            return;
        }
        
        // String[] args1 = new String[3];
        // Scanner obito = new Scanner(System.in);
        // args1[0] = obito.nextLine();
        // args1[1] = obito.nextLine();
        // args1[2] = obito.nextLine();
        // obito.close();
        
        int cacheSize = (int)(1024*Float.parseFloat(args[0]));
       
        int associativity = Integer.parseInt(args[1]);
        String traceFilePath = args[2];

        javaproject cacheSimulation = new javaproject(cacheSize, associativity, traceFilePath);
        cacheSimulation.printResults();
    }
}

