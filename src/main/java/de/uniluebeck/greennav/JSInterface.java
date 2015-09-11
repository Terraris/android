package de.uniluebeck.greennav;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

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


    @JavascriptInterface
    public void registerGPSCallback(String callback)
    {
        Log.d("GreenNav Android", "JSInterface:registerGPSCallback(), callback = " + callback);
        this.callback = callback;

    }

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

    @JavascriptInterface
    public void create(final String msg)
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
