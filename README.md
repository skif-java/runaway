# Runaway Exception

RunawayException is unchecked runtime exception used to handle software bugs -
situations which were not yet handled in the code - e.g. null pointers, index out of bounds, conversion errors, etc.


The main difference of this exception over common runtime exceptions is ability to add snapshots of data values 
to the matching stack trace frame of the exception which allows to get stack traces like the one below:

```
->> 2:org.runaway.exception.C.getData[21]: [paramA=54110][paramB=my test param][data=null][jj=159]
->> 1:org.runaway.exception.B.calc[17]: [paramA=my test param][x=35]
->> 0:org.runaway.exception.A.main[16]: [testParam=my test param]
```

Original version of this kind of exception handling was created somewhere in 2003 or earlier.  
In 2019 Java&#8482; still does not support ability to add data elements to a specific exception stack frame.