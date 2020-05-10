// File: CacheLine.java
// Author(s): Mark Link, Benjamin Ramon
// Date: 04/30/2020
// Section: 509
// E-mail: markuslink@tamu.edu
// Description:
// The content of this file implements our sub class cacheline, which represents a line in our cache ArrayList
// from main, and has all the member variables that a cacheline has

import java.util.ArrayList;

public class CacheLine {
    protected int tbits;
    protected int sbits;
    protected int bbits;
    protected int dirtyBit;
    protected int validBit;
    protected ArrayList<String> data = new ArrayList<>();
    protected int lineNum;
    protected int time_last_used;
    protected int use_frequency;
    protected String tag;
    protected int set;

    /*
     * default constructor for cache line, defaults all member variables
     */
    public CacheLine(int B, int total_sets, int cur_set, int lineNum){
        this.time_last_used = 0;
        this.use_frequency = 0;
        this.lineNum = lineNum;
        this.sbits = cachesimulator.log2(total_sets);
        this.bbits = cachesimulator.log2(B);
        this.tbits = 8 - (sbits + bbits);
        this.dirtyBit = 0;
        this.validBit = 0;
        this.set = cur_set;
        this.tag = "";
        for(int i = 0; i < tbits; i++){
            tag += "0";
        }
        for(int i = 0; i < B; i++){
            data.add("00");
        }
    }

    /*
     * getter for tag
     * returns the tag of the cache line
     */
    public String getTag() {
        return this.tag;
    }

    /*
     * getter for line number
     * returns the line number for the cacheline
     */
    public int getLineNum(){
        return this.lineNum;
    }

    /*
     * getter for data
     * returns the data of the cache line
     */
    public ArrayList<String> getData(){
        return this.data;
    }

    /*
     * getter for the dirtyBit
     * returns the dirtyBit of the cache line
     */
    public int getDirtyBit(){
        return this.dirtyBit;
    }

    /*
     * getter for last time the cache line was used
     * returns the time the cache line was used
     */
    public int getTime_last_used(){
        return this.time_last_used;
    }

    /*
     * getter for frequency
     * returns the frequency of the cache line
     */
    public int get_frequency(){
        return this.use_frequency;
    }

    /*
     * getter for address
     * returns binary address of line (offset 0)
     */
    public String getAddress(){
        String address = "";
        String t = tag;
        while(t.length() != tbits && tbits != 0){
            t = "0" + t;
        }
        address += t;
        String s = Integer.toBinaryString(set);
        while(s.length() != sbits && sbits!= 0){
            s = "0" + s;
        }
        address += s;
        while(address.length() != sbits+bbits+tbits){
            address += "0";
        }
        return address;
    }

    /*
     * member function isValid
     * returns if there is a vailid bit for the cache line
     */
    public boolean isValid(){
        if(validBit == 1) return true;
        return false;
    }

    /*
     * setter for vailidbit, takes a bit
     * does not return anything, updates current validBit for cacheline
     */
    public void setValidBit(int bit){
        this.validBit = bit;
    }

    /*
     * setter for dirtyBit, takes a bit
     * does not return anything, updates current dirtyBit for cacheline
     */
    public void setDirtyBit(int bit){
        this.dirtyBit = bit;
    }

    /*
     * setter for frequency, takes a freq
     * does not return anything, updates current frequency for cacheline
     */
    public void set_frequency(int freq){
        this.use_frequency = freq;
    }

    /*
     * setter for time last used, takes a time
     * does not return anything, updates current time for cacheline
     */
    public void setTime_last_used(int time){
        this.time_last_used = time;
    }

    /*
     * setter for data, takes a data
     * does not return anything, updates current data for cacheline
     */
    public void setData(ArrayList<String> data){
        this.data = data;
    }

    /*
     * setter for tag, takes a tag
     * does not return anything, updates current tag for cacheline
     */
    public void setTag(String tag){
        this.tag = tag;
    }

    /*
     * to string method for a cache line
     * returns the string of the overloaded output
     */
    @Override
    public String toString(){
        String line;
        line = validBit + " " + dirtyBit + " ";
        int dec = Integer.parseInt(tag, 2);
        String hex = Integer.toHexString(dec);
        if(hex.length()==1){
            hex = "0"+hex;
        }
        line += hex.toUpperCase();
        for(String d: data){
            line = line + " " + d;
        }
        return line;
    }
}
