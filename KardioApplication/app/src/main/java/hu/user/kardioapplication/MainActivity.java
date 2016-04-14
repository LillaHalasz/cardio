package hu.user.kardioapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.internal.ButterKnifeProcessor;

public class MainActivity extends AppCompatActivity
{

    @Bind(R.id.et_lastname) EditText lastname;
    @Bind(R.id.et_firstname)EditText firstname;
    @Bind(R.id.et_telnumber) EditText telnumber;
    @Bind(R.id.et_email) EditText email;
    @Bind(R.id.datePicker) DatePicker datePicker;
    @Bind(R.id.btn_continue) Button btncontinue;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Út az egészséghez");
        ButterKnife.bind(this);

        //checkViews();

        Button openMap = (Button) findViewById(R.id.btn_continue);
        openMap.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkViews()
    {
        lastname.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.hasText(lastname);
            }
        });

        firstname.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.hasText(firstname);
            }
        });

        email.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.isEmailAddress(email, true);
            }
        });

        telnumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Validation.isPhoneNumber(telnumber, false);
            }
        });

       btncontinue.setOnClickListener(new View.OnClickListener()
       {
           @Override
           public void onClick(View v)
           {
               if (checkValidation())
               {
                   Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                   startActivity(intent);
                   finish();
               }
               else
                   Toast.makeText(getApplicationContext(), "Kérem ellenőrizze, hogy minden mezőt kitöltött-e!", Toast.LENGTH_SHORT).show();
           }
       });
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(!Validation.hasText(firstname)) ret = false;
        if(!Validation.hasText(lastname)) ret = false;
        if(!Validation.isEmailAddress(email, true)) ret = false;
        if(!Validation.isPhoneNumber(telnumber, false)) ret = false;

        return ret;
    }



}
