package de.uniluebeck.greennav;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This is the MainActivity of the Application. It provides a Webview for input purposes, some
 * debug features and an instance of a JavaScriptInterface (JSInterface)
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    /* permissionString to call for permission on runtime API 22+ */
    private String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    private Toolbar viewToolbar;
    private int RequestId = 1;
    private TextView originText; // debug
    private String serverURL = "http://141.83.176.208/index.html";
    private String serverIP = "192.168.2.100:8112";
    private String compassDegree;
    private String compassHeading;
    private boolean evalMode;
    private boolean detailMode;
    private float degree;
    private SensorManager sensorManager;
    private Tracker tracker;
    WebView myMainWebView;
    JSInterface jsInterface;
    AppContext myAppContext;
    private int[] actions = new int[3]; // evaluation-data array, size depends on the number of datapoints you want to collect
    private int axisPressure = 0;

    /**
     * onCreate is called when the application starts
     *
     * @param savedInstanceState - the state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /* Check SDK Version for Android 6 */
        //if (Build.VERSION.SDK_INT > 22)
        //    requestPermissions(permissions, RequestId);

        /* get variables from settings window */
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            serverURL = extras.getString("SERVER_URL");
            evalMode = extras.getBoolean("EVALUATION_MODE");
            detailMode = extras.getBoolean("DETAIL_MODE");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        /* synchronous Task call to implement 1 minute delay for taking screenshots in evaluation mode */
        Sync sync = new Sync(call, 60 * 1000);

        /* setup sensorManager for compass */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        /* set context */
        myAppContext = (AppContext) getApplicationContext();

        viewToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(viewToolbar);

        /* set a new Context */
        final AppContext app = (AppContext) getApplicationContext();

        /* instanciate a new tracker */
        app.tracker = new Tracker(this);
        originText = (TextView) findViewById(R.id.originTextView); // debug
        myMainWebView = (WebView) findViewById(R.id.mainView);

        /* Java-Script stuff */
        CookieSyncManager.createInstance(this);
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();

        myMainWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return false;
            }

            /**
             * will be called when the page has loaded
             * @param view - MainActivities' WebView
             * @param url - The url of the serverText
             */
            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                view.clearCache(true);
            }
        });

        /**
         * Evaluation-mode, monitor userActions on WebView, create screenshots every minute
         */
        if (evalMode && isSDAvailable())
        {
            final Time today = new Time(Time.getCurrentTimezone());

            File evalData = Environment.getExternalStorageDirectory();
            evalData = new File(evalData.getPath() + "/Android/data/greennav/logs");
            if (!evalData.exists())
            {
                evalData.mkdirs();
            }

            FileOutputStream stream = null;
            try
            {
                stream = new FileOutputStream(evalData.getPath() + "/userEval.txt");
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setCancelable(false);

                builder.setMessage("Cannot access file!");

                builder.setNeutralButton("I see", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }

            final FileOutputStream finalStream = stream;
            final PrintStream printStream = new PrintStream(stream);
            myMainWebView.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (finalStream != null)
                    {
                        /** here are the evaluation data sources defined
                         *  adapt data array if you perform changes
                         */
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_UP:
                                today.setToNow();
                                printStream.print("ACTION_UP    : " + MotionEvent.ACTION_UP + ", at: " + today.format("%k:%M:%S") + "\r\n");
                                actions[0]++;

                            case MotionEvent.ACTION_DOWN:
                                today.setToNow();
                                printStream.print("ACTION_DOWN  : " + MotionEvent.ACTION_DOWN + ", at: " + today.format("%k:%M:%S") + "\r\n");
                                actions[1]++;

                            case MotionEvent.ACTION_CANCEL:
                                today.setToNow();
                                printStream.print("ACTION_CANCEL: " + MotionEvent.ACTION_CANCEL + ", at: " + today.format("%k:%M:%S") + "\r\n");
                                actions[2]++;

                            case MotionEvent.ACTION_SCROLL:
                                today.setToNow();
                                printStream.print("ACTION_SCROLL: " + MotionEvent.ACTION_SCROLL + ", at: " + today.format("%k:%M:%S") + "\r\n");
                                actions[3]++;


                            case MotionEvent.AXIS_PRESSURE:
                                today.setToNow();
                                printStream.print("AXIS_PRESSURE: " + MotionEvent.AXIS_PRESSURE + ", at: " + today.format("%k:%M:%S") + "\r\n");
                                axisPressure = MotionEvent.AXIS_PRESSURE;

                                break;
                        }
                    }
                    return false;
                }
            });
        }


        WebSettings ws = myMainWebView.getSettings();
        /* enabling JS-Interface */
        ws.setJavaScriptEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);

        jsInterface = new JSInterface(this, myMainWebView);
        app.tracker.registerJSInterface(jsInterface);
        myMainWebView.addJavascriptInterface(jsInterface, "android");

        myMainWebView.measure(100, 100);
        //ws.setUseWideViewPort(true);
        //ws.setLoadWithOverviewMode(true);

        /* Debug Button */
        final Button trackButton = (Button) findViewById(R.id.mainTrackButton);
        trackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Log.d("GreenNav Android", "MainActivity:onCreate(), trackButton onClick()");
                double[] myCoordinates = app.tracker.track();
                originText.setText(myCoordinates[0] + "° N , " + myCoordinates[1] + "° E");
            }
        });

        myMainWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public boolean onConsoleMessage(ConsoleMessage m)
            {
                Log.d("GreenNav Android", "MainActivity:onCreate(), message = " + m.message() + " -- (line " + m.lineNumber() + " of " + m.sourceId() + ")");
                return super.onConsoleMessage(m);
            }
        });
        //myMainWebView.loadUrl("http://192.168.0.52:8112/index.html");
        myMainWebView.loadUrl(serverURL);
        //myMainWebView.loadUrl("http://141.83.177.5:8112/index.html");

        /* Debug-Button */
        final Button navigateButton = (Button) findViewById(R.id.startButton);
        navigateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d("GreenNav Android", "Course: " + compassDegree + "°" + ", heading: " + compassHeading);
                Log.d("GreenNav Android", "Evaluationmode active? " + evalMode);
                Log.d("GreenNav Android", "Detailmode active? " + detailMode);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_settings));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        /* sensor registered listeners */
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        /* compass: save battery if paused */
        sensorManager.unregisterListener(this);
        /* if app is paused, write evalation data (sum of different interactions) onto disk */
        if (evalMode)
        {
            writeEvalData(actions, axisPressure);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        getOrientation(sensorEvent);
        compassDegree = String.format("%.1f", degree);
        compassHeading = calculateDirection(degree);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle interaction on the action bar items
        switch (item.getItemId())
        {
            case R.id.action_settings:
                openSettings();
                return true;
            //case R.id.action_search:
            //openSearch();
            //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
    }

    /**
     * this method writes the collected data into the evaluation file
     *
     * @param actions  an array of the number of actions depending on type (= arrayIndex)
     * @param pressure
     */
    private void writeEvalData(int[] actions, int pressure)
    {
        BufferedWriter output = null;
        try
        {
            File evalSumData = Environment.getExternalStorageDirectory();
            FileWriter sumWriter = new FileWriter(evalSumData.getPath() + "/Android/data/greennav/logs/userEval.txt", true);
            output = new BufferedWriter(sumWriter);
            output.write("\r\n");
            output.write("App is Paused, collecting Data... \r\n");
            for (int i = 0; i < actions.length; i++)
            {
                output.write("Sum of action no. " + i + " = " + actions[i] + "\r\n");
            }

            output.write("Axis pressure: " + pressure + "\r\n");

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Check if SD-card is available
     *
     * @return SD-cards' status
     */
    public boolean isSDAvailable()
    {
        String status = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(status))
        {
            return true;
        } else

            return false;
    }

    /**
     * if evaluation mode is active, this method will be called every minute
     * and creates a screenshot every minute
     */
    private void createScreenshot()
    {
        if (evalMode)
        {
            try
            {
                String mPath = Environment.getExternalStorageDirectory().toString() + "/Android/data/greennav/logs/" + System.currentTimeMillis() + ".jpg";

                // create screen capture bitmap
                View v1 = getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);

                File imageFile = new File(mPath);

                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * compass
     * sets the actual degree of the compass
     */
    private void getOrientation(SensorEvent sensorEvent)
    {
        Log.d("GreenNav Android", "getOrientation(), value: " + sensorEvent.values[0]);
        degree = sensorEvent.values[0];
    }

    /**
     * calculates the cardinal direction
     *
     * @param degree - the degree of x-axis
     * @return heading - the heading as a string
     */
    protected String calculateDirection(float degree)
    {
        String heading = "";

        if (degree >= 337.5 || degree < 22.5)
        {
            heading = "N";
        }
        if (degree >= 22.5 && degree < 67.5)
        {
            heading = "NE";
        }
        if (degree >= 67.5 && degree < 112.5)
        {
            heading = "E";
        }
        if (degree >= 112.5 && degree < 157.5)
        {
            heading = "SE";
        }
        if (degree >= 157.5 && degree < 202.5)
        {
            heading = "S";
        }
        if (degree >= 202.5 && degree < 247.5)
        {
            heading = "SW";
        }
        if (degree >= 247.5 && degree < 292.5)
        {
            heading = "W";
        }
        if (degree >= 292.5 && degree < 337.5)
        {
            heading = "NW";
        }
        return heading;
    }

    /**
     * finds the devices' loaction and sets origins' text
     */
    private void findLocation()
    {
        this.tracker = myAppContext.tracker;
        double[] myCoordinates = myAppContext.tracker.track();
        Log.d("FindLocation", "Your location: " + myCoordinates[0] + "° N , " + myCoordinates[1] + "° E");
    }

    /**
     * The intent to open settings' activity
     */
    private void openSettings()
    {
        Intent intent;
        intent = new Intent(MainActivity.this, Settings.class);
        /* define transfer parameters to use in settings' activity */
        intent.putExtra("SERVER_IP", serverIP);
        intent.putExtra("DETAIL_MODE", detailMode);
        intent.putExtra("EVALUATION_MODE", evalMode);
        startActivity(intent);
        finish();
    }

    /**
     * getter for the JavaScriptInterface
     *
     * @return myMainWebView - the acutal WebView
     */
    public WebView getWebView()
    {
        return myMainWebView;
    }

    /**
     * This method implements a fade for a element of the view
     *
     * @param view
     */
    public void fade(View view)
    {
        ImageView image = (ImageView) findViewById(R.id.imageView);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        image.startAnimation(animation1);
    }

    /**
     * handle syncronous task, setup timer for 1 Minute (60*1000 ms)
     */
    final private Runnable call = new Runnable()
    {
        public void run()
        {
            createScreenshot(); // call every minute
            handler.postDelayed(call, 60 * 1000);
        }
    };
    public final Handler handler = new Handler();

    /**
     * Sync-object to setup a minute-counter
     */
    public class Sync
    {
        Runnable task;

        public Sync(Runnable task, long time)
        {
            this.task = task;
            handler.removeCallbacks(task);
            handler.postDelayed(task, time);
        }
    }
}