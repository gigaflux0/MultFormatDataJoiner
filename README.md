
Java application for reading csv, json and xml files. Each file type is parsed using open libraries to maintain safety, with xml using build in Java libraries. 

The input files of each type (multiples of any and any combination allowed) are merged together, sorted if required and written to new json, xml and csv files that contains all the records formatted appropriatly.

If I had more time more unit tests would be written to cover more edge cases.
If I had even more time than that the structure of the code would be rewritten to be more dynamic with the format of the data being read in, writing back to the learned input format rather than to the current static formats.


