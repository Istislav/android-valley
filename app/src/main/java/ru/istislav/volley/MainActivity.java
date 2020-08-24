package ru.istislav.volley;

import androidx.appcompat.app.AppCompatActivity;

import android.app.VoiceInteractor;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private Button jsonButton;
    private Button saveButton;
    private Button saveTodoButton;
    private TextView result;
    private EditText enterMessage;

    private SharedPreferences myPrefs;
    private static final String PREFS_NAME = "myPrefsFile";

    private final static String URL = "https://api.reelgood.com/v3.0/content/people/best-of?availability=onAnySource&content_kind=both&hide_seen=false&hide_tracked=false&hide_watchlisted=false&imdb_end=10&imdb_start=0&region=us&rg_end=100&rg_start=0&sort=0&year_end=2020&year_start=1900";
    private final static String URL_String = "https://api.reelgood.com/v3.0/checkip";
    private final static String URL_EQ = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson";

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enterMessage = (EditText) findViewById(R.id.enterName);
        result = (TextView) findViewById(R.id.resulTextView);

        jsonButton = (Button) findViewById(R.id.jsonButton);
        jsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = Volley.newRequestQueue(MainActivity.this);
                getStringObject(URL_String);
                getJsonObject(URL_EQ);
                getArrayObject(URL);
            }
        });

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPrefs = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = myPrefs.edit();

                editor.putString("message", enterMessage.getText().toString());
                editor.commit();
            }
        });


        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        if(prefs.contains("message")) {
            String message = prefs.getString("message", "not found");
            result.setText("Message: " +  message);
        }


        saveTodoButton = (Button) findViewById(R.id.saveTodoButton);
        saveTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = enterMessage.getText().toString();
                if (message.equals("")) {

                } else {
                    writeToFile(message);
                }
            }
        });

        String msg = null;
        try {
            msg = readFromFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (msg != null) {
            enterMessage.setText(msg);
        }
    }

    public void getJsonObject(String url) {
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Object string: ", response.getString("type").toString());
                    JSONObject metadata = response.getJSONObject("metadata");
                    Log.d("Object subobject", metadata.toString());
                    Log.d("Object subobject Str", metadata.getString("title").toString());

                    JSONArray features = response.getJSONArray("features");
                    JSONObject propObj = features.getJSONObject(0).getJSONObject("properties");
                    Log.d("Object arr Str", propObj.getString("place").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", error.getMessage());
            }
        });
        queue.add(objectRequest);
    }

    public void getArrayObject(String url) {
        // because in the URL it is array, not object, else you need to use JsonObjectRequest
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.d("Response", response.toString());
                for (int i = 0; i<response.length(); i++) {
                    try {
                        JSONObject movieObject = response.getJSONObject(i);
                        Log.d("Items: ", movieObject.getString("name")); //name is the string property of the each object in the json-array, that we have got
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", error.getMessage());
            }
        });
        queue.add(arrayRequest);
    }

    public void getStringObject(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("My String: ", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void writeToFile(String message) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("todoList.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(message);
            outputStreamWriter.close(); // Don't forget to close your stream!
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFromFile() throws IOException {
        String result = "";

        InputStream inputStream = openFileInput("todoList.txt");

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String tempString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ( (tempString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(tempString).append("\n");
            }
            inputStream.close();
            result = stringBuilder.toString();
        }

        return result;
    }
}