package hpsaturn.pollutionreporter.view;

import android.content.Context;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

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

    private final String type;

    public List<Entry> entries = new ArrayList<Entry>();

    public LineDataSet dataSet;

    public ArrayList<Integer> colors = new ArrayList<Integer>();

    public ChartVar(Context ctx, String type, String label, int color, float width, boolean isMainValue) {
        this.ctx = ctx;
        this.type = type;
        if (isMainValue) dataSet = getMainLineDataSet(entries,color,label,width);
        else dataSet = getGenericLineDataSet(entries,color,label,width);
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

    private LineDataSet getMainLineDataSet(List<Entry> entry, int color, String label,float width) {

        LineDataSet dataSet = new LineDataSet(entry,label);
        dataSet.setColor(ctx.getResources().getColor(color));
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setCircleRadius(4.0f);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setLineWidth(width);

        return dataSet;
    }

    public void addValue(float time,SensorData data){
        switch (type) {
            case "CO2":
                if (data.CO2 <= 600) colors.add(ctx.getResources().getColor(R.color.green));
                else if (data.CO2 <= 800)  colors.add(ctx.getResources().getColor(R.color.yellow));
                else if (data.CO2 <= 1000) colors.add(ctx.getResources().getColor(R.color.orange));
                else if (data.CO2 <= 1500) colors.add(ctx.getResources().getColor(R.color.red));
                else if (data.CO2 <= 2000) colors.add(ctx.getResources().getColor(R.color.purple));
                else colors.add(ctx.getResources().getColor(R.color.brown));
                break;
            case "P25":
                if (data.P25 <= 13) colors.add(ctx.getResources().getColor(R.color.green));
                else if (data.P25 <= 35) colors.add(ctx.getResources().getColor(R.color.yellow));
                else if (data.P25 <= 55) colors.add(ctx.getResources().getColor(R.color.orange));
                else if (data.P25 <= 150)colors.add(ctx.getResources().getColor(R.color.red));
                else if (data.P25 <= 250)colors.add(ctx.getResources().getColor(R.color.purple));
                else colors.add(ctx.getResources().getColor(R.color.brown));
                break;
        }

        try {
//            Logger.e(TAG,"ChartVal:"+type);
            dataSet.addEntry(new Entry(time,data.getClass().getField(type).getFloat(data)));

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }


    }

    public void refresh(){
        switch (type){
            case "CO2":

            case "P25":
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
