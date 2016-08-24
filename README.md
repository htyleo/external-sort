# external-sort
This is a Java implementation of External Sorting using parallelism. External sorting is basically composed of two phases:

* Partition: partition the input file into a bunch of smaller sorted files.
* Merge: merge the sorted files into a single one, which is the output.

Code Example
-----
```
ExternalSortConfig config = new ExternalSortConfig();
File sourceFile = new File("/sourceFile.txt");
File dstDir = new File("/dst");
File dstFile = ExternalSort.sort(sourceFile, dstDir, config);
```


Features
-----
- We use multi-threading to speed up the partition phase.
- During the partition phase, every line will be included in a certain partition in its entirely (i.e. no line will not be partitioned in between).
- We treat the first and last several lines of the source file as header and tail, each of which will be written to a separate file. Consequently, after the partition phase, there will be a header file, a tail file and a bunch of (sorted) body files.
- You can decide whether to ignore the leading and trailing blank lines in the source file.
- We support user-defined line filter and comparator.

Implementation Overview
-----
The basic idea of this implementation is that we logically partition the file into smaller slices, each of which is then sorted in memory and written to file.
Different slices are processed concurrently (i.e. by a pool of threads) in order to improve sorting speed. The memory used could be up to maxPoolSize * (slice size + buffer size).