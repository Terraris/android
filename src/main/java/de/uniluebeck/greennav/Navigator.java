package de.uniluebeck.greennav;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class Navigator extends Activity implements SensorEventListener
{

    //String serverURL = "http://192.168.178.23/index.html";
    String serverURL = "http://192.168.2.100:8112/index.html";
    //String serverURL = "http://www.isp.uni-luebeck.de/greennav/";
    //String serverURL = "http://141.83.177.47:8112/index.html";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView compassDegree;
    private TextView origin;
    private JSInterface jsInterface;
    private Tracker tracker;
    float[] gravity;
    float[] geomagnetic;
    Float azimut;

    //private ImageView image;

    AppContext app;

    String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    int RequestId = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigator_layout);

        requestPermissions(permissions, RequestId);

        final Button trackButton = (Button) findViewById(R.id.trackButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        app = (AppContext) getApplicationContext();
        compassDegree = (TextView) findViewById(R.id.compassDegree);
        origin = (TextView) findViewById(R.id.originTextView);
        //image = (ImageView) findViewById(R.id.iconView);
        this.tracker = app.tracker;

        trackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                double[] myCoordinates = app.tracker.track();
                origin.setText("lat: " + myCoordinates[0] + " , long: " + myCoordinates[1]);
                //makeText(getApplicationContext(), "Coords: " + Arrays.toString(myCoordinates), LENGTH_LONG).show();

            }
        });
        initializeWebView();

    }


    @Override
    protected void onResume()
    {
        super.onResume();
        // sensor registered listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // save battery if paused
        sensorManager.unregisterListener(this);
    }


    /**
     * compass
     */
    private void getOrientation(SensorEvent sensorEvent)
    {
        Log.d("GreenNav Android", "Navigator:getOrientation()");
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = sensorEvent.values;
        Log.d("GreenNav Android", "Navigator:getOrientation(), gravity = " + gravity);
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            Log.d("GreenNav Android", "Navigator:getOrientation(), geomagnetic = " + geomagnetic);
        geomagnetic = sensorEvent.values;
        if (gravity != null && geomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
        //degree = Math.round(sensorEvent.values[0]);
        if (azimut != null)
        {
            compassDegree.setText("Heading: " + Float.toString(azimut) + "°");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        getOrientation(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    /**
     * WebView
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
