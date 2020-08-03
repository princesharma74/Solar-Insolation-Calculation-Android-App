package com.example.solarinsolationsih;

import android.os.Build;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class SunPosition {

    // A date has day 'd', month 'm' and year 'y'
    private static LocalDate ld = null;
    public static float updateLST;
    private static LocalTime lt = null;

    public static LocalTime getLt() {
        return lt;
    }

    public static void setLt(LocalTime lt) {
        SunPosition.lt = lt;
    }

    private static double HRA = 0.0;
    private static double decANGLE = 0.0;

    private static float longitude = (float) 0.0;
    private static float latitude = (float) 0.0;

    private static float B = (float) 0.0;

    public static LocalDate getLd() {
        return ld;
    }

    public static void setLd(LocalDate ld) {
        SunPosition.ld = ld;
    }

    public static double getHRA() {
        return HRA;
    }

    public static void setHRA(double hRA) {
        HRA = hRA;
    }

    public static double getDecANGLE() {
        return decANGLE;
    }

    public static void setDecANGLE(double decANGLE) {
        SunPosition.decANGLE = decANGLE;
    }

    public static float getLongitude() {
        return longitude;
    }

    public static void setLongitude(float longitude) {
        SunPosition.longitude = longitude;
    }

    public static float getLatitude() {
        return latitude;
    }

    public static void setLatitude(float latitude) {
        SunPosition.latitude = latitude;
    }

    public static float getB() {
        return B;
    }

    public static void setB(float b) {
        B = b;
    }


    public float AZIMUTH(java.util.Date date, float log, float lat) {
        LocalDate ld1 = null;
        LocalTime lt1 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ld1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            lt1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }

        setLd(ld1);
        setLt(lt1);
        setLongitude(log);
        setLatitude(lat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setB((float) Math.toRadians((360 / 365.0) * (ld1.getDayOfYear() - 81))); // CALCULATION OF B IS ABSOLUTELY FINE
        }
        System.out.println("Value of B comes out to be: " + B);

        setHRA(Math.toRadians(HRA_IN_DEGREES()));
        setDecANGLE(Math.toRadians(Declination_in_degrees()));

        return AZIMUTH_IN_DEGREES();

    }

    public float ELEVATION(java.util.Date date, float log, float lat) {
        LocalDate ld1 = null;
        LocalTime lt1 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ld1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            lt1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }


        setLd(ld1);
        setLt(lt1);
        setLongitude(log);
        setLatitude(lat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setB((float) Math.toRadians((360 / 365.0) * (ld1.getDayOfYear() - 81))); // CALCULATION OF B IS ABSOLUTELY FINE
        }

        setHRA(Math.toRadians(HRA_IN_DEGREES()));
        setDecANGLE(Math.toRadians(Declination_in_degrees()));
        return ELEVATION_IN_DEGREES();

    }

    public static float AZIMUTH_IN_DEGREES() {
        float unsigned_azimuth = (float)

                Math.toDegrees(Math.acos((Math.sin(decANGLE) * Math.cos(Math.toRadians(latitude))
                        - Math.cos(decANGLE) * Math.sin(Math.toRadians(latitude)) * Math.cos(HRA))
                        / Math.cos(Math.toRadians(ELEVATION_IN_DEGREES()))));
        if (LST() <= 12) {
            return (float) unsigned_azimuth;
        } else {
            return (float) (360 - unsigned_azimuth);
        }

    }

    public static float ELEVATION_IN_DEGREES() {
        return (float)

                Math.toDegrees(Math.asin((Math.sin(decANGLE) * Math.sin(Math.toRadians(latitude))
                        + Math.cos(decANGLE) * Math.cos(Math.toRadians(latitude)) * Math.cos(HRA))));
    }

    public static float HRA_IN_DEGREES() {
        float LST = LST();
        return 15 * (LST - 12);

    }

    // LOCAL SOLAR TIME IS RETURNED IN HOURS
    // CONFIRMED WORKING FINE
    public static float LST() {

        float LSTM = (float) (5.5 * 15);
        float LT = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LT = (float) (lt.getHour() + (lt.getMinute() / 60.00) + (lt.getSecond() / 3600.00));
        }
        float TC = (float) (4 * (longitude - LSTM) + EoT()); // TIME CORRECTION
        updateLST = (LT + TC/60);
        return (LT + TC / 60);
    }

    // DECLINATION ANGLE IN DEGREES
    // WORKING ABSOLUTELY FINE CONFIRMED
    public static float Declination_in_degrees() {
        System.out.println("B ki value yahan par " + B + " hai");
        System.out.println("Declination (in Deg.): " + (float) ((float) 23.45 * Math.sin(B)));
        return (float) ((float) 23.45 * Math.sin(B));
    }

    // EoT working absolutely fine

    public static float EoT() {
        System.out.println("Equation of Time, EoT (in mins): "
                + (float) (9.87 * Math.sin(2 * B) - 7.53 * Math.cos(B) - 1.5 * Math.sin(B)));
        return (float) (9.87 * Math.sin(2 * B) - 7.53 * Math.cos(B) - 1.5 * Math.sin(B));

    }

}
