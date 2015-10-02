package de.uniluebeck.greennav;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

/**
 * This is the MainActivity of the Application. It provides a Webview for input purposes, some
 * debug features and an instance of a JavaScriptInterface (JSInterface)
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    /* permissionString to call for permission on runtime */
    private String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    private Toolbar toolbar;
    private int RequestId = 1;
    private TextView origin;

    private String serverURL = "http://192.168.2.100:8112/index.html";
    private String serverIP = "192.168.2.100:8112";
    private String stringDegree;
    private String stringHeading;

    private boolean eval;
    private boolean detail;
    private float degree;
    private SensorManager sensorManager;
    private Tracker tracker;
    WebView myMainWebView;
    JSInterface jsInterface;
    AppContext app;

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


        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            serverURL = extras.getString("SERVER_URL");
            eval = extras.getBoolean("EVALUATION_MODE");
            detail = extras.getBoolean("DETAIL_MODE");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        /* set context */
        app = (AppContext) getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        /* set a new Context */
        final AppContext app = (AppContext) getApplicationContext();

        /* instanciate a new tracker */
        app.tracker = new Tracker(this);
        final Button trackButton = (Button) findViewById(R.id.mainTrackButton);
        origin = (TextView) findViewById(R.id.originTextView);
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
        trackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Log.d("GreenNav Android", "MainActivity:onCreate(), trackButton onClick()");
                double[] myCoordinates = app.tracker.track();
                origin.setText(myCoordinates[0] + "° N , " + myCoordinates[1] + "° E");

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
                Log.d("GreenNav Android", "Course: " + stringDegree + "°" + ", heading: " + stringHeading);
                Log.d("GreenNav Android", "Evaluationmode active? " + eval);
                Log.d("GreenNav Android", "Detailmode active?" + detail);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_settings));
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setOnQueryTextListener();
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
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        getOrientation(sensorEvent);
        stringDegree = String.format("%.1f", degree);
        stringHeading = calculateDirection(degree);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle presses on the action bar items
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
     * compass
     * sets the actual degree of the compass
     */
    private void getOrientation(SensorEvent sensorEvent)
    {
        Log.d("GreenNav Android", "getOrientation(), value: " + sensorEvent.values[0]);
        degree = sensorEvent.values[0];
    }


    /**
     * calcuates the cardinal direction
     *
     * @param degree - the degree of x-axis
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

    /**
     * finds the devices' loaction and sets origins' text
     */
    private void findLocation()
    {
        this.tracker = app.tracker;
        double[] myCoordinates = app.tracker.track();
        Log.d("FindLocation", "Your location: " + myCoordinates[0] + "° N , " + myCoordinates[1] + "° E");
    }

    private void openSettings()
    {
        Intent intent;
        intent = new Intent(MainActivity.this, Settings.class);
        intent.putExtra("SERVER_IP", serverIP);
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

    public void fade(View view)
    {
        ImageView image = (ImageView) findViewById(R.id.imageView);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        image.startAnimation(animation1);
    }
}