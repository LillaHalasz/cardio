package hu.user.kardioapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import org.joda.time.DateTime;
import org.joda.time.Years;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersonalDetailsActivity extends AppCompatActivity
{

    @Bind(R.id.et_lastname)
    EditText etLastName;
    @Bind(R.id.et_firstname)
    EditText etFirstName;
    @Bind(R.id.et_telnumber)
    EditText etTelNum;
    @Bind(R.id.et_email)
    EditText etEmail;
    @Bind(R.id.datePicker)
    DatePicker datePicker;
    @Bind(R.id.et_birthplace)
    EditText etBirthPlace;
    @Bind(R.id.btn_continue)
    Button btnContinue;
    @Bind(R.id.rbGenderMale)
    RadioButton rbGenderMale;
    @Bind(R.id.rbGenderFemale)
    RadioButton rbGenderFemale;


    SharedPreferences sharedPreferences;
    int gender;
    int birthYear;
    int birthMonth;
    int birthDay;
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_details);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Személyes adatok");
        ButterKnife.bind(this);

        setDividerColor(datePicker, Color.MAGENTA);

        //checkViews();


      /*  btnContinue.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
                startActivity(intent);
                finish();
            }
        });*/


    }
    @OnClick(R.id.btn_continue)
    public void startActivity(Button button)
    {
        if (rbGenderMale.isChecked()) gender = 0; //male
        else gender = 1; //female

        birthYear = datePicker.getYear();
        birthMonth = datePicker.getMonth() + 1;
        birthDay = datePicker.getDayOfMonth();



        DateTime dateOfBirth = new DateTime(birthYear,birthMonth, birthDay,0 ,0);
        DateTime currentDate = DateTime.now();
        age = Years.yearsBetween(dateOfBirth,currentDate).getYears();

        sharedPreferences = getSharedPreferences("Personal", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FirstName" , etFirstName.getText().toString());
        editor.putString("LastName" , etLastName.getText().toString());
        editor.putString("TelNumber" , etTelNum.getText().toString());
        editor.putString("BirthPlace", etBirthPlace.getText().toString());
        editor.putString("Email", etEmail.getText().toString());
        editor.putInt("Gender", gender);
        editor.putInt("BirthDay", birthDay);
        editor.putInt("BirthMonth", birthMonth);
        editor.putInt("BirthYear", birthYear);
        editor.putInt("Age", age);
        editor.commit();
        Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkViews()
    {
        etLastName.addTextChangedListener(new TextWatcher()
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
                Validation.hasText(etLastName);
            }
        });

        etFirstName.addTextChangedListener(new TextWatcher()
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
                Validation.hasText(etFirstName);
            }
        });

        etEmail.addTextChangedListener(new TextWatcher()
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
                Validation.isEmailAddress(etEmail, true);
            }
        });

        etTelNum.addTextChangedListener(new TextWatcher()
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
                Validation.isPhoneNumber(etTelNum, false);
            }
        });

     /*   btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkValidation())
                {
                    Intent intent = new Intent(PersonalDetailsActivity.this, HealthMattersActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "Kérem ellenőrizze, hogy minden mezőt kitöltött-e!", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if (!Validation.hasText(etFirstName)) ret = false;
        if (!Validation.hasText(etLastName)) ret = false;
        if (!Validation.isEmailAddress(etEmail, true)) ret = false;
        if (!Validation.isPhoneNumber(etTelNum, false)) ret = false;

        return ret;
    }

    private void setDividerColor(DatePicker picker, int color)
    {

        java.lang.reflect.Field[] pickerFields = DatePicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields)
        {
            if (pf.getName().equals("mSelectionDivider"))
            {
                pf.setAccessible(true);
                try
                {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                }
                catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
