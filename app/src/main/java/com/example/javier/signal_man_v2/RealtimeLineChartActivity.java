
package com.example.javier.signal_man_v2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class RealtimeLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {
    private LineChart mChart;
    int displayValue = 200;
    Button btnMarket;


    CheckBox CheckSave;
    CheckBox CheckShowValues;
    CheckBox CreateMarker;

    int PK_ID_int;
    int Header_int;
    int bitsExpected;

    int ch1;
    int ch2;
    int ch3;
    int ch4;


    int  cont_write;
    boolean write_open;

    int scaling = 320000 ;
    public int contDisplay;
    public int refresh = 2;
    int readBufferPosition;
    Button btnSave;
    TextView value1;
    TextView value2;
    TextView value3;
    TextView value4;
    TextView Counter;

    TextView lv1;
    TextView lv2;
    TextView lv3;
    TextView lv4;
    TextView lhe;
    TextView lid;
    TextView lPK_C;

    static int [] [] readbuffer_copy;
    private static final String TAG = "bluetooth2";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    static int[] readBuffer;

    boolean stopWorker2;
    Thread workerThread2;
    int PK_Counter_int = 0;

    OutputStream mmOutputStream;
    int downsample;
    TextView Header;
    TextView PK_ID;
    private BluetoothReadThread readThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:13:12:23:56:18";
    OutputStreamWriter osw = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_realtime_linechart);

        value1 = (TextView) findViewById(R.id.textView2);
        value2 = (TextView) findViewById(R.id.textView3);
        value3 = (TextView) findViewById(R.id.textView4);
        value4 = (TextView) findViewById(R.id.textView5);
        Header = (TextView) findViewById(R.id.textView11);
        PK_ID = (TextView) findViewById(R.id.textView13);
        Counter = (TextView) findViewById(R.id.textView15);

        lv1 = (TextView) findViewById(R.id.textView6);
        lv2 = (TextView) findViewById(R.id.textView7);
        lv3 = (TextView) findViewById(R.id.textView8);
        lv4 = (TextView) findViewById(R.id.textView9);
        lhe = (TextView) findViewById(R.id.textView10);
        lid = (TextView) findViewById(R.id.textView12);
        lPK_C = (TextView) findViewById(R.id.textView14);
        CreateMarker = (CheckBox) findViewById(R.id.checkBox3);
        btnMarket = (Button) findViewById(R.id.btnMarket);



        CheckShowValues = (CheckBox) findViewById(R.id.checkBox2);

        CheckSave = (CheckBox) findViewById(R.id.checkBox);
        btnSave = (Button) findViewById(R.id.btnSave);




        readBuffer = new int[10240];
        readbuffer_copy = new int [1024][14];


        btAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
        checkBTState();



        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString("address");
            bitsExpected = Integer.parseInt(extras.getString("bits"));
            //Toast.makeText(this, "" + bitsExpected , Toast.LENGTH_SHORT).show();

            if(Connect()) {

                btnSave.setEnabled(false);
                plot_results_thread();
                            }
            else finish();


        }
        else {Toast.makeText(this, "Invalid BT", Toast.LENGTH_SHORT).show(); finish();}

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");


        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color

        mChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();


        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(200f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);

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
/*
        set = data.getDataSetByIndex(2);
        set = createSet3();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  300f, set.getEntryCount()), 2);}

        set = data.getDataSetByIndex(3);
        set = createSet4();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  400f, set.getEntryCount()), 3);}
/*
        set = data.getDataSetByIndex(4);
        set = createSet5();
        data.addDataSet(set);
/*
        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  270f, set.getEntryCount()), 4);}

        set = data.getDataSetByIndex(5);
        set = createSet6();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry( 330f, set.getEntryCount()), 5);}

        set = data.getDataSetByIndex(6);
        set = createSet7();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  390f, set.getEntryCount()), 6);}

        set = data.getDataSetByIndex(7);
        set = createSet8();
        data.addDataSet(set);

        for(int i=0; i<=displayValue ; i++ ){
            data.addEntry(new Entry(  450f, set.getEntryCount()), 7);}
*/


        //SingleFeedRunThread();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try {

                            String startTring = ":";
                            mmOutputStream.write(startTring.getBytes());

                            readThread.cancel();
                            btSocket.close();
                            stopWorker2=true;



                        } catch (IOException ex) {
                        }
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disconnect");
        builder.setMessage("Are you sure?, BT will be disconnected...");
        builder.setIcon(R.drawable.alert_triangle_red_128);
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);
        builder.show();

        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
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


    private int year = 2015;

    private void addEntry(int counter, int chn1, int chn2, int chn3, int chn4) {

        LineData data = mChart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);


            // add a new x-value first
            data.addXValue("" + (set.getEntryCount() - displayValue));
            data.addEntry(new Entry((float) chn1 + 50f, set.getEntryCount()), 0);

            set = data.getDataSetByIndex(1);

            data.addEntry(new Entry((float) chn2 + 100f, set.getEntryCount()), 1);
