# Cache-Simulator
This program is the implementation of a minimalized, command line cache simulator. It will read from an input file to create a RAM and then accept inputs for the cache to be configured. The cache currently only supports one level.

The input file is provided; it is 256 lines of hexidecimal data (one byte per line) that is meant to populate a simulated RAM. 

To run, navigate to the folder where cachesimulator.java is being held. Then, type "javac \*.java" to compile the code. Finally, type "java cachesimulator input.txt" (or a path to the input folder) to start the program. From here, follow the prompts to create and simulate the cache.

Other inputs within the program are as follows: Cache size (8 to 256 bytes), Data block size (Any power of 2 bytes), Associativity (1, 2, or 4), Replacement policy (1=Random, 2=LRU, 3=LFU), Write-hit policy (1=write-through, 2=write-back), Write-miss policy (1=write-allocate, 2=no-write-allocate). 

Once the cache is configured, type the command name (not the number). Give a hexidecimal address if using cache-read (ie cache-read 0xA3). Give a hexidecimal address and data value if using cache-write (ie cache-write 0x04 0xBF) where 0x04 is the address and 0xBF is the data. All other commands take no other inputs. To exit the program, type 'quit' into the command line.
