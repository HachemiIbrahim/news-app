package com.example.newsreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("url", urls.get(i));
                startActivity(intent);
            }
        });
        listView.setAdapter(arrayAdapter);

        arrayAdapter.notifyDataSetChanged();


        DownloadTask task = new DownloadTask();

        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result = result + current;
                    data = reader.read();
                }

                JSONArray jsonArray = new JSONArray(result);
                int nbrItems = 30;
                if (nbrItems > jsonArray.length()) {
                    nbrItems = jsonArray.length();
                }

                for (int i = 0; i < nbrItems; i++) {
                    String articlId = jsonArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articlId + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();
                    String articlInfo = "";
                    while (data != -1) {
                        char current = (char) data;
                        articlInfo = articlInfo + current;
                        data = reader.read();
                    }
                    JSONObject jsonObject = new JSONObject(articlInfo);
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {

                        String articlTitle = jsonObject.getString("title");
                        String articlUrl = jsonObject.getString("url");
                        titles.add(articlTitle);
                        urls.add(articlUrl);
                        Log.i("title :", articlTitle);
                        Log.i("URL :", articlUrl);

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            arrayAdapter.notifyDataSetChanged();
        }
    }

}