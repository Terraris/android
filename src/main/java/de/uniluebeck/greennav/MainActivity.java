package de.uniluebeck.greennav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


public class MainActivity extends Activity
{

    private String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };
    private int RequestId = 1;


    WebView myMainWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestPermissions(permissions, RequestId);
        Log.d("GreenNav Android", "MainActivity:onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        final AppContext app = (AppContext) getApplicationContext();
        app.tracker = new Tracker(this);
        final Button trackButton = (Button) findViewById(R.id.mainTrackButton);

        myMainWebView = (WebView) findViewById(R.id.mainView);

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

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                view.clearCache(true);
            }
        });

        WebSettings ws = myMainWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        JSInterface jsInterface = new JSInterface(this, myMainWebView);
        app.tracker.registerJSInterface(jsInterface);
        myMainWebView.addJavascriptInterface(jsInterface, "android");
        myMainWebView.measure(100, 100);
        //ws.setUseWideViewPort(true);
        //ws.setLoadWithOverviewMode(true);

        trackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d("GreenNav Android", "MainActivity:onCreate(), trackButton onClick()");
                double[] myCoordinates = app.tracker.track();
                Log.d("GreenNav Android", "MainActivity:onCreate(), myCoordinates = " + String.valueOf(myCoordinates));
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
        myMainWebView.loadUrl("http://192.168.2.100:8112/index.html");

        final Button navigateButton = (Button) findViewById(R.id.startButton);

        navigateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent;
                intent = new Intent(MainActivity.this, Navigator.class);
                startActivity(intent);
                finish();

            }
        });
    }

    public WebView getWebView()
    {
        return myMainWebView;
    }


    /**
     String serverURL = "http://141.83.177.47:8112/index.html";

     public class JSInterface {
     final MainActivity t;

     public JSInterface(MainActivity t) {
     this.t = t;
     }

     @JavascriptInterface public void create(final String msg) {
     t.runOnUiThread(new Runnable() {
     @Override public void run() {
     Toast toast = Toast.makeText(t.getApplicationContext(), msg, Toast.LENGTH_SHORT);
     TextView tw = (TextView) findViewById(R.id.streetTextView);
     tw.setText(msg);
     toast.show();
     }
     });
     }
     }

     @Override protected void onCreate(Bundle savedInstanceState) {

     super.onCreate(savedInstanceState);
     setContentView(R.layout.main_layout);

     CookieSyncManager.createInstance(this);
     CookieManager cm = CookieManager.getInstance();
     cm.removeAllCookie();

     WebView w = (WebView) findViewById(R.id.webView);
     w.setBackgroundColor(Color.parseColor("#99ff99"));

     w.addJavascriptInterface(new JSInterface(this), "android");

     w.setWebViewClient(new WebViewClient() {
     @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
     view.loadUrl(url);
     return false;
     }

     @Override public void onPageFinished(WebView view, String url) {
     super.onPageFinished(view, url);
     view.clearCache(true);
     }
     });

     WebSettings ws = w.getSettings();
     ws.setJavaScriptEnabled(true);
     w.loadUrl(serverURL);
     w.measure(100, 100);
     ws.setUseWideViewPort(true);
     ws.setLoadWithOverviewMode(true);

     w.setWebChromeClient(new WebChromeClient() {
     @Override public boolean onConsoleMessage(ConsoleMessage m) {
     Log.d("GreenNav Client", m.message() + " -- (line " + m.lineNumber() + " of " + m.sourceId() + ")");
     return super.onConsoleMessage(m);
     }
     });
     }
     */
}