package hpsaturn.pollutionreporter.view;

import android.content.Context;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.hpsaturn.tools.Logger;

import java.util.ArrayList;
import java.util.List;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/20/21.
 */
public class ChartVar {

    public static String TAG = ChartVar.class.getSimpleName();

    private Context ctx;

    public String type;

    public List<Entry> entries = new ArrayList<>();

    public LineDataSet dataSet;

    public ArrayList<Integer> colors = new ArrayList<>();

    public ChartVar(Context ctx, String type,String label) {

        this.ctx = ctx;

        this.type = type;

        switch (type) {

            case "P25":
                dataSet = getMainLineDataSet(entries, R.color.grey, label,1.5F,5.0F);
                break;

            case "CO2":
                dataSet = getMainLineDataSet(entries, R.color.black, label,1.5F,3.0F);
                break;

            case "PAX":
                dataSet = getMainLineDataSet(entries, R.color.light_red, label,2.0F,3.0F);
                break;

            case "P1":
                dataSet = getGenericLineDataSet(entries, R.color.brown, label,1F);
                break;

            case "P4":
                dataSet = getGenericLineDataSet(entries, R.color.colorAccent, label,1F);
                break;

            case "P10":
                dataSet = getGenericLineDataSet(entries, R.color.colorAccentWeb, label,2F);
                break;

            case "CO2T":
                dataSet = getGenericLineDataSet(entries, R.color.light_red, label,1F);
                break;

            case "CO2H":
                dataSet = getGenericLineDataSet(entries, R.color.light_blue, label,1F);
                break;

            case "tmp":
                dataSet = getGenericLineDataSet(entries, R.color.red, label,1F);
                break;

            case "hum":
                dataSet = getGenericLineDataSet(entries, R.color.blue, label,2F);
                break;

            case "alt":
                dataSet = getGenericLineDataSet(entries, R.color.purple, label,1F);
                break;

            case "pre":
                dataSet = getGenericLineDataSet(entries, R.color.yellow, label,1F);
                break;

            case "co":
                dataSet = getGenericLineDataSet(entries, R.color.blue, label,1F);
                break;

            case "nh3":
                dataSet = getGenericLineDataSet(entries, R.color.black, label,1F);
                break;


            default:
                    dataSet = getGenericLineDataSet(entries, R.color.yellow, label,1F);
                    Logger.e(TAG,"Bad variable type compilation");
                    break;

        }

    }

    private LineDataSet getMainLineDataSet(List<Entry> entry, int color, String label, float width, float radious) {

        LineDataSet dataSet = new LineDataSet(entry,label);
        dataSet.setColor(ctx.getResources().getColor(color));
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setCircleRadius(radious);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setLineWidth(width);

        return dataSet;
    }

    private LineDataSet getGenericLineDataSet(List<Entry> entry, int color, String label,float width) {

        LineDataSet dataSet = new LineDataSet(entry,label);
        dataSet.setColor(ctx.getResources().getColor(color));
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setLineWidth(width);

        return dataSet;
    }


    public void addValue(float time,SensorData data){
        switch (type) {
            case "CO2":
                if (data.CO2 <= 600) colors.add(ctx.getResources().getColor(R.color.green));
                else if (data.CO2 <= 800)  colors.add(ctx.getResources().getColor(R.color.yellow_dark));
                else if (data.CO2 <= 1000) colors.add(ctx.getResources().getColor(R.color.orange));
                else if (data.CO2 <= 1500) colors.add(ctx.getResources().getColor(R.color.red));
                else if (data.CO2 <= 2000) colors.add(ctx.getResources().getColor(R.color.purple));
                else colors.add(ctx.getResources().getColor(R.color.brown));
                break;
            case "P25":
                if (data.P25 <= 13) colors.add(ctx.getResources().getColor(R.color.green));
                else if (data.P25 <= 35) colors.add(ctx.getResources().getColor(R.color.yellow_dark));
                else if (data.P25 <= 55) colors.add(ctx.getResources().getColor(R.color.orange));
                else if (data.P25 <= 150) colors.add(ctx.getResources().getColor(R.color.red));
                else if (data.P25 <= 250) colors.add(ctx.getResources().getColor(R.color.purple));
                else colors.add(ctx.getResources().getColor(R.color.brown));
                break;
            case "PAX":
                if (data.PAX <= 20) colors.add(ctx.getResources().getColor(R.color.green));
                else if (data.PAX <= 50) colors.add(ctx.getResources().getColor(R.color.yellow_dark));
                else if (data.PAX <= 100) colors.add(ctx.getResources().getColor(R.color.orange));
                else if (data.PAX <= 400) colors.add(ctx.getResources().getColor(R.color.red));
                else if (data.PAX <= 800) colors.add(ctx.getResources().getColor(R.color.purple));
                else colors.add(ctx.getResources().getColor(R.color.brown));
                break;
        }

        try {
            float value = data.getClass().getField(type).getFloat(data);
            dataSet.addEntry(new Entry(time, value));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        
    }

    public void refresh(){
        switch (type){
            case "CO2":
            case "P25":
            case "PAX":
                dataSet.setCircleColors(colors);
                break;

        }
    }

    public void clear() {
        dataSet.clear();
        colors.clear();
        entries.clear();
    }
}
