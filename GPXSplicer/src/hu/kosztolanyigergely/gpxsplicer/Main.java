package hu.kosztolanyigergely.gpxsplicer;

import java.io.Console;

public class Main {

    public static void main(String[] args) {
	    GPXFile toInterpolate = new GPXFile(args[0]);
        System.out.println("File to interpolate (" + args[0] + ") loaded. Found " + Integer.toString(toInterpolate.trackPoints.size()) + " track points.");
        GPXFile interpolationSource = new GPXFile(args[1]);
        System.out.println("Interpolation source (" + args[1] + ") loaded. Found " + Integer.toString(interpolationSource.trackPoints.size()) + " track points.");

        String outputFilename = null;

        if(args.length > 2) {
            outputFilename = args[2];
        }
        else {
            outputFilename = args[0].replace(".gpx", ".output.gpx");
        }
        System.out.println("Output will go to " + outputFilename);
        System.out.println("Starting interpolation...");

        GPXPointInterpolator.interpolatePoints(toInterpolate, interpolationSource);
        System.out.println("Interpolation completed. Saving to output file...");
        toInterpolate.saveXmlDocument(outputFilename);
        System.out.println("Done.");
    }
}