/*
            set = data.getDataSetByIndex(2);

            data.addEntry(new Entry((float) chn3 + 300f, set.getEntryCount()), 2);

            set = data.getDataSetByIndex(3);

            data.addEntry(new Entry((float) chn4 + 400f, set.getEntryCount()), 3);

        /*
            set = data.getDataSetByIndex(4);

            data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 270f, set.getEntryCount()), 4);

            set = data.getDataSetByIndex(5);

            data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 330f, set.getEntryCount()), 5);

            set = data.getDataSetByIndex(6);

            data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 390f, set.getEntryCount()), 6);

            set = data.getDataSetByIndex(7);

            data.addEntry(new Entry((float) (Math.random() % 20 * 40) + 450f, set.getEntryCount()), 7);
*/


        contDisplay++;
        //System.out.println("Cont: "+ counter + " V1 = " + chn1 + " V2 = " + chn2 + " V3 = " + chn3 + " V4 = " + chn4 );

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
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getXValCount() - 121);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);

*/
            // let the chart know it's data has changed
            //mChart.notifyDataSetChanged();

            // limit the number of visible entries
            //mChart.setVisibleXRangeMaximum(displayValue);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            //mChart.moveViewToX(data.getXValCount() - (displayValue+1));

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
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



    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Channel 1");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(4f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet2() {

        LineDataSet set = new LineDataSet(null, "Channel 2");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#8BC34A"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(4f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#8BC34A"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet3() {

        LineDataSet set = new LineDataSet(null, "Channel 3");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#EF6C00"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#EF6C00"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet4() {

        LineDataSet set = new LineDataSet(null, "Channel 4");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#607D8B"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#607D8B"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
