package com.alvin.health.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by alvin on 2015/11/24.
 */
public class MyChart {
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context mContext;
    private int addX = 0, addY;

    int[] xv = new int[30];
    int[] yv = new int[30];
    private final String mTitle = "heart beat";

    public MyChart(Context context) {
        mContext = context;
        series = new XYSeries(mTitle);
        mDataset = new XYMultipleSeriesDataset();
        mDataset.addSeries(series);
        int color = Color.rgb(255, 0, 0);
        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color, style, true);
        setChartSettings(renderer, "X", "Y", 0, 60, 50, 160);
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYSeriesRenderer r = new XYSeriesRenderer();
        //        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(4);
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle
            , double xMin, double xMax, double yMin, double yMax) {
        //        renderer.setChartTitle(mTitle);
        //        renderer.setXTitle(xTitle);
        //        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(Color.WHITE);
        renderer.setLabelsColor(Color.argb(100, 80, 80, 80));
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.argb(100, 80, 80, 80));
        renderer.setXLabels(30);
        renderer.setYLabels(20);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setPointSize(4);
        renderer.setShowLegend(false);
    }

    public View getChartView() {
        return chart;
    }

    public void updateChart(int value) {

        //设置好下一个需要增加的节点
        addX = 0;
        addY = value;
        //移除数据集中旧的点集
        mDataset.removeSeries(series);
        //判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
        int length = series.getItemCount();
        if (length > 30) {
            length = 30;
        }
        //将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i) + 1;
            yv[i] = (int) series.getY(i);
        }
        //点集先清空，为了做成新的点集而准备
        series.clear();

        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        //这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        series.add(addX + 2, addY);
        for (int k = 0; k < length; k++) {
            series.add(xv[k]+2, yv[k]);
        }
        //在数据集中添加新的点集
        mDataset.addSeries(series);

        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart.invalidate();
    }
}
