package com.omz.pdf_transformer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.omz.pdf_transformer.FormatStorePage.formatNamesList;

public class FetchData extends AsyncTask<Void, Void, Void> {
    String data = "";
    private final Context context;
    JSONObject formatInformation;
    JSONObject formatJson;

    public FetchData (Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            URL url = new URL("http://69.43.72.249:2376/database-information");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while(line != null) {
                line = bufferedReader.readLine();
                if (line != "null")
                    data = data + line + "\n";
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        JSONArray formatNames;
        try {
            formatJson = new JSONObject(data);
            formatNames = formatJson.getJSONArray("format-names");
            for (int i = 0; i < formatNames.length(); i++) {
                formatNamesList.add((String) formatNames.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected  void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        FormatStorePage.formatNamesListAdapter = new ArrayAdapter<>(context, R.layout.format_names_listview, formatNamesList);
        FormatStorePage.formatNamesListView.setAdapter(FormatStorePage.formatNamesListAdapter);


        //on item long click, the user will be asked if user wants to delete the folder.
        FormatStorePage.formatNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String formatNameString = FormatStorePage.formatNamesListView.getItemAtPosition(position).toString();
                String filePath = context.getFilesDir().getAbsolutePath() + "/" + "format_folder";
                verifyFormatFolderExists (context);
                try {
                    String formatJsonString = formatJson.getJSONObject(formatNameString).toString();
                    String fileName = formatNameString + ".json";
                    File file = new File(filePath, fileName);
                    //stream can be used to write into the file
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(formatJsonString.getBytes());
                    stream.write("\n".getBytes());
                    stream.close();
                    Toast toast = Toast.makeText(context, "Format Downloaded!", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static void verifyFormatFolderExists(Context context) {
        File folder = new File(context.getFilesDir().getAbsolutePath() + "/" + "format_folder");
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
    }
}
