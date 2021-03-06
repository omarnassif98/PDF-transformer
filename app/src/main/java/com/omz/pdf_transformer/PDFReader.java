//TEST
package com.omz.pdf_transformer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class PDFReader extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_view);
        Intent intent = getIntent();
        final Uri pdfURI = intent.getParcelableExtra("pdfURI");
        final TextView pageView = findViewById(R.id.documentView);
        final TextView pdfPageNumberView = findViewById(R.id.pdfPageNumberView);
        Toolbar myToolbar = findViewById(R.id.toolbar_pdfview);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PDFroggy++");
        AssetManager am = getAssets();
        try {
            InputStream configFile = getBaseContext().openFileInput("ReaderViewPreference.json");
            int configFileSize = configFile.available();
            byte[] rawData = new byte[configFileSize];
            configFile.read(rawData);
            configFile.close();
            final JSONObject configJSON = new JSONObject(new String(rawData, "UTF-8"));
            Log.d("READERPREFS", configJSON.toString());
            PDFContentManager.singleton.LoadSpanPreferences(configJSON);
            final int[] pageNumber = {0};
            ImageButton previousPageBtn = findViewById(R.id.previousPageBtn);
            PDFContentManager.singleton.ScrapePDF(pageView, pdfURI, getContentResolver(), pageNumber[0]);
            previousPageBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PDFContentManager.singleton.Decomission();
                    PDFContentManager.singleton.LoadSpanPreferences(configJSON);
                    pageNumber[0]--;
                    PDFContentManager.singleton.ScrapePDF(pageView, pdfURI, getContentResolver(), pageNumber[0]);

                    pdfPageNumberView.setText(String.valueOf(pageNumber[0]));
                }
            });

            ImageButton nextPageBtn = findViewById(R.id.nextPageBtn);
            nextPageBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PDFContentManager.singleton.Decomission();
                    PDFContentManager.singleton.LoadSpanPreferences(configJSON);
                    pageNumber[0]++;
                    PDFContentManager.singleton.ScrapePDF(pageView, pdfURI, getContentResolver(), pageNumber[0]);

                    pdfPageNumberView.setText(String.valueOf(pageNumber[0]));
                }
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed(){
        PDFContentManager.singleton.Decomission();
        Log.d("DECOMISSIONED", "Decommisioned");
        super.onBackPressed();
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
        if (id == R.id.action_format_store) {
            Intent intent = new Intent(this, FormatStorePage.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}


