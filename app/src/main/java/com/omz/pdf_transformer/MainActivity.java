package com.omz.pdf_transformer;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView pageView = findViewById(R.id.documentView);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        try {
            new PDFContentManager(getApplicationContext());

        } catch (IOException e) {
        }
        Log.d("CONFIG", "PRE");
        DeployAsset("ReaderViewPreference.json");
        DeployAsset("ViewerTemplate.json");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filePick = new Intent(Intent.ACTION_GET_CONTENT);
                filePick.setType("application/pdf");
                startActivityForResult(filePick, 2);
                Log.d("fab", "pulling up file picker");
            }
        });
    }

        @Override
        protected void onActivityResult(int req, int _res, Intent data) {
            super.onActivityResult(req, _res, data);
            Log.d("onRes", String.format("Called with req value %d and res value %d", req, _res));
        if (req == 2 && _res == RESULT_OK) {
            try {
                Intent displayPDF = new Intent(this, PDFReader.class);
                displayPDF.putExtra("pdfURI", data.getData());
                Log.d("PDF loader", "valid pdf picked ");
                startActivity(displayPDF);
                //RENDER EACH PAGE AND PIPE IT FORWARD
            } catch (Exception ex) {
                Log.d("Activitycallback", "ERROR");
                Log.d("Activitycallback", String.format("ERROR: %s", ex.getLocalizedMessage()));
                Log.d("Activitycallback", String.format("ERROR: %s", Uri.parse(data.getDataString())));
                Log.d("Activitycallback", String.format("ERROR: %s", data.getDataString()));
            }
        }
    }

    public void DeployAsset(String fName){
        try {
            if (!getBaseContext().getFileStreamPath(fName).exists()) {
                Log.d("CONFIG", "Starting");
                AssetManager am = getAssets();
                InputStream configFile = am.open(fName);
                int configFileSize = configFile.available();
                byte[] rawData = new byte[configFileSize];
                configFile.read(rawData);
                configFile.close();
                FileOutputStream outputStream;
                outputStream = openFileOutput(fName , Context.MODE_PRIVATE);
                outputStream.write(rawData);
                outputStream.close();
            }else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}