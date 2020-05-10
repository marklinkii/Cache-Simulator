// File: cachesimulator.java
// Author(s): Mark Link, Benjamin Ramon
// Date: 04/30/2020
// Section: 509
// E-mail: markuslink@tamu.edu
// Description:
// The content of this file implements our main class, which gets user input from a command line, then reads in data from our
// input.txt file to be saved to RAM. We then take commands from the user, updating our cache based on the given commands

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class cachesimulator {

    static int hits = 0;
    static int misses = 0;
    static int eviction_line = -1;
    static ArrayList<String> RAM = new ArrayList<>();
    static ArrayList<ArrayList<CacheLine>> cache = new ArrayList<>();
    /*
     * main function that continues to read in user input from stdin, until desired to quit
     */
    public static void main(String[] args){
        String filename = args[0];
        Scanner infile = null;

        //try opening the input file
        try {
            infile = new Scanner(new FileReader(filename));
        } catch (FileNotFoundException e) {
            //file unable to open
            System.out.println("Error: Unable to open file!");
            System.exit(0);
        }

        if(args.length > 2) {
            System.out.println("Error: invalid input!");
            System.exit(0);
        }
        System.out.println("*** Welcome to the cache simulator ***");
        System.out.println("initialize the RAM:");
        System.out.println("init-ram 0x00 0xFF");

        int lineCounter = 0;

        //loop through data, line by line
        while(infile.hasNextLine()) {
            RAM.add(infile.nextLine());
            lineCounter += 1;
        }

        //ensure correct amount of data in input file
        if(lineCounter != 256) {
            System.out.println("Error: unable to initialize RAM");
            System.exit(0);
        }
        else{ System.out.println("ram successfully initialized!"); }

        Scanner inputs = new Scanner(System.in);

        //inputs for cache configuration
        System.out.println("configure the cache: ");

        System.out.print("cache size: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int cache_size = inputs.nextInt();

        System.out.print("data block size: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int data_block_size = inputs.nextInt();

        System.out.print("associativity: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int associativity = inputs.nextInt();

        System.out.print("replacement_policy: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int replacement_policy = inputs.nextInt();

        System.out.print("write hit policy: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int write_hit_policy = inputs.nextInt();

        System.out.print("write miss policy: ");
        if(!inputs.hasNextInt()){
            System.out.println("Invalid input!");
            System.exit(0);
        }
        int write_miss_policy = inputs.nextInt();
        inputs.nextLine();

        //check for valid cache size
        if(8 > cache_size || cache_size > 256) {
            System.out.println("Error: invalid cache size!");
            System.exit(0);
        }

        //check for valid associativity
        if(associativity != 1 && associativity != 2 && associativity != 4){
            System.out.println("Error: invalid set associativity!");
            System.exit(0);
        }

        //calculate number of sets in the cache
        int sets = cache_size / (data_block_size * associativity);
        //create the sets
        for(int i = 0; i < sets; i++){
            cache.add(new ArrayList<CacheLine>());
        }

        //create the cache lines within the sets
        for(int j = 0; j < sets; j++){
            for(int k = 0; k<associativity; k++) {
                CacheLine cl = new CacheLine(data_block_size, sets, j, k);
                cache.get(j).add(cl);
            }
        }

        //calculate the number of tag bits in each address
        int tag_bits = 8 - (log2(data_block_size) + log2(sets));

        //initialization of variables for the main loop
        System.out.println("*** Cache simulator menu ***\ntype one command:\n1. cache-read\n2. cache-write\n3. cache-flush" +
                "\n4. cache-view\n5. memory-view\n6. cache-dump\n7. memory-dump\n8. quit\n" +
                "****************************");
        String userInput = inputs.nextLine();
        String[] inputArray = userInput.split(" ");

        String command = inputArray[0];
        String read_data = "";
        String address = "";
        String write_data = "";
        if(inputArray.length == 2){
            read_data = inputArray[1];
        }
        else if(inputArray.length == 3){
            address = inputArray[1];
            write_data = inputArray[2];
        }
        boolean simulate = true;

        //main loop of program (cache simulation)
        while(simulate) {
            if(command.equalsIgnoreCase("cache-read")) {
                if(inputArray.length != 2){
                    System.out.println("Invalid input!");
                    System.exit(0);
                }
                int set = getSet(read_data, sets, tag_bits);
                int offset = getOffset(read_data, sets, tag_bits);
                String tag = getTag(read_data, tag_bits);
                cache_read(read_data, set, offset, tag, replacement_policy, data_block_size);
            }
            else if(command.equalsIgnoreCase("cache-write")) {
                if(inputArray.length != 3){
                    System.out.println("Invalid input!");
                    System.exit(0);
                }
                int set = getSet(address, sets, tag_bits);
                int offset = getOffset(address, sets, tag_bits);
                String tag = getTag(address, tag_bits);
                cache_write(address, write_data, set, offset, tag, write_hit_policy, write_miss_policy, data_block_size, replacement_policy);
            }
            else if(command.equalsIgnoreCase("cache-flush")) {
                cache_flush(cache_size, data_block_size, associativity);
                System.out.println("cache_cleared");
            }
            else if(command.equalsIgnoreCase("cache-view")) {
                String rep_pol = null;
                String rep_hit = null;
                String rep_miss = null;
                switch(replacement_policy){
                    case 1: rep_pol = "random_replacement"; break;
                    case 2: rep_pol = "least_recently_used"; break;
                    case 3: rep_pol = "least_frequently_used"; break;
                }
                switch(write_hit_policy){
                    case 1: rep_hit = "write_through"; break;
                    case 2: rep_hit = "write_back"; break;
                }
                switch (write_miss_policy){
                    case 1: rep_miss = "write_allocate"; break;
                    case 2: rep_miss = "no_write_allocate"; break;
                }
                cache_view(cache_size, data_block_size, associativity, rep_pol, rep_hit, rep_miss, hits, misses);
            }
            else if(command.equalsIgnoreCase("memory-view")) {
                memory_view();
            }
            else if(command.equalsIgnoreCase("cache-dump")) {
                cache_dump();
            }
            else if(command.equalsIgnoreCase("memory-dump")) {
                memory_dump();
            }
            else if(command.equalsIgnoreCase("quit")) {
                simulate = false;
            }
            else {
                System.out.print("Error: invalid input");
                System.exit(0);
            }

            if(simulate) {
                System.out.println("*** Cache simulator menu ***\ntype one command:\n1. cache-read\n2. cache-write\n3. cache-flush" +
                        "\n4. cache-view\n5. memory-view\n6. cache-dump\n7. memory-dump\n8. quit\n" +
                        "****************************");
                userInput = inputs.nextLine();
                inputArray = userInput.split(" ");

                command = inputArray[0];
                if(inputArray.length == 2){
                    read_data = inputArray[1];
                }
                else if(inputArray.length == 3){
                    address = inputArray[1];
                    write_data = inputArray[2];
                }
                command = inputArray[0];
            }
        }
    }

    //returns log base 2 of an integer
    public static int log2(int num){
        return (int) (Math.log(num)/Math.log(2));
    }

    /*
     * public member cache_read, takes an address, set, offset, tag, replacement_policy, and block size
     * returns a updated cache from a least_frequently_used policy
     */
    public static ArrayList<ArrayList<CacheLine>> cache_read(String address, int set, int offset, String tag, int replacement_policy, int block_size) {
        String binary = getBinary(address);
        boolean hit = false;
        String data = "";
        //check for a cache hit
        for(CacheLine line: cache.get(set)){
            //hit found
            if(line.getTag().equalsIgnoreCase(tag) && line.isValid()){
                hit = true;
                hits += 1;
                data = line.getData().get(offset);
                eviction_line = -1;
                line.setTime_last_used(0);
                line.set_frequency(line.get_frequency()+1);
                //increment time of last access for other lines
                for(CacheLine l2: cache.get(set)){
                    if(l2.equals(line)) continue;
                    else{
                        l2.setTime_last_used(l2.getTime_last_used() + 1);
                    }
                }
                break;
            }
        }
        //no hit
        if (!hit) {
            misses += 1;
            //run a replacement method based on current policy
            switch(replacement_policy){
                //do replacement policies
                case 1:
                    random_replacement(block_size, set, address, tag, offset); break;
                case 2:
                    least_recently_used(block_size, set, address, tag, offset); break;
                case 3:
                    least_frequently_used(block_size, set, address, tag, offset); break;
            }
            int decimal = Integer.parseInt(binary, 2);
            data = RAM.get(decimal);
        }
        int int_tag = Integer.parseInt(tag, 2);
        String hex_tag = Integer.toHexString(int_tag);
        System.out.println("set:"+set);
        System.out.println("tag:"+hex_tag.toUpperCase());
        if (hit) {
            System.out.println("hit:yes");
            System.out.println("eviction_line:" + eviction_line);
            System.out.println("ram_address:-1");
            System.out.println("data:0x"+data);
        }
        else {
            System.out.println("hit:no");
            System.out.println("eviction_line:"+ eviction_line);
            System.out.println("ram_address:"+address);
            System.out.println("data:0x"+data);
        }
        return cache;
    }


    /*
     * public member least_frequently_used, takes block_size, sets, address, tag, and offset
     * returns a updated cache from a least_frequently_used policy
     */
    private static ArrayList<ArrayList<CacheLine>> least_frequently_used(int block_size, int set, String address, String tag, int offset) {
        address = getBinary(address);
        int ramLine = Integer.parseInt(address, 2);
        ArrayList<String> newData = new ArrayList<>();
        int freq = -1;
        CacheLine line = null;
        ArrayList<CacheLine> cur_set = cache.get(set);
        //loop through current set
        for(int i = 0; i < cur_set.size(); i++){
            //base case, initialize last time used and line
            if(i==0) {
                line = cur_set.get(i);
                freq = line.get_frequency();
            }
            else{
                //if the frequency of current line is lower, assign a new replacement line
                if(cur_set.get(i).get_frequency() < freq){
                    line = cur_set.get(i);
                    freq = line.get_frequency();
                }
            }
        }
        //updates ram if evicted line had dirty bit set
        int updateLine = Integer.parseInt(line.getAddress(), 2);
        int index = 0;
        if(line.getDirtyBit() == 1){
            for(int j = updateLine; j < updateLine + block_size; j++){
                RAM.set(j, line.getData().get(index));
                index++;
            }
        }
        eviction_line = line.getLineNum();
        if(block_size + ramLine > 256){
            for(int i = 256-block_size; i < 256; i++){
                newData.add(RAM.get(i));
            }
        }
        else {
            //fills data with correct offset
            for(int i = ramLine-offset; i < ramLine; i++){
                newData.add(RAM.get(i));
            }
            for(int j = ramLine; j < ramLine+(block_size-offset); j++){
                newData.add(RAM.get(j));
            }
        }
        line.setData(newData);
        line.setValidBit(1);
        line.setTag(tag);
        line.setDirtyBit(0);
        line.set_frequency(1);
        return cache;
    }

    /*
     * public member least_recently_used, takes block_size, sets, address, tag, and offset
     * returns a updated cache from a least_recently_used policy
     */
    private static ArrayList<ArrayList<CacheLine>> least_recently_used(int block_size, int set, String address, String tag, int offset) {
        address = getBinary(address);
        int ramLine = Integer.parseInt(address, 2);
        ArrayList<String> newData = new ArrayList<>();
        int useAmt = -1;
        CacheLine line = null;
        ArrayList<CacheLine> cur_set = cache.get(set);
        //loop through current set
        for(int i = 0; i < cur_set.size(); i++){
            //base case, initialize last time used and line
            if(i==0) {
                line = cur_set.get(i);
                useAmt = line.getTime_last_used();
            }
            else{
                //if the time since last use is greater, assign a new replacement line
                if(cur_set.get(i).getTime_last_used() > useAmt){
                    line = cur_set.get(i);
                    useAmt = line.getTime_last_used();
                }
            }
        }
        //updates ram if evicted line had dirty bit set
        int updateLine = Integer.parseInt(line.getAddress(), 2);
        int index = 0;
        if(line.getDirtyBit() == 1){
            for(int j = updateLine; j < updateLine + block_size; j++){
                RAM.set(j, line.getData().get(index));
                index++;
            }
        }
        eviction_line = line.getLineNum();
        if(block_size + ramLine > 256){
            for(int i = 256-block_size; i < 256; i++){
                newData.add(RAM.get(i));
            }
        }
        else {
            //fills data with correct offset
            for(int i = ramLine-offset; i < ramLine; i++){
                newData.add(RAM.get(i));
            }
            for(int j = ramLine; j < ramLine+(block_size-offset); j++){
                newData.add(RAM.get(j));
            }
        }
        line.setData(newData);
        line.setValidBit(1);
        line.setTag(tag);
        line.setDirtyBit(0);
        line.setTime_last_used(0);
        //increment time of use for others
        for(CacheLine l2: cache.get(set)){
            if(l2.equals(line)) continue;
            else{
                l2.setTime_last_used(l2.getTime_last_used() + 1);
            }
        }
        return cache;
    }

    /*
     * public member random_replacement, takes block_size, sets, address, tag, and offset
     * returns a updated cache from a random_replacement policy
     */
    public static ArrayList<ArrayList<CacheLine>> random_replacement(int block_size, int set, String address, String tag, int offset) {
        boolean replaced = false;
        int lineNum;
        address = getBinary(address);
        int ramLine = Integer.parseInt(address, 2);
        ArrayList<String> newData = new ArrayList<>();
        //search for line with invalid bit and replace with ram data
        for(CacheLine line: cache.get(set)){
            if(!line.isValid()){
                replaced = true;
                eviction_line = line.getLineNum();
                //fills up new data with data in ram
                if(block_size + ramLine > 256){
                    for(int i = 256-block_size; i < 256; i++){
                        newData.add(RAM.get(i));
                    }
                }
                else {
                    //fills data with correct offset
                    for(int i = ramLine-offset; i < ramLine; i++){
                        newData.add(RAM.get(i));
                    }
                    for(int j = ramLine; j < ramLine+(block_size-offset); j++){
                        newData.add(RAM.get(j));
                    }
                }
                line.setData(newData);
                line.setValidBit(1);
                line.setTag(tag);
                break;
            }
        }
        //if no invalid bits set, get a random line
        if(!replaced){
            Random r = new Random();
            lineNum = r.nextInt(cache.get(0).size());
            for(CacheLine line: cache.get(set)){
                if(line.getLineNum() == lineNum){
                    //updates ram if evicted line had dirty bit set
                    if(line.getDirtyBit() == 1){
                        int updateLine = Integer.parseInt(line.getAddress(), 2);
                        int index = 0;
                        for(int j = updateLine; j < updateLine + block_size; j++){
                            RAM.set(j, line.getData().get(index));
                            index++;
                        }
                    }
                    eviction_line = line.getLineNum();
                    if(block_size + ramLine > 256){
                        for(int i = 256-block_size; i < 256; i++){
                            newData.add(RAM.get(i));
                        }
                    }
                    else {
                        //fills data with correct offset
                        for(int i = ramLine-offset; i < ramLine; i++){
                            newData.add(RAM.get(i));
                        }
                        for(int j = ramLine; j < ramLine+(block_size-offset); j++){
                            newData.add(RAM.get(j));
                        }
                    }
                    line.setData(newData);
                    line.setValidBit(1);
                    line.setTag(tag);
                    line.setDirtyBit(0);
                }
            }
        }
        return cache;
    }

    /*
     * public member function getBinary, takes an address (in hex)
     * returns a binary string from a given hexadecimal address
     */
    public static String getBinary(String add) {
        String hexadecimal = add.substring(2);
        int decimal = Integer.parseInt(hexadecimal, 16);
        String bin = Integer.toBinaryString(decimal);
        while(bin.length()< 8){
            bin = "0"+bin;
        }
        return bin;
    }

    /*
     * public member function getSet, takes an address, set bits, tag bits
     * returns the set of an address from the set and tag bits
     */
    public static int getSet(String address, int S, int t){
        String bin = getBinary(address);
        int quantity = log2(S);
        String setBinary = bin.substring(t, t+quantity);
        if(setBinary.equals("")) return 0;
        return Integer.parseInt(setBinary, 2);
    }

    /*
     * public member function getOffset, takes an address, set bits, tag bits
     * returns the offset of an address from the set and tag bits
     */
    public static int getOffset(String address,  int S, int t){
        String bin = getBinary(address);
        String offsetBinary = bin.substring(t+log2(S));
        if(offsetBinary.equals("")) return 0;
        return Integer.parseInt(offsetBinary, 2);
    }

    /*
     * public member function getTag, takes an address and a t for tag bits
     * returns the tag for an address
     */
    public static String getTag(String address, int t){
        String bin = getBinary(address);
        return bin.substring(0, t);
    }

    /*
     * public member function cache_flush, takes a cache size, data block size, and associativity
     * returns an updated cache, contents cleared, and memory updated based on dirty bit
     */
    public static ArrayList<ArrayList<CacheLine>> cache_flush(int cache_size, int data_block_size, int associativity){
        //copies any dirty cache memory into ram memory
        for(ArrayList<CacheLine> set: cache){
            for(CacheLine l: set){
                //dirty bit of line set, copy to RAM
                if(l.getDirtyBit() == 1){
                    String address = l.getAddress();
                    //find line in ram to start at
                    int ram_line = Integer.parseInt(address, 2);
                    //set ram contents based on size of data block
                    for(int i = 0; i < data_block_size; i++) {
                        RAM.set(ram_line+i, l.getData().get(i));
                    }
                }
            }
        }
        //creates an empty cache to be returned
        ArrayList<ArrayList<CacheLine>> cache_new = new ArrayList<>();
        int sets = cache_size / (data_block_size * associativity);
        for(int i = 0; i < sets; i++){
            cache_new.add(new ArrayList<CacheLine>());
        }

        for(int j = 0; j < sets; j++){
            for(int k = 0; k<associativity; k++) {
                CacheLine cl = new CacheLine(data_block_size, sets, j, k);
                cache_new.get(j).add(cl);
            }
        }
        return cache_new;
    }

    /*
     * public member function cache_view, takes a cache size, data block size, associativity, replacement_type,
     * write hit type, write miss type, number of cache hit, and number of cache miss
     * does not return anything, but outputs to a the contents of cache
     */
    public static void cache_view(int cache_sz, int data_block_sz, int assoc, String replacement_type, String write_hit_type, String write_miss_type,  int num_cache_hit, int num_cache_miss) {
        System.out.println("cache_size:"+cache_sz);
        System.out.println("data_block_size:"+data_block_sz);
        System.out.println("associativity:"+assoc);
        System.out.println("replacement_policy:"+replacement_type);
        System.out.println("write_hit_policy:"+write_hit_type);
        System.out.println("write_miss_policy:"+write_miss_type);
        System.out.println("number_of_cache_hits:"+num_cache_hit);
        System.out.println("number_of_cache_misses:"+num_cache_miss);
        System.out.println("cache_content:");
        for (ArrayList<CacheLine> sets : cache) {
            for (CacheLine line : sets) {
                System.out.println(line);
            }
        }

    }

    /*
     * public member function memory_dump, takes no parameters
     * does not return anything, but outputs to a text file the contents of memory
     */
    public static void memory_dump() {
        try {
            FileWriter out = new FileWriter("ram.txt", false);
            PrintWriter writer = new PrintWriter(out);
            for (String memory: RAM) {
                writer.println(memory);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Cannot output RAM to ram.txt");
        }
    }

    /*
     * public member function cache_dump,
     * does not return anything, but outputs to a text file the contents of cache
     */
    public static void cache_dump() {
        try {
            FileWriter out = new FileWriter("cache.txt", false);
            PrintWriter writer = new PrintWriter(out);
            for (ArrayList<CacheLine> sets : cache) {
                for (CacheLine line : sets) {
                    ArrayList<String> data_line = line.getData();
                    for (String d : data_line) {
                        writer.print(d + " ");
                    }
                    writer.println();
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Cannot output Cache to cache.txt");
        }
    }

    /*
     * public member function memory_view, takes no parameter, just a command called from user input
     * outputs to terminal the status of our ram
     */
    public static void memory_view(){
        System.out.println("memory_size:"+RAM.size());
        System.out.println("memory_content:");
        System.out.println("Address:Data");
        for(int i = 0; i < RAM.size(); i++){
            if(i==0){
                System.out.print("0x00:");
                System.out.print(RAM.get(i));
            }
            else if(i%8 == 0){
                System.out.print("\n");
                String hex = Integer.toHexString(i).toUpperCase();
                if(hex.length()==1){
                    hex = "0"+hex;
                }
                System.out.print("0x"+hex+":");
                System.out.print(RAM.get(i));
            }
            else{
                System.out.print(" "+RAM.get(i));
            }
        }
        System.out.print("\n");
    }

    /*
     * public member function cache_write, takes an address, write_data, set, offset, tag, hit_policy
     * miss_policy, data_block_size and replacement_policy
     * returns the updated cache from user input, and outputs to terminal cache status
     */
    public static ArrayList<ArrayList<CacheLine>> cache_write(String address, String write_data, int set, int offset, String tag, int hit_policy, int miss_policy, int data_block_size, int replacement_policy) {
        //binary of given address
        String binary = getBinary(address);
        boolean hit = false;
        String data = "";
        CacheLine hit_line = null;
        ArrayList<String> line_data = new ArrayList<>();
        ArrayList<String> new_data = new ArrayList<>();
        int lineNum = -1;
        //check for a cache hit
        for(CacheLine line: cache.get(set)){
            //hit found
            if(line.getTag().equalsIgnoreCase(tag) && line.isValid()){
                hit = true;
                hits++;
                data = line.getData().get(offset);
                eviction_line = -1;
                line.setTime_last_used(0);
                line.set_frequency(line.get_frequency()+1);
                line_data = line.getData();
                lineNum = line.getLineNum();
                hit_line = line;
                //increment time of last access for other lines
                for(CacheLine l2: cache.get(set)){
                    if(l2.equals(line)) continue;
                    else{
                        l2.setTime_last_used(l2.getTime_last_used() + 1);
                    }
                }
                break;
            }
        }
        //line number of address for ram
        int decimal = Integer.parseInt(binary, 2);
        //cache hit
        if(hit){
            for(int i = 0; i < line_data.size(); i++){
                if(line_data.get(i).equals(data)){
                    new_data.add(write_data.substring(2));
                }
                else{
                    new_data.add(line_data.get(i));
                }
            }
            switch(hit_policy){
                //write through
                case 1:
                    hit_line.setData(new_data);
                    RAM.set(decimal, write_data.substring(2));
                    cache.get(set).set(lineNum,hit_line);
                    break;
                //write back
                case 2:
                    hit_line.setData(new_data);
                    hit_line.setDirtyBit(1);
                    cache.get(set).set(lineNum,hit_line);
                    break;
            }
        }
        //cache miss
        else{
            misses++;
            //sets correct eviction line
            switch(replacement_policy){
                case 1:
                    if(miss_policy == 1) {
                        eviction_line = getEvictionLineRR(set);
                    }
                    else{
                        eviction_line = -1;
                    }
                    break;
                case 2:
                    //increment time of use for others if using allocate
                    if(miss_policy == 1) {
                        eviction_line = getEvictionLineLRU(set);
                        cache.get(set).get(eviction_line).setTime_last_used(0);
                        for (CacheLine l2 : cache.get(set)) {
                            if (l2.equals(cache.get(set).get(eviction_line))) continue;
                            else {
                                l2.setTime_last_used(l2.getTime_last_used() + 1);
                            }
                        }
                    }
                    else{
                        eviction_line = -1;
                    }
                    break;
                case 3:
                    if(miss_policy == 1) {
                        eviction_line = getEvictionLineLFU(set);
                        cache.get(set).get(eviction_line).set_frequency(1);
                    }
                    else{
                        eviction_line = -1;
                    }
                    break;
            }
            switch (miss_policy){
                //write allocate
                case 1:
                    //updates ram if evicted line had dirty bit set
                    int updateLine = Integer.parseInt(cache.get(set).get(eviction_line).getAddress(), 2);
                    int index = 0;
                    if(cache.get(set).get(eviction_line).getDirtyBit() == 1){
                        for(int j = updateLine; j < updateLine + data_block_size; j++){
                            RAM.set(j, cache.get(set).get(eviction_line).getData().get(index));
                            index++;
                        }
                    }
                    ArrayList<String> new_block_data = new ArrayList<>();
                    //grab entire block from RAM & set content of cache to that block
                    if(data_block_size + decimal > 256){
                        for(int i = 256-data_block_size; i < 256; i++){
                            new_block_data.add(RAM.get(i));
                        }
                    }
                    else {
                        //fills data with correct offset
                        for(int i = decimal-offset; i < decimal; i++){
                            new_block_data.add(RAM.get(i));
                        }
                        for(int j = decimal; j < decimal+(data_block_size-offset); j++){
                            new_block_data.add(RAM.get(j));
                        }
                    }
                    if(hit_policy == 1){
                        //update byte in RAM && bring to cache
                        RAM.set(decimal, write_data.substring(2));
                        new_block_data.set(offset, write_data.substring(2));
                        cache.get(set).get(eviction_line).setDirtyBit(0);
                    }
                    else{
                        new_block_data.set(offset, write_data.substring(2));
                        cache.get(set).get(eviction_line).setDirtyBit(1);
                    }
                    cache.get(set).get(eviction_line).setData(new_block_data);
                    cache.get(set).get(eviction_line).setTag(tag);
                    cache.get(set).get(eviction_line).setValidBit(1);
                    break;
                //no write allocate
                case 2:
                    //set contents of RAM
                    RAM.set(decimal, write_data.substring(2));
                    break;
            }
        }
        int int_tag = Integer.parseInt(tag, 2);
        String hex_tag = Integer.toHexString(int_tag);
        System.out.println("set:"+set);
        System.out.println("tag:"+hex_tag.toUpperCase());
        if (hit) {
            System.out.println("write_hit:yes");
            System.out.println("eviction_line:" + eviction_line);
            System.out.println("ram_address:-1");
            System.out.println("data:0x"+write_data.substring(2).toUpperCase());
            System.out.println("dirty_bit:"+hit_line.getDirtyBit());
        }
        else {
            System.out.println("hit:no");
            System.out.println("eviction_line:"+ eviction_line);
            if(miss_policy == 1) System.out.println("ram_address:"+address);
            else System.out.println("ram_address:-1");
            System.out.println("data:0x"+write_data.substring(2).toUpperCase());
            if(miss_policy == 1 && hit_policy == 2) System.out.println("dirty_bit:1");
            else System.out.println("dirty_bit:0");
        }
        return cache;
    }

    /*
     * public member function getEvictionLineRR, which takes in an integer set
     * returns the line number that is to be evicted from cache given the random replacement policy guidelines
     */
    public static int getEvictionLineRR(int set){
        int lineNum;
        //search for line with invalid bit and replace with ram data
        for(CacheLine line: cache.get(set)){
            if(!line.isValid()){
                return line.getLineNum();
            }
        }
        //if no invalid bits set, get a random line
        Random r = new Random();
        lineNum = r.nextInt(cache.get(0).size());
        return lineNum;
    }

    /*
     * public member function getEvictionLineLRU, which takes in an integer set
     * returns the line number that is to be evicted from cache given the LRU policy guidelines
     */
    public static int getEvictionLineLRU(int set){
        int useAmt = -1;
        CacheLine line = null;
        ArrayList<CacheLine> cur_set = cache.get(set);
        //loop through current set
        for(int i = 0; i < cur_set.size(); i++){
            //base case, initialize last time used and line
            if(i==0) {
                line = cur_set.get(i);
                useAmt = line.getTime_last_used();
            }
            else{
                //if the time since last use is greater, assign a new replacement line
                if(cur_set.get(i).getTime_last_used() > useAmt){
                    line = cur_set.get(i);
                    useAmt = line.getTime_last_used();
                }
            }
        }
        return line.getLineNum();
    }

    /*
     * public member function getEvictionLineLFU, which takes in an integer set
     * returns the line number that is to be evicted from cache given the LFU policy guidelines
     */
    public static int getEvictionLineLFU(int set){
        int freq = -1;
        CacheLine line = null;
        ArrayList<CacheLine> cur_set = cache.get(set);
        //loop through current set
        for(int i = 0; i < cur_set.size(); i++){
            //base case, initialize last time used and line
            if(i==0) {
                line = cur_set.get(i);
                freq = line.get_frequency();
            }
            else{
                //if the frequency of current line is lower, assign a new replacement line
                if(cur_set.get(i).get_frequency() < freq){
                    line = cur_set.get(i);
                    freq = line.get_frequency();
                }
            }
        }
        return line.getLineNum();
    }
}
