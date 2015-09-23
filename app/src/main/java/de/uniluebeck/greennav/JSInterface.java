package de.uniluebeck.greennav;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * This class provides a JavaScript Interface to connect the android Application to the webBrowser
 */
public class JSInterface extends Service
{
    final Activity t;
    final WebView view;
    String callback;

    public JSInterface(Activity t, WebView view)
    {
        this.t = t;
        this.view = view;
    }

    /**
     * Registers a Callback for the web application to manage origin of the route
     *
     * @param callback - the Callback for the webapp
     */
    @JavascriptInterface
    public void registerGPSCallback(String callback)
    {
        Log.d("GreenNav Android", "JSInterface:registerGPSCallback(), callback = " + callback);
        this.callback = callback;

    }

    /**
     * fires a GPSChangeEvent on locationChanged()
     *
     * @param lat - the latitude of the device
     * @param lon - the longitude of the device
     */
    public void fireGPSChangeEvent(double lat, double lon)
    {
        final String js = "javascript:" + callback + "(" + lat + "," + lon + ");";
        Log.d("GreenNav Android", "JSInterface:fireGPSChangeEvent(), js =" + js);
        if (t != null)
        {
            t.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    view.loadUrl(js);
                }
            });
        }

    }

    /**
     * is called onCreate()
     *
     * @param msg - the message which will be send
     */
    @JavascriptInterface
    public void create(final String msg)
    {

    }

    /**
     * generated by JSInterface
     *
     * @param intent - The intent to call
     * @return null - a null value
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}