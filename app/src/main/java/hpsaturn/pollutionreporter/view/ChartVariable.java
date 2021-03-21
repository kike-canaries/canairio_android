package hpsaturn.pollutionreporter.view;

import android.content.Context;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/20/21.
 */
public abstract class ChartVariable  {

    private Context ctx;

    public List<Entry> entries = new ArrayList<Entry>();

    public LineDataSet dataSet;

    public ArrayList<Integer> colors = new ArrayList<Integer>();

    public ChartVariable(Context ctx, String label, int color, float width, boolean isMainValue) {
        this.ctx = ctx;
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
        dataSet.setHighlightEnabled(true);
        dataSet.setCircleRadius(4.0f);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setLineWidth(width);

        return dataSet;
    }

    public abstract void addValue(float time,SensorData data);

    public void refresh(){
        dataSet.setCircleColors(colors);
    }

    public void clear() {
        dataSet.clear();
        colors.clear();
        entries.clear();
    }
}
