package br.sf;

import java.util.ArrayList;
import java.util.List;

public class HoughTransform {
    private int width;
    private int height;
    private int maxDistance;
    private int numAngles;
    private int[][] accumulator;

    public HoughTransform(int width, int height, int numAngles) {
        this.width = width;
        this.height = height;
        this.numAngles = numAngles;

        maxDistance = (int) Math.ceil(Math.hypot(width, height));
        accumulator = new int[numAngles][2 * maxDistance];
    }

    public void addPoint(int x, int y) {
        for (int theta = 0; theta < numAngles; theta++) {
            double rho = x * Math.cos(Math.toRadians(theta)) + y * Math.sin(Math.toRadians(theta));
            int rhoIdx = (int) Math.round(rho) + maxDistance;
            accumulator[theta][rhoIdx]++;
        }
    }

    public void process() {
        // No processing needed for Hough transform
    }

    public Line[] getLines(int threshold) {
        List<Line> lines = new ArrayList();

        for (int theta = 0; theta < numAngles; theta++) {
            for (int rho = 0; rho < 2 * maxDistance; rho++) {
                if (accumulator[theta][rho] >= threshold) {
                    double thetaRad = Math.toRadians(theta);
                    double rhoVal = rho - maxDistance;
                    lines.add(new Line(thetaRad, rhoVal));
                }
            }
        }

        return lines.toArray(new Line[0]);
    }

    public static class Line {
        private double theta;
        private double rho;

        public Line(double theta, double rho) {
            this.theta = theta;
            this.rho = rho;
        }

        public double getTheta() {
            return theta;
        }

        public double getRho() {
            return rho;
        }
    }
}
