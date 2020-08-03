package com.example.solarinsolationsih;

import java.util.ArrayList;

public class Line_Coordinates_Tilted_At_an_Angle {


//	static float theta = (float) -45.0;
//	static int X = 12;
//	static int Y = 12;

    //Equation y = mod(x.cot(theta) - 0.5(Y + cot(theta)*X))
    //ROUND OFF Y TO Integer

//	public static void main(String[] args) {
//
//			ArrayList<String> received = new ArrayList<String>();
//			float theta = (float) -45.0;
//			received = passingCoordinates(12, 12, theta);
//			for (String i : received) {
//				System.out.println(i);
//			}
//
//	}

    public ArrayList<String> passingCoordinates(int X, int Y, float theta){
        ArrayList<String> coordinates = new ArrayList<String>();
        // TODO Auto-generated method stub
        int y = 0;
        for (int i = 0; i<X; i++) {

            if(theta == 0) {
                for (int j = 0; j<Y; j++) {
                    //System.out.println("("+X/2+", "+j+")");
                    coordinates.add(X/2+", "+j);
                }
                break;
            }

            y = (int) (i*COT(theta) - 0.5*(Y + COT(theta)*X));
            //System.out.println("y: "+y);
            if (y<=0) {
                y = (-1)*y;
            }
            else {
                continue;
            }
            if (y < Y) {
                //System.out.println("("+i+", "+y+")");
                coordinates.add(i+", "+y);
            }
        }
        return coordinates;
    }

    public static float COT(float f) {
        f = (float) Math.toRadians(f);
        return Float.parseFloat(String.format("%.2f", (1/Math.tan(f))));

    }

    public String whichQuadrant(int i, int j) {
        if(i>=0 && j>=0) {
            return "I";
        }
        if(i <= 0 && j >= 0) {
            return "II";
        }
        if(i <= 0 && j <= 0) {
            return "III";
        }
        if(i>=0 && j<=0) {
            return "IV";
        }
        return null;
    }

    public float distance(int X1, int Y1, int X2, int Y2) {
        return (float) Math.sqrt(Math.pow((X2-X1), 2)+Math.pow((Y2-Y1), 2));
    }


}
