package de.uniluebeck.greennav;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
public class MainActivity extends Activity
{
    /* permissionString to call for permission on runtime */
    private String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    private int RequestId = 1;
    private TextView origin;
    WebView myMainWebView;
    JSInterface jsInterface;
    private String serverURL = "http://192.168.2.100:8112/index.html";


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
        if(extras != null)
        {
           serverURL = extras.getString("SERVER_URL");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

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
                /* start Navigator */
                Intent intent;
                intent = new Intent(MainActivity.this, Navigator.class);
                intent.putExtra("SERVER_URL", serverURL);
                startActivity(intent);
                finish();
            }
        });
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