package com.example.imu;

import java.util.Arrays;

public class GyroProcessor {

    private double[] qInt = {1, 0, 0, 0};  // Initial quaternion (identity quaternion)
    private static final double DEG_TO_RAD = Math.PI / 180;
    private static final double RAD_TO_DEG = 180 / Math.PI;

    public double[] quaternionMultiply(double[] quaternion1, double[] quaternion0) {
        double w0 = quaternion0[0], x0 = quaternion0[1], y0 = quaternion0[2], z0 = quaternion0[3];
        double w1 = quaternion1[0], x1 = quaternion1[1], y1 = quaternion1[2], z1 = quaternion1[3];

        return new double[]{
                -x1 * x0 - y1 * y0 - z1 * z0 + w1 * w0,
                x1 * w0 + y1 * z0 - z1 * y0 + w1 * x0,
                -x1 * z0 + y1 * w0 + z1 * x0 + w1 * y0,
                x1 * y0 - y1 * x0 + z1 * w0 + w1 * z0
        };
    }
    public void setupQInt(){
        qInt = new double[]{1, 0, 0, 0};  // Initial quaternion (identity quaternion)
    }

    public float rom(short[] data, double[] offset) {
        double[] doubleData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            doubleData[i] = (double) data[i];
        }

        double[] gyro = Arrays.copyOfRange(doubleData, 0, 3);
        double delT = doubleData[3] / 1000000.0;  // Convert microseconds to seconds

        for (int i = 0; i < gyro.length; i++) {
            gyro[i] = (gyro[i]  - offset[i])/65.5;
            gyro[i] = gyro[i] * DEG_TO_RAD;  // Convert to radians
        }

        double norm = Math.sqrt(gyro[0] * gyro[0] + gyro[1] * gyro[1] + gyro[2] * gyro[2]);
        double[] aor;
        if (norm != 0) {
            aor = new double[]{gyro[0] / norm, gyro[1] / norm, gyro[2] / norm};
        } else {
            aor = new double[]{gyro[0], gyro[1], gyro[2]};
        }

        double deltaTheta = norm * delT;
        double tempVar = Math.sin(deltaTheta / 2);
        double[] qGyro = {
                Math.cos(deltaTheta / 2),
                aor[0] * tempVar,
                aor[1] * tempVar,
                aor[2] * tempVar
        };

        // Multiply quaternions
        qInt = quaternionMultiply(qGyro, qInt);

        // Calculate the angle


        return (float) (2 * RAD_TO_DEG * Math.acos(qInt[0]));
    }
}
