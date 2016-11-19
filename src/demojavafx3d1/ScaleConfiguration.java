package demojavafx3d1;

import java.util.Collections;
import java.util.List;

class ScaleConfiguration {
    private final double cameraDistance = -40;
    private final double fieldOfView = 35;

    private List<point> pointsList = null;
    private double maxAbsCoor = 0;
    private double scaleFactor = 0;
    private double radius = 0;
    private double[] centerOfMass = new double[3];
    private double[] movedCenterOfMass = new double[3];

    public ScaleConfiguration(List<point> pointsList, double maxAbsCoor)
    {
        this.pointsList = pointsList;
        Collections.sort(this.pointsList);

        this.maxAbsCoor = maxAbsCoor;
        this.scaleFactor = calculateScaleFactor();
        this.radius = (calculateMinDis(0, pointsList.size() - 1) / 2) * scaleFactor;

        this.centerOfMass = calculateCenterOfMass();
        this.movedCenterOfMass = centerOfMass;
    }

    public double getScaleFactor()
    {
        return this.scaleFactor;
    }

    public double[] getCenterOfMass()
    {
        return this.movedCenterOfMass;
    }

    public void moveCenterTo(double newX, double newY, double newZ)
    {
        movedCenterOfMass[0] = newX;
        movedCenterOfMass[1] = newY;
        movedCenterOfMass[2] = newZ;
    }

    public double[] getOriginalCenter()
    {
        return centerOfMass;
    }

    public double[] calculateCenterOfMass()
    {
        double sumX, sumY, sumZ;
        double[] center = new double[3];
        sumX = sumY = sumZ = 0;

        for (point p : pointsList)
        {
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();
        }

        double size = (double)pointsList.size();
        center[0] = scaleFactor * sumX / size;
        center[1] = scaleFactor * sumY / size;
        center[2] = scaleFactor * sumZ / size;

        return center;
    }

    private double calculateScaleFactor()
    {
        double max = 0.0;
        for (int i = 0; i < pointsList.size(); i ++)
        {
            max = Math.max(max, Math.abs(pointsList.get(i).getX()));
            max = Math.max(max, Math.abs(pointsList.get(i).getY()));
            max = Math.max(max, Math.abs(pointsList.get(i).getZ()));
        }

        return maxAbsCoor / max;
    }

    public double getRadius()
    {
        return this.radius;
    }

    private double calculateMinDis(int start, int end)
    {
        if (start >= end)
            return 0;
        else
        {
            int middle = (start + end) / 2;
            double d1 = calculateMinDis(start, middle);
            double d2 = calculateMinDis(middle + 1, end);
            double d3 = Double.MAX_VALUE;

            double end_op = (end - middle - 1 > 6) ? middle + 7 : end;

            for (int i = (middle - start > 6) ? middle - 6 : start; i <= middle; i ++)
                for (int j = middle + 1; j <= end_op; j ++)
                    if (pointsList.get(i).getX() - pointsList.get(j).getX() <= d3)
                    {
                        double dis = pointsList.get(i).disTo(pointsList.get(j));
                        if (dis > 0)
                            d3 = Math.min(d3, dis);
                    }

            double minD = Double.MAX_VALUE;
            if (d1 > 0)
                minD = Math.min(minD, d1);
            if (d2 > 0)
                minD = Math.min(minD, d2);
            if (d3 > 0)
                minD = Math.min(minD, d3);

            return minD;
        }
    }

    public double getCameraDistance(){
        return this.cameraDistance;
    }

    public double getFieldOfView(){
        return this.fieldOfView;
    }

    
}
