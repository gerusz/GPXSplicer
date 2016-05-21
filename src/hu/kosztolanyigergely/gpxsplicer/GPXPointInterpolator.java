package hu.kosztolanyigergely.gpxsplicer;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by gerusz on 2016. 05. 19..
 */
public class GPXPointInterpolator {

    public static void interpolatePoints(GPXFile toInterpolate, GPXFile interpolationSource) {
        interpolatePoints(toInterpolate.trackPoints, interpolationSource.trackPoints);
    }

    public static void interpolatePoints(List<GPXPoint> toInterpolate, List<GPXPoint> interpolationSource) {
        boolean shouldInterpolateHeartRate = false;
        boolean shouldInterpolateElevation = false;

        List<GPXPoint> pointsWithElevation = new LinkedList<>();
        List<GPXPoint> pointsWithHeartRate = new LinkedList<>();

        for(GPXPoint point : interpolationSource) {
            if(point.elevation != null && point.time != null) {
                shouldInterpolateElevation = true;
                pointsWithElevation.add(point);
            }

            if(point.heartRate != null && point.time != null) {
                shouldInterpolateHeartRate = true;
                pointsWithHeartRate.add(point);
            }
        }

        System.out.println("Interpolating the following values:");
        if(shouldInterpolateElevation) {
            System.out.println("\t*Elevation (" + Integer.toString(pointsWithElevation.size()) + " points contain a value)");
        }
        if(shouldInterpolateHeartRate) {
            System.out.println("\t*Heart rate (" + Integer.toString(pointsWithHeartRate.size()) + " points contain a value)");
        }

        int percent = toInterpolate.size() / 100;
        if(percent == 0) {
            percent = 1;
        }
        int done = 0;
        for(GPXPoint point : toInterpolate) {
            if(shouldInterpolateElevation) {
                interpolateElevation(point, pointsWithElevation);
            }
            if(shouldInterpolateHeartRate) {
                interpolateHeartRate(point, pointsWithHeartRate);
            }
            done++;
            if(done % percent == 0) {
                double donePercentage = (100.0 * done)/toInterpolate.size();
                System.out.format("Interpolation at %3f.2%% (%d/%d)\r\n", donePercentage, done, toInterpolate.size());
            }
        }

    }

    public static void interpolateHeartRate(GPXPoint toInterpolate, List<GPXPoint> interpolationSource) {
        Pair<GPXPoint, GPXPoint> neighbors = findNeighbors(toInterpolate, interpolationSource);
        interpolateHeartRate(toInterpolate, neighbors.getKey(), neighbors.getValue());
    }

    public static void interpolateElevation(GPXPoint toInterpolate, List<GPXPoint> interpolationSource) {
        Pair<GPXPoint, GPXPoint> neighbors = findNeighbors(toInterpolate, interpolationSource);
        interpolateElevation(toInterpolate, neighbors.getKey(), neighbors.getValue());
    }

    public static void interpolateHeartRate(GPXPoint toInterpolate, GPXPoint below, GPXPoint above) {
        if(below == null) {
            toInterpolate.heartRate = above.heartRate;
        }
        else if(above == null) {
            toInterpolate.heartRate = below.heartRate;
        }
        else {
            long totalTimeDiff = above.time.getTime() - below.time.getTime();
            long aboveWeight = toInterpolate.time.getTime() - below.time.getTime();
            long belowWeight = above.time.getTime() - toInterpolate.time.getTime();
            toInterpolate.heartRate = Math.round((float)((float)above.heartRate * aboveWeight + (float)below.heartRate * belowWeight) / (float)totalTimeDiff);
        }
    }

    public static void interpolateElevation(GPXPoint toInterpolate, GPXPoint below, GPXPoint above) {
        if(below == null) {
            toInterpolate.elevation = above.elevation;
        }
        else if(above == null) {
            toInterpolate.elevation = below.elevation;
        }
        else {
            long totalTimeDiff = above.time.getTime() - below.time.getTime();
            long aboveWeight = toInterpolate.time.getTime() - below.time.getTime();
            long belowWeight = above.time.getTime() - toInterpolate.time.getTime();
            toInterpolate.elevation = (above.elevation * aboveWeight + below.elevation * belowWeight) / totalTimeDiff;
        }
    }

    public static Pair<GPXPoint, GPXPoint> findNeighbors(GPXPoint toInterpolate, List<GPXPoint> interpolationSource) {
        GPXPoint lastBelow = null;
        GPXPoint firstAbove = null;
        for(GPXPoint point : interpolationSource) {
            if(point.time.getTime() <= toInterpolate.time.getTime()) {
                lastBelow = point;
            }
            if(point.time.getTime() > toInterpolate.time.getTime()) {
                firstAbove = point;
            }
            if(firstAbove != null) break;
        }
        return new Pair<>(lastBelow, firstAbove);
    }

}
