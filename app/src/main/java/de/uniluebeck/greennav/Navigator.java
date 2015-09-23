package de.uniluebeck.greennav;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * This class provides the navigation-activity. It is called within the MainActivity.java
 * it realizes a GPSTracker and a compass to provide position and heading status.
 */
public class Navigator extends Activity implements SensorEventListener
{

    //String serverURL = "http://141.83.177.5:8112/index.html";
    private String serverURL = "http://192.168.2.100:8112/index.html";
    //String serverURL = "http://www.isp.uni-luebeck.de/greennav/";
    //String serverURL = "http://141.83.177.47:8112/index.html";
    private SensorManager sensorManager;
    private Sensor magnetometer;
    private TextView compassDegree;
    private TextView originTextView;
    private TextView destinationTextView;
    private JSInterface jsInterface;
    private Tracker tracker;
    private float degree;
    //private ImageView image;

    AppContext app;
    String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    int RequestId = 2;

    /**
     * called from MainActivity
     *
     * @param savedInstanceState - The state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            serverURL = extras.getString("SERVER_URL");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigator_layout);

        if (Build.VERSION.SDK_INT > 22)
            //requestPermissions(permissions, RequestId);

        /* compass' sensorManager and listeners */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        /* set context */
        app = (AppContext) getApplicationContext();
        /* create textfields */
        compassDegree = (TextView) findViewById(R.id.heading);
        originTextView = (TextView) findViewById(R.id.originTextView);
        destinationTextView = (TextView) findViewById(R.id.destTextView);

        findLocation();
        initializeWebView();
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
    }

    /**
     * finds the devices' loaction and sets origins' text
     */
    private void findLocation()
    {
        this.tracker = app.tracker;
        double[] myCoordinates = app.tracker.track();
        originTextView.setText("Your location: " + myCoordinates[0] + "° N , " + myCoordinates[1] + "° E");
    }

    /**
     * sets destinations' TextView
     *
     * @param destination - the desired destination
     */
    private void setDestination(String destination)
    {
        destinationTextView.setText("Destination: " + destination);
    }

    /**
     * compass
     * sets the actual degree of the compass
     */
    private void getOrientation(SensorEvent sensorEvent)
    {
        //Log.d("GreenNav Android", "Navigator:getOrientation(), value: " + sensorEvent.values[0]);
        degree = sensorEvent.values[0];
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        getOrientation(sensorEvent);
        //Log.d("GreenNav Android", "Navigator:onSensorChanged(), degree: " + degree);
        String stringDegree = String.format("%.1f", degree);
        String stringHeading = calculateDirection(degree);
        compassDegree.setText("Course: " + stringDegree + "°" + ", heading: " + stringHeading);
    }

    /**
     * calcuates the cardinal direction
     *
     * @param  degree - the degree of x-axis
     * @return heading - the heading as string
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
    }

    /**
     * init the Navigator WebView
     */
    private void initializeWebView()
    {
        WebView myWebView = (WebView) findViewById(R.id.mainView);

        JSInterface jsInterface = new JSInterface(this, myWebView);
        app.tracker.registerJSInterface(jsInterface);

        myWebView.addJavascriptInterface(jsInterface, "android");
        WebSettings ws = myWebView.getSettings();
        myWebView.setBackgroundColor(Color.parseColor("#99ff99"));

        myWebView.setWebViewClient(new WebViewClient()

        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                view.clearCache(true);
            }
        });
        // VULN: will introduce XSS
        ws.setJavaScriptEnabled(true);

        //myWebView.loadUrl(serverURL);
        myWebView.getSettings().setDisplayZoomControls(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        myWebView.loadUrl(serverURL);
        myWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public boolean onConsoleMessage(ConsoleMessage m)
            {
                Log.d("GreenNav Client", m.message() + " -- (line " + m.lineNumber() + " of " + m.sourceId() + ")");
                return super.onConsoleMessage(m);
            }
        });
    }
}
