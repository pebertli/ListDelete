package com.pebertli.listdelete.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import com.pebertli.listdelete.R;
import com.pebertli.listdelete.adapters.CountriesAdapter;
import com.pebertli.listdelete.helper.SwipeRowHelper;
import com.pebertli.listdelete.models.Country;

public class MainActivity extends AppCompatActivity {

    private CountriesAdapter mAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Country> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        list = new ArrayList<Country>();



        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Animation spin = AnimationUtils.loadAnimation(this, R.anim.spin_center);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //clean the list and try to populate again

                AsyncTask asyncTask = new AsyncTask<Void, Void, Integer>()
                {
                    protected void onPreExecute() {
                        fab.startAnimation(spin);
                        recyclerView.setEnabled(false);
                        resetList();
                    }

                    @Override
                    protected Integer doInBackground(Void... urls) {

                        try {
                            URL url = new URL("https://restcountries.eu/rest/v2/all");
                            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                            int responseCode = urlConnection.getResponseCode();
                            if(responseCode == 200)
                            {
                                try //try parse the json to country model
                                {
                                    BufferedReader in = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
                                    StringBuilder stringBuilder = new StringBuilder();
                                    String line;
                                    while ((line = in.readLine()) != null)
                                    {
                                        stringBuilder.append(line).append("\n");
                                    }
                                    in.close();

                                    list = parserJson(stringBuilder.toString());

                                } catch (Exception e)
                                {
                                    return -1;
                                } finally
                                {
                                    urlConnection.disconnect();
                                }
                            }
                                return responseCode;
                        }
                        catch (Exception e) {
                            return -1;
                        }
                    }

                    @Override
                    protected void onPostExecute(Integer respondeCode) {
                        fab.clearAnimation();
                        if(respondeCode == 200)
                        {
                            initList();
                            recyclerView.setEnabled(true);
                        }
                        else
                        {
                            Toast.makeText(recyclerView.getContext(), "Could not load data", Toast.LENGTH_LONG).show();
                        }
                    }

                }.execute();
            }
        });
    }

    private void resetList()
    {
        int size = list.size();
        list.clear();
        if(mAdapter != null)
            mAdapter.notifyItemRangeRemoved(0, size);
    }

    private void initList()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        metrics = getResources().getDisplayMetrics();
        //send the metrics in dp
        SwipeRowHelper helper = new SwipeRowHelper(recyclerView, list, metrics.density);

        mAdapter = new CountriesAdapter(helper);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setTextViewEmpty(findViewById(R.id.textViewEmpty));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public ArrayList<Country> parserJson(String jsonString)
    {

        ArrayList<Country> result = new ArrayList<Country>();

        if(jsonString != null) {
            try {
                // Getting JSON Root Array node
                JSONArray countries = new JSONArray(jsonString);

                // looping through All countries, but get only name, language and currency
                for (int i = 0; i < countries.length(); i++) {
                    JSONObject c = countries.getJSONObject(i);
                    String name = c.getString("name");

                    JSONArray currencies = c.getJSONArray("currencies");
                    String currencyName = "Not Informed";
                    if (currencies.length() > 0) {//if there are currencies, get only the first
                        JSONObject currency = (JSONObject) currencies.get(0);
                        currencyName = currency.getString("name");
                    }

                    JSONArray languages = c.getJSONArray("languages");
                    String languageName = "Not Informed";
                    if (languages.length() > 0) {//if there are languages, get only the first
                        JSONObject language = (JSONObject) languages.get(0);
                        languageName = language.getString("name");
                    }

                    Country country = new Country();
                    country.setName(name);
                    country.setCurrency(currencyName);
                    country.setLanguage(languageName);

                    // adding contact to contact list
                    result.add(country);
                }
            } catch (final JSONException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        return result;
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
