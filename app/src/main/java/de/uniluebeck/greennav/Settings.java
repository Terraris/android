package de.uniluebeck.greennav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

/**
 * This class provides the welcome screen, which is used to set the serverURLs
 */
public class Settings extends Activity
{

    private String server;
    private String serverURL;
    private Switch evaluationSwitch;
    private Switch detailSwitch;
    private Button start, info, contact;
    private Boolean eval = false;
    private Boolean detail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);

        fade();
        final EditText serverText = (EditText) findViewById(R.id.serverEditText);
        Bundle extras = getIntent().getExtras();
        //serverText.setText(extras.getString("SERVER_IP")); // debug
        info = (Button) findViewById(R.id.infoButton);
        info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        contact = (Button) findViewById(R.id.contactButton);
        contact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        start = (Button) findViewById(R.id.startApp);
        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                server = serverText.getText().toString();
                serverURL = "http://" + server + "/index.html";
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("SERVER_URL", serverURL);
                if (eval)
                {
                    intent.putExtra("EVALUATION_MODE", true);
                }
                if (detail)
                {
                    intent.putExtra("DETAIL_MODE", true);
                }
                startActivity(intent);
                finish();
            }
        });

        evaluationSwitch = (Switch) findViewById(R.id.eval_switch);
        if (extras.getBoolean("EVALUATION_MODE"))
        {
            evaluationSwitch.setChecked(true);
        }
        evaluationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    eval = true;
                } else
                {
                    eval = false;
                }
            }
        });

        detailSwitch = (Switch) findViewById(R.id.detail_switch);
        if (extras.getBoolean("DETAIL_MODE"))
        {
            detailSwitch.setChecked(true);
        }
        detailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    detail = true;
                } else
                {
                    detail = false;
                }
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