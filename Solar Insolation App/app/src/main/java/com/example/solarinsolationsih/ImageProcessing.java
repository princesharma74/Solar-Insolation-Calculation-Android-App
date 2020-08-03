package com.example.solarinsolationsih;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageProcessing {        //	Camera.Parameters p = camera.getParameters();
        Camera cameraX = Camera.open();
        double thetaH = cameraX.getParameters().getHorizontalViewAngle(); //	double thetaV = Math.toRadians(p.getVerticalViewAngle())
        double thetaV = cameraX.getParameters().getVerticalViewAngle();  //	double thetaH = Math.toRadians(p.getHorizontalViewAngle());

        static double angle_covered_by_one_pixel = 65.0 / 720; //need to calculate.

        static float rotationAngle = 0; //initialized the rotation angle value

    public ArrayList<ArrayList<Float>> getRefined_spatial_angles() {
        return refined_spatial_angles;
    }

    private ArrayList<ArrayList<Float>> refined_spatial_angles = new ArrayList<ArrayList<Float>>();
        private ArrayList<ArrayList<Float>> spatial_angles = new ArrayList<ArrayList<Float>>();
        static float AoE = 0;

    public void setSpatial_angles(ArrayList<ArrayList<Float>> spatial_angles) {
        this.spatial_angles = spatial_angles;
    }

    static float result_AoE = 0;
        static float Azimuth = 0;
        static float least_count_azimuth = 0;

        ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

        public void setImageBitmaps(ArrayList<Bitmap> bitmaps) {
            imageBitmaps.addAll(bitmaps);
        }



        public void REFINE_SPATIAL_ANGLES() throws IOException {

//        ImageProcessing.spatial_angles = spatial_angles
            for (int i = 0; i < 5; i++) {
                if(i == 0)
                    spatial_angles.add(new ArrayList<Float>(Arrays.asList(30.0f, 7.9f, 30.0f)));
                if(i == 1)
                    spatial_angles.add(new ArrayList<Float>(Arrays.asList(60.0f, 12.9f, -9.0f)));
                if(i == 2)
                    spatial_angles.add(new ArrayList<Float>(Arrays.asList(30.0f, 45f, -15.0f)));
                if(i == 3)
                    spatial_angles.add(new ArrayList<Float>(Arrays.asList(80.0f, 7.9f, 30.0f)));
                if(i == 4)
                    spatial_angles.add(new ArrayList<Float>(Arrays.asList(130.0f, 20f, 30.0f)));
            }
            // TODO Auto-generated method stub

            //TAKE ALL THE IMAGES IN THE FILES ARRAY.

            least_count_azimuth = (float) (360.0 / 5);

            // FileWriter fw = new FileWriter("C:/Users/princ/Desktop/obstacle_AoE.csv", true);
            //BufferedWriter bw = new BufferedWriter(fw);

//        @SuppressWarnings("resource")
//        PrintWriter pw = new PrintWriter(bw);
//        pw.println("Azimuth, Rotation Angle, AoE_LowestSkyPixel");
//        //READING CSV FILE USING CSVPARSER. [APACHE COMMONS CSV LIBRARY]


            //STARTED ITETERATING THROUGH EACH FILE.
            for (int f = 0; f < imageBitmaps.size(); f++) {
                ArrayList<Float> values = new ArrayList<>();
//            File rd = files[f];

                //rotationAngle = getRandomIntegerBetweenRange(-89, 89);
                //System.out.println("ROTATED ANGLE: "+rotationAngle);

                if (Azimuth + least_count_azimuth <= 360) {
                    Azimuth = Azimuth + least_count_azimuth;
                    values.add(Azimuth);
                    //      pw.print(Azimuth + ",");
                } else {
                    //    pw.print(Azimuth + ",");
                    values.add(Azimuth);
                }

                rotationAngle = spatial_angles.get(f).get(2);
                //rotationAngle = Float.parseFloat(String.format("%.2f", Float.parseFloat((csvrecords.get(f).get(2)))));//GET(2) BECAUSE THIRD COLUMN IS ROTATION ANGLE.
                values.add(rotationAngle);
                //pw.print(rotationAngle + ",");


                //AoE = getRandomIntegerBetweenRange(0, 80);
                //System.out.println("ELEVATED ANGLE: "+AoE);
                AoE = spatial_angles.get(f).get(1);
                //AoE = Float.parseFloat(String.format("%.2f", Float.parseFloat((csvrecords.get(f).get(1)))));//GET(1) BECAUSE THIRD COLUMN IS ANGLE OF ELEVATION.

                System.out.println("ONE PIXEL ANGLE: " + angle_covered_by_one_pixel);

                Bitmap readImage = imageBitmaps.get(f);
                //BufferedImage readImage = ImageIO.read(rd);

                ArrayList<String> received = new ArrayList<String>();
                Line_Coordinates_Tilted_At_an_Angle obj = new Line_Coordinates_Tilted_At_an_Angle();
                received = obj.passingCoordinates(readImage.getWidth(), readImage.getHeight(), rotationAngle);

                String[] prev = received.get(0).split(", ");

                boolean lineClear = true;

                for (String cd : received) {
                    String s[] = cd.split(", ");

                    int prev1 = Integer.parseInt(prev[0]);
                    int prev2 = Integer.parseInt(prev[1]);

                    int i = Integer.parseInt(s[0]);
                    int j = Integer.parseInt(s[1]);


                    if (readImage.getPixel(prev1, prev2) != readImage.getPixel(i, j)) {
//                    System.out.println("PrevRGB Value: " + readImage.getPixel(prev1, prev2) + " at (" + prev1 + ", " + prev2 + ")");
//                    System.out.println("Current RGB Value: " + readImage.getPixel(i, j) + " at (" + i + ", " + j + ")");
                        System.out.println("Interface Detected at (" + i + ", " + j + ")");

                        float dist = obj.distance(readImage.getWidth() / 2, readImage.getHeight() / 2, i, j);
                        float angle_tobe_propagated = (float) angle_covered_by_one_pixel * dist;
                        System.out.println("AtbePGTD: " + angle_tobe_propagated);
                        System.out.println("Distance from Mid: " + dist);

                        if (obj.whichQuadrant(i - (readImage.getWidth() / 2), j + (readImage.getHeight() / 2)) == "I" || obj.whichQuadrant(i - (readImage.getWidth() / 2), j + (readImage.getHeight() / 2)) == "II") {
                            result_AoE = AoE - angle_tobe_propagated;
                            if(result_AoE<0) result_AoE = 0;
                            //          pw.println(result_AoE + ",");
                            values.add(result_AoE);

                        } else {
                            result_AoE = AoE + angle_tobe_propagated;
                            if(result_AoE < 0 ) result_AoE = 0;
                            //        pw.println(result_AoE + ",");
                            values.add(result_AoE);
                        }

//                        if (result_AoE < 0) {
//                            //      pw.println("NEGATIVE DETECTED! SOMETHING WRONG HERE.");
//
//                            System.out.println("NEGATIVE DETECTED! SOMETHING WRONG HERE.");
//                        }

//                            BufferedImage rgbImage = new BufferedImage(readImage.getWidth(), readImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
//                            rgbImage = ImageIO.read(rd);
//                            Graphics2D g2d = rgbImage.createGraphics();
//                            g2d.setColor(Color.green);
//                            Ellipse2D.Double circle = new Ellipse2D.Double(i-30, j-30, 60, 60);
//                            g2d.fill(circle);
//
//                            g2d.setColor(Color.red);
//                            Ellipse2D.Double circle1 = new Ellipse2D.Double((rgbImage.getWidth()/2)-30, (rgbImage.getHeight()/2)-30, 60, 60);
//
//                            g2d.fill(circle1);
//                            g2d.dispose();
//                            try {
//                                ImageIO.write(rgbImage, "jpg", new File("C:/Users/princ/Desktop/EditedMaskedImages/"+(f+1)+"_"+"AoE_"+String.format("%.2f", result_AoE)+".png"));
//                                System.out.println("Image Written");
//                                System.out.println();
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
                        lineClear = false;
//                            System.out.println("DONE image_"+f);
//                            break;
                    }
                    prev = cd.split(", ");
                }
//
                if(lineClear) {
                    values.add((float) 80.0);
                    System.out.println("Line Was Clear!..... So skipped image_"+f);
                }


                refined_spatial_angles.add(values);
                System.out.println();
            }
            Log.d("TAG", "Refined angles: " + String.valueOf(refined_spatial_angles));


//        pw.flush();
//        pw.close();
        }


    }


