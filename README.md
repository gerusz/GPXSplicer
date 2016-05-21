# GPXSplicer
This little Java application splices elevation and heart rate values from one GPX into another one.
----
## Usage
GPXSplicer file_to_splice splice_source [output_file]
## Operation
The operation of this little utility is very simple. It parses the two GPX files, interpolates the elevation and heartrate values in the first file based on the second and saves the resulting file in the output file. If no output file is given, it will be the first file's name with a .output.gpx replacing the .gpx.

The interpolation is based on time. Thus it's fairly important to make sure that the times in the files are either:

1. In the same timezone, or
2. Contain time zones
 
----
## Plans for future improvement

* Determine time offset between the files automatically
* Use geographic distance for interpolation
* Select data to interpolate
* GUI?
