# external-sort

[![Licence](https://img.shields.io/dub/l/vibe-d.svg)](LICENSE.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.htyleo.extsort/extsort/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cextsort)
[![Docs](http://www.javadoc.io/badge/com.htyleo.extsort/extsort.svg)](http://www.javadoc.io/doc/com.htyleo.extsort/extsort/)
[![Build Status](https://travis-ci.org/htyleo/external-sort.svg?branch=master)](https://travis-ci.org/htyleo/external-sort)


This is a Java implementation of External Sorting using parallelism. External sorting is basically composed of two phases:

* Partition: partition the input file into a bunch of smaller sorted files.
* Merge: merge the sorted files into a single one, which is the output.

Maven Dependency
-----
```xml
<dependency>
  <groupId>com.htyleo.extsort</groupId>
  <artifactId>extsort</artifactId>
  <version>1.0.0</version>
</dependency>
```

Code Example
-----
```
ExternalSortConfig config = new ExternalSortConfig();
File sourceFile = new File("/sourceFile.txt");
File dstDir = new File("/dst");
File dstFile = ExternalSort.sort(sourceFile, dstDir, config);
```

API Documentation
-----
[http://www.javadoc.io/doc/com.htyleo.extsort/extsort/](http://www.javadoc.io/doc/com.htyleo.extsort/extsort/)

Features
-----
- We use multi-threading to speed up the partition phase.
- During the partition phase, every line will be included in a certain partition in its entirety (i.e. no line will be partitioned in between).
- We treat the first and last several lines of the source file as header and tail, each of which will be written to a separate file. Consequently, after the partition phase, there will be a header file, a tail file and a bunch of (sorted) body files.
- You can decide whether to ignore the leading and trailing blank lines in the source file.
- We support user-defined line filter and comparator.

Implementation Overview
-----
The basic idea of this implementation is that we logically partition the file into smaller slices, each of which is then sorted in memory and written to file.
Different slices are processed concurrently (i.e. by a pool of threads) in order to improve sorting speed. The memory used could be up to maxPoolSize * (slice size + buffer size).
