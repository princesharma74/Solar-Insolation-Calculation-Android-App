package com.example.solarinsolationsih;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.threeten.extra.chrono.JulianDate;

import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SolarPath extends AppCompatActivity {

    LineChart lineChart1, chart2;
    float latitude, longitude;
    ArrayList<Entry> juneSunPath = new ArrayList<>();
    ArrayList<Entry> decSunPath = new ArrayList<>();
    ArrayList<Entry> marchSunPath = new ArrayList<>();

    private String[] Months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private int[] Colors = new int[]{Color.RED, Color.GREEN, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.YELLOW,
            Color.DKGRAY, Color.WHITE, Color.BLUE, Color.BLACK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar_path);

        lineChart1 = findViewById(R.id.lineChart1);
        TextView textLocation = findViewById(R.id.location);
        chart2 = findViewById(R.id.chart2);
        Intent intent = getIntent();
        latitude = intent.getFloatExtra("latitude", 0);
        longitude = intent.getFloatExtra("longitude", 0);
        textLocation.setText("Latitude: " + String.valueOf(latitude) + ", Longitude: " + String.valueOf(longitude));

        drawXYSUNGraph();
        drawDOMESHAPEDGraph();
    }

    private void drawDOMESHAPEDGraph(){

        ArrayList<Entry> entry1 = new ArrayList<>();
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        for (float i = -5; i<=5; i+=0.1){
            float x = i;
            float y = (float)(Math.sqrt(25f-Math.pow(i, 2)));
            entry1.add(new Entry(i, y));
        }
        LineDataSet lineDataSet1 = new LineDataSet(entry1, "Chart1");
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setColor(Color.BLACK);
        sets.add(lineDataSet1);
        ArrayList<Entry> entry2 = new ArrayList<>();
        for(float j = -5.0f; j<=5.0; j+=0.1 ){
            float x = j;
            float y = (float)(Math.sqrt(25f-Math.pow(j, 2)));
            entry2.add(new Entry(j, (-1)*y));
        }
        LineDataSet lineDataSet2 = new LineDataSet(entry2, "Chart2");
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setColor(Color.BLACK);
        sets.add(lineDataSet2);

        LineDataSet lineDataSet3 = new LineDataSet(marchSunPath, "Mar");
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setColor(Color.RED);
        sets.add(lineDataSet3);
        LineDataSet lineDataSet4 = new LineDataSet(juneSunPath, "Jun");
        lineDataSet3.setColor(Color.BLUE);
        sets.add(lineDataSet4);
        lineDataSet4.setDrawCircles(false);
        LineDataSet lineDataSet5 = new LineDataSet(decSunPath, "Dec");
        lineDataSet5.setDrawCircles(false);
        lineDataSet5.setColor(Color.GREEN);
        sets.add(lineDataSet5);


        chart2.setPinchZoom(false);
        chart2.setScaleEnabled(false);
        chart2.animateXY(800, 800);

        chart2.setDrawGridBackground(false);
        chart2.getXAxis().setDrawGridLines(false);
        chart2.setDrawBorders(true);
        chart2.getAxisLeft().setDrawGridLines(false);
        chart2.getAxisRight().setDrawGridLines(false);
        chart2.getXAxis().setAxisMinimum(-7);
        chart2.getXAxis().setAxisMaximum(7);
        YAxis yAxis = chart2.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinimum(-7);
        yAxis.setAxisMaximum(7);
        chart2.setData(new LineData(sets));
        chart2.invalidate();


    }

    private void drawXYSUNGraph() {

        Calendar calendar = Calendar.getInstance();
        JulianDate jd = JulianDate.now();
        ChronoLocalDateTime<JulianDate> jd_s = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            jd_s = jd.atTime(LocalTime.now());
        }
        String[] s = jd_s.toString().split("[-|T|:| ]");

        int YEAR = Integer.parseInt(s[2]);
        int DATE = Integer.parseInt(s[4]);
        double MINUTE = 00;
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<ArrayList<Entry>> entries = new ArrayList<ArrayList<Entry>>(12);

        for (int MONTH = 0; MONTH < 12; MONTH++) {
            ArrayList<Entry> entry = new ArrayList<>();
            for (double HOUR = 6; HOUR < 20; HOUR = HOUR + 0.20) {

                MINUTE = (HOUR - (int) HOUR) * 60;
                calendar.set(YEAR, MONTH, DATE, (int) HOUR, (int) MINUTE);
                Date date = calendar.getTime();

                SunPosition p1 = new SunPosition();

                float az = (float) (p1.AZIMUTH(date, longitude, latitude));
                float ele = (float) (p1.ELEVATION(date, longitude, latitude));
                if (ele >= 0)
                    entry.add(new Entry(az, ele));

                float[] f = new float[2];
                f = StereographicValues(az, ele, 5); //kept the circle of radius 5

                switch(MONTH) {
                    case 5 : //FOR JUNE
                        if(HOUR>=12.5) juneSunPath.add(new Entry(f[0], f[1]));//we need to replace this with local solar time
                        else
                            f[0] = (-1)*f[0];
                        juneSunPath.add(new Entry(f[0], f[1]));
                        //System.out.println("Excellent!");
                        break;
                    case 11 :
                        if(HOUR>=12.5) decSunPath.add(new Entry(f[0], f[1]));
                        else
                            f[0] = (-1)*f[0];
                        decSunPath.add(new Entry(f[0], f[1]));
                        break;
                    case 2 :
                        if(HOUR>=12.5) marchSunPath.add(new Entry(f[0], f[1]));
                        else
                            f[0] = (-1)*f[0];
                        marchSunPath.add(new Entry(f[0], f[1]));
                        break;
                }


            }

            entries.add(entry);
            LineDataSet set = new LineDataSet(entries.get(MONTH), "Month" + (MONTH + 1));
            set.setColor(Colors[MONTH]);
            set.setDrawCircles(false);
            dataSets.add(set);

        }

        Legend legend = lineChart1.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(10);
        legend.setXEntrySpace(12);
        legend.setFormToTextSpace(8);

        LegendEntry[] legendEntry = new LegendEntry[12];

        for (int i = 0; i < 12; i++) {
            LegendEntry entry = new LegendEntry();
            entry.formColor = Colors[i];
            entry.label = Months[i];
            legendEntry[i] = entry;
        }

        legend.setCustom(legendEntry);

        Description description = lineChart1.getDescription();
        description.setText("Sun Path");
        description.setTextColor(Color.BLUE);
        description.setTextSize(12);
        description.setXOffset(5);
        description.setYOffset(5);
        description.setTypeface(Typeface.SANS_SERIF);

        lineChart1.getLegend().setWordWrapEnabled(true);
        lineChart1.setDescription(description);
        lineChart1.setDrawBorders(true);
        lineChart1.setData(new LineData(dataSets));
        lineChart1.invalidate();

    }


    public static float[] StereographicValues(float az, float ele, float R) {
        float[] fl = new float[2];
        float r = (float) (R*((90-ele)/90.0));
        fl[0] = (float) (r*(Math.sqrt(1-Math.pow(Math.cos(Math.toRadians(az)), 2))));
        fl[1] = (float)(r*Math.cos(Math.toRadians(az)));
        return fl;
    }
}