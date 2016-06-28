package com.example.javier.signal_man_v2;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Set;



public class MainActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {

    int bits=0;
    ListView lv;
    private Set<BluetoothDevice> pairedDevices;
    private static String address = "00:13:12:23:56:18";
    private BluetoothAdapter BA = null;

    private LineChart mChart;
    int displayValue = 200;
    public int contDisplay;
    int refresh=1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv = (ListView)findViewById(R.id.listView);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                           expand(extract_addr(adapterView.getItemAtPosition(i).toString()));
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        BA = BluetoothAdapter.getDefaultAdapter();

        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }







        //char animation
        mChart = (LineChart) findViewById(R.id.chart2);

        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(ColorTemplate.rgb("#bfbfbf"));
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        data.setDrawValues(false);

        // add empty data
        mChart.setData(data);
        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(20);
        xl.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(ColorTemplate.rgb("bfbfbf"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMaxValue(200f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        data = mChart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);

        set = createSet();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addXValue("");
            data.addEntry(new Entry(  50f, set.getEntryCount()), 0);}

        set = data.getDataSetByIndex(1);
        set = createSet2();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  100f, set.getEntryCount()), 1);}



        //feedMultiple();





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }



    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAdd: {
                addEntry();
                break;
            }
            case R.id.actionClear: {
                mChart.clearValues();
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                feedMultiple();
                break;
            }
            case R.id.actionOpenSettings:
            {
                Toast.makeText(this, "en", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }





    public void list(View v){
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.custom, list);
        lv.setAdapter(adapter);
    }

    String extract_addr(String Name){
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        String rVal = "";

        for(BluetoothDevice bt : pairedDevices)
        {
            if( bt.getName().contains(Name)) { rVal = bt.getAddress(); break;}
        }

        return rVal;

    }



    private int mResult;
    public int getYesNoWithExecutionStop(String title, String message, Context context) {
        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setIcon(R.drawable.head_qs);

        alert.setPositiveButton("16 Bits", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult = 22;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.setNegativeButton("24 Bits", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult = 28;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.show();


        // loop till a runtime exception is triggered.
        try { Looper.loop(); }
        catch(RuntimeException e2) {}

        return mResult;
    }




    public void expand(String address) {

        bits= getYesNoWithExecutionStop("Bits of Operation", "Please, choose a number to continue...", this);


        Intent i = new Intent(this, RealtimeLineChartActivity.class);
        i.putExtra("address", address);
        i.putExtra("bits", Integer.toString(bits));
        startActivity(i);



    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#FFFFD1D1"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(4f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#FFFFD1D1"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet2() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#FFC4F9EA"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(4f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#FFC4F9EA"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    private void feedMultiple() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for(int i = 0; i < 500; i++) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    try {
                        Thread.sleep(35);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void addEntry() {

        LineData data = mChart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);


        // add a new x-value first
        data.addXValue("" + (set.getEntryCount() - displayValue));
        data.addEntry(new Entry((float) (Math.random() * 40) + 50f, set.getEntryCount()), 0);

        set = data.getDataSetByIndex(1);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 100f, set.getEntryCount()), 1);
/*
        set = data.getDataSetByIndex(2);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 150f, set.getEntryCount()), 2);

        set = data.getDataSetByIndex(3);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 210f, set.getEntryCount()), 3);

        /*
        set = data.getDataSetByIndex(4);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 270f, set.getEntryCount()), 4);

        set = data.getDataSetByIndex(5);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 330f, set.getEntryCount()), 5);

        set = data.getDataSetByIndex(6);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 390f, set.getEntryCount()), 6);

        set = data.getDataSetByIndex(7);

        data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 450f, set.getEntryCount()), 7);

/*
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getXValCount() - 121);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
/*
*/

        contDisplay++;


        if (contDisplay==refresh) {

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(displayValue);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry

            mChart.moveViewToX(data.getXValCount() - displayValue + 1);
            contDisplay=0;
        }
        /*
        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        // limit the number of visible entries
        mChart.setVisibleXRangeMaximum(displayValue);
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        mChart.moveViewToX(data.getXValCount() - (displayValue + 1));

        // this automatically refreshes the chart (calls invalidate())
        // mChart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);*/
    }




}
