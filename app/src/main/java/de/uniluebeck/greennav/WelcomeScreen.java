package de.uniluebeck.greennav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * This class provides the welcome screen, which is used to set the serverURLs
 */
public class WelcomeScreen extends Activity
{

    private String server;
    private String serverURL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);

        final EditText serverText = (EditText) findViewById(R.id.serverEditText);
        Button start = (Button) findViewById(R.id.startApp);

        fade();
        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                server = serverText.getText().toString();
                serverURL = "http://" + server + "/index.html";
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("SERVER_URL", serverURL);
                startActivity(intent);
                finish();
            }
        });

    }

    public void fade()
    {
        ImageView image = (ImageView) findViewById(R.id.imageView);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        image.startAnimation(animation1);

    }
}
