package com.example.imu;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;



public class XAxisValueFormatter extends ValueFormatter  {
    private List<String> labels;

    public XAxisValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;
        if (index >= 0 && index < labels.size()) {
            return labels.get(index);
        } else {
            return "";
        }
    }
}
