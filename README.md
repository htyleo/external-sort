# external-sort
This is an implementation of External Sorting in Java. External sorting is basically composed of two phases:

* Partition: partition the input file into a bunch of smaller sorted files.
* Merge: merge the sorted files into a single one, which is the output.

Code example
-----
```
ExternalSortConfig config = new ExternalSortConfig();
File sourceFile = new File("/sourceFile.txt");
File dstDir = new File("/dst");
File dstFile = ExternalSort.sort(sourceFile, dstDir, config);
```


Features
-----
- The sorting is performed by multiple threads concurrently.
- Every line will be included in some slice in its entirely (i.e. no line will not be partitioned in between).
- The first and last several lines of the source file are treated as header and tail, respectively. They will be stored in two separate files.
- There is an option whether to ignore the leading and trailing blank lines in the source file.
- Support user-defined line filter and comparator.

Implementation overview
-----
The basic idea of this implementation is that we logically partition the file into smaller slices, each of which is then sorted in memory and written to file.
Different slices are processed concurrently (i.e. by a pool of threads) in order to improve sorting speed. The memory used could be up to maxPoolSize * (slice size + buffer size).