/*
    private LineDataSet createSet5() {

        LineDataSet set = new LineDataSet(null, "Channel 5");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#E91E63"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#E91E63"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet6() {

        LineDataSet set = new LineDataSet(null, "Channel 6");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#F44336"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#F44336"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet7() {

        LineDataSet set = new LineDataSet(null, "Channel 7");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#D500F9"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#D500F9"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet8() {

        LineDataSet set = new LineDataSet(null, "Channel 8");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#2196F3"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#2196F3"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

*/

    private void feedMultiple() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for(int i = 0; i < 5000; i++) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }




    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }


    int MultiplicationCombine(int x_high,  int x_low)
    {
        int combined;
        combined = x_high;
        combined = combined*256;
        combined |= x_low;
        return combined;
    }

    int MultiplicationCombine( int x_high,  int x_medium, int x_low )
    {
        int combined;
        combined = x_high;
        combined = combined*256*256;
        combined |=  x_medium*256;
        combined |=  x_low;
        return combined;
    }

    void plot_results_thread()
    {
        final Handler handler2 = new Handler();

        stopWorker2 = false;

        workerThread2 = new Thread(new Runnable()


        {


            public void run()
            {
                workerThread2.checkAccess();
                workerThread2.setPriority(5);

                while(!Thread.currentThread().isInterrupted() && !stopWorker2)
                {
                    try
                    {
                         {

                             if(bitsExpected == 16) {
                                 value1.setText("" + ch1);
                                 value2.setText("" + ch2);
                                 value3.setText("" + ch3);
                                 value4.setText("" + ch4);

                                 Header.setText("" + Header_int);
                                 PK_ID.setText("" + PK_ID_int);
                                 Counter.setText("" + PK_Counter_int);

                             }
                             else
                             {

                                 value1.setText("" + ch1);
                                 value2.setText("" + ch2);
                                 value3.setText("" + ch3);
                                 value4.setText("" + ch4);

                                 Header.setText("" + Header_int);
                                 PK_ID.setText("" + PK_ID_int);
                                 Counter.setText("" + PK_Counter_int);

                             }
                        }
                        //Thread.sleep(50);
                    }
                    catch (Exception ex)
                    {
                        stopWorker2 = true;
                    }
                }
            }
        });

        workerThread2.start();
    }

    boolean Connect (){

        boolean toreturn=false;

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

    /*try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }*/

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            mmOutputStream = btSocket.getOutputStream();
            readThread = new BluetoothReadThread();
            readThread.start();
            toreturn=true;
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        //mConnectedThread = new ConnectedThread(btSocket);
        //mConnectedThread.start();

    return toreturn;



    }


 class BluetoothReadThread extends Thread {


    private final InputStream iStream;
    private final OutputStream mmOutputStream;

    private boolean continueReading = true;

    public BluetoothReadThread() {
        InputStream tmp = null;
        OutputStream tmp2 = null;

        try {
            tmp = btSocket.getInputStream();
            tmp2 = btSocket.getOutputStream();

        } catch (IOException e) {
        }
        iStream = tmp;
        mmOutputStream = tmp2;


    }

    @Override
    public void run()  {

        int c;
        int waitCount=0;
        while (continueReading) {
            try {
                // Read integer values from Bluetooth stream.
                // Assemble them manually into doubles, split on newline (\n) character





                if (iStream.available() > 0) {
                    waitCount = 0;
                    c = iStream.read();
                    readBuffer[readBufferPosition++] = c;



                    if (readBufferPosition == bitsExpected) {


                if (bitsExpected==22) {
                    ch1 = MultiplicationCombine(readBuffer[4], readBuffer[3]);
                    ch2 = MultiplicationCombine(readBuffer[6], readBuffer[5]);
                    ch3 = MultiplicationCombine(readBuffer[8], readBuffer[7]);
                    ch4 = MultiplicationCombine(readBuffer[10], readBuffer[9]);




                }
                        else
                {
                    ch1 = MultiplicationCombine(readBuffer[5], readBuffer[4], readBuffer[3]);
                    ch2 = MultiplicationCombine(readBuffer[8], readBuffer[7], readBuffer[6]);
                    ch3 = MultiplicationCombine(readBuffer[11], readBuffer[10], readBuffer[9]);
                    ch4 = MultiplicationCombine(readBuffer[14], readBuffer[13], readBuffer[12]);


                }

                        Header_int = readBuffer[0];
                        PK_ID_int = readBuffer[1];
                        PK_Counter_int = readBuffer[2];


                        if(downsample++==4) { addEntry( PK_Counter_int, ch1/scaling, ch2/scaling, ch3/scaling, ch4/scaling); downsample=0;}

                        if(write_open){ osw.write("Cont: "+ PK_Counter_int + " V1 = " + ch1 + " V2 = " + ch2 + " V3 = " + ch3 + " V4 = " + ch4 + "\n");}

                        System.out.println("Cont: "+ PK_Counter_int + " V1 = " + ch1 + " V2 = " + ch2 + " V3 = " + ch3 + " V4 = " + ch4 );


                       // if(downsample++==14) { safe_copy(readBuffer);  plot=true; downsample=0;}



                        readBufferPosition=0;



                    }



                } else { // No input stream available, wait
                    if (waitCount >= 500000) {
                        // No data ready in 500000 loop cycles, ECG has probably been disconnected. Close self.
                        waitCount = 0;
                        System.out.println("----wait count expired " + iStream.available());
                        //continueReading = false;
                        //this.stopAndSendIntent();
                    } else {
                        waitCount++;
                    }
                }

            } catch (IOException e) {
                System.out.println(e+"\nError sending data + :"+e);
                // Bluetooth error! Stop reading.
                //this.stopAndSendIntent();
            }
        }
    }
    /*
            public void stopAndSendIntent() {

                this.cancel();

                Intent intent = new Intent();
                intent.setAction(BLUETOOTH_ACTION_DONE_READING);
                sendBroadcast(intent);
            }
    */
    public void cancel() {
        System.out.println("-----Cancelling readThread!!");
        try{
            iStream.close();
        } catch (IOException e) {
        } catch (NullPointerException e){};

        continueReading = false;
    }

}



    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    public void sendIni(View V) throws IOException
    {

        String startTring = "=";
        mmOutputStream.write(startTring.getBytes());
        Toast.makeText(this, "INI Command Sent", Toast.LENGTH_SHORT);

    }

    public void SendCancel(View V) throws IOException
    {
        String startTring = ":";
        mmOutputStream.write(startTring.getBytes());
        Toast.makeText(this, "STP Command Sent", Toast.LENGTH_SHORT);

    }

    public void safe_copy(int[]value)
    {


        readbuffer_copy [cont_write++] = value;

        if (cont_write>999)cont_write=0;



    }

    public void onClickCheckToSave(View V)
    {
        if (CheckSave.isChecked()) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/EEG_adq");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
        } else {
        }
        SimpleDateFormat s = new SimpleDateFormat("dd_MM_yyyy_ hh:mm:ss");
        String nameFile = s.format(new Date());

        File SdCard = Environment.getExternalStorageDirectory();
        String Convert = SdCard.getAbsolutePath().toString() + "/EEG_adq";

        final File file = new File(Convert, "SF " + nameFile.toString() + ".txt");


        try {
            osw = new OutputStreamWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            success=false;
            e.printStackTrace();
        }

           if (success){ CreateMarker.setVisibility(View.VISIBLE); btnSave.setVisibility(View.VISIBLE); write_open=true; CheckSave.setEnabled(false); btnSave.setEnabled(true);}

    }
    }

    public void saveFile(View V) throws  IOException
    {
        osw.write("end");
        osw.close();
        Toast.makeText(this, "Saved at EEG_adq folder (internal mem)", Toast.LENGTH_SHORT ).show();
        btnSave.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(false);
        CreateMarker.setVisibility(View.INVISIBLE);
        btnMarket.setVisibility(View.INVISIBLE);
        CheckSave.setEnabled(true);
        CheckSave.setChecked(false);
        write_open=false;

    }

    public void onClickShowValues(View V)
    {



        int x;

        if ( CheckShowValues.isChecked()) x = View.VISIBLE;
        else x = View.INVISIBLE;


        lv1.setVisibility(x);
        value1.setVisibility(x);

        lv2.setVisibility(x);
        value2.setVisibility(x);

        lv3.setVisibility(x);
        value3.setVisibility(x);

        lv4.setVisibility(x);
        value4.setVisibility(x);

        lhe.setVisibility(x);
        Header.setVisibility(x);

        lid.setVisibility(x);
        PK_ID.setVisibility(x);

        lPK_C.setVisibility(x);
        Counter.setVisibility(x);


    }


    public void onClickMarket(View V) throws  IOException
    {
        if(CreateMarker.isChecked()) {btnMarket.setVisibility(View.VISIBLE);btnMarket.setText("Closed"); btnMarket.setEnabled(true);}

    }

    public void onClickbtn(View V) throws  IOException
    {
        String toWrite = btnMarket.getText().toString();

        osw.write(toWrite + "\n");

        if (toWrite.equals("Closed")) btnMarket.setText("Open");
        else btnMarket.setText("Closed");



    }


    }


