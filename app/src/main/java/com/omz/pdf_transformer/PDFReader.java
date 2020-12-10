//TEST
package com.omz.pdf_transformer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
        Uri pdfURI = intent.getParcelableExtra("pdfURI");
        TextView pageView = findViewById(R.id.documentView);
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
            JSONObject configJSON = new JSONObject(new String(rawData, "UTF-8"));
            Log.d("READERPREFS", configJSON.toString());
            PDFContentManager.singleton.LoadSpanPreferences(configJSON);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        PDFContentManager.singleton.ScrapePDF(pageView, pdfURI, getContentResolver(), getResources().getDisplayMetrics().density);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
