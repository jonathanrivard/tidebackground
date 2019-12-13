package io.github.jonathanrivard.autotidebackground;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences shared;
    SharedPreferences.Editor editor;
    Switch doAutoSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        shared = getSharedPreferences("TideAppSettings", Context.MODE_PRIVATE);
        editor = shared.edit();
        doAutoSwitch = (Switch) findViewById(R.id.settingsDoAutoSwitch);
        doAutoSwitch.setChecked(shared.getBoolean("autoChange", false));
    }

    public void back(View view){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    public void save(View view){
        editor.putBoolean("autoChange", doAutoSwitch.isChecked());
        editor.commit();
        Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
    }
}
