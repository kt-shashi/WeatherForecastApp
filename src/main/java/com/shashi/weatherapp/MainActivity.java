package com.shashi.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText cityEditText;
    Button getForecastButton;

    TextView weatherTextView;
    TextView descriptionTextView;
    TextView tempTextView;
    TextView humidityTextView;

    TextView cityNameTextView;

    private final String API_KEY = "your_api_here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

    }

    private void initViews() {

        cityEditText = findViewById(R.id.city_edit_text);
        getForecastButton = findViewById(R.id.button_get_data);

        cityNameTextView = findViewById(R.id.city_name_text_view);

        weatherTextView = findViewById(R.id.weather_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        tempTextView = findViewById(R.id.temp_text_view);
        humidityTextView = findViewById(R.id.humidity_text_view);

        getForecastButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        String cityName = cityEditText.getText().toString().trim().toLowerCase();
        if (cityName.isEmpty()) {
            cityEditText.setError("Field cannot be empty");
            return;
        }

        cityEditText.setText("");
        weatherTextView.setText("");
        descriptionTextView.setText("");
        tempTextView.setText("");
        humidityTextView.setText("");

        cityNameTextView.setText("Showing results for: " + cityName);

        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY;

        GetWeather getWeather = new GetWeather();
        getWeather.execute(urlString);

    }

    public class GetWeather extends AsyncTask<String, Void, WeatherDataModel> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected WeatherDataModel doInBackground(String... urls) {

            String fetchedData = "";
            WeatherDataModel dataModel = null;

            try {

                //Open Connection
                URL myUrl = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) myUrl.openConnection();
                httpURLConnection.setRequestMethod("GET");

                //Check for proper response
                int code = httpURLConnection.getResponseCode();
                if (code != 200) {
                    Log.d("debug_shashi", "Error! Status code: " + code);
                    return null;
                }

                //Get Data
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String line = "";
                while (line != null) {
                    line = br.readLine();
                    fetchedData += line;
                }

                //Parsing JSON data
                JSONObject root = new JSONObject(fetchedData);

                JSONArray weather = root.getJSONArray("weather");
                JSONObject weatherData = weather.getJSONObject(0);

                String mainDescrption = weatherData.getString("main");
                String secondaryDescription = weatherData.getString("description");

                JSONObject temperatureData = root.getJSONObject("main");

                String temperature = temperatureData.getString("temp");
                String humidity = temperatureData.getString("humidity");

                //Saving the data to our DATA MODEL CLASS
                dataModel = new WeatherDataModel();
                dataModel.setWeather(mainDescrption);
                dataModel.setDescription(secondaryDescription);
                dataModel.setTemperature(temperature);
                dataModel.setHumidity(humidity);

            } catch (Exception e) {
                Log.d("debug_shashi", "Error! ");
                return null;
            }

            return dataModel;
        }

        @Override
        protected void onPostExecute(WeatherDataModel dataModel) {

            if (dataModel == null) {
                cityNameTextView.setText("Unexpected Error!");
                return;
            }

            //convert Kelvin to Celsius
            String kelvin = dataModel.getTemperature();
            double celsius = Double.parseDouble(kelvin) - 273.15;
            String celsiusString = String.format("%.2f", celsius);

            //set data to views
            weatherTextView.setText("Weather: " + dataModel.getWeather());
            descriptionTextView.setText("Description: " + dataModel.getDescription());
            tempTextView.setText("Temperature: " + celsiusString + "°C");
            humidityTextView.setText("Humidity: " + dataModel.getHumidity());

        }
    }

    //To create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.contact_developer) {
            ConstraintLayout constraintLayout = findViewById(R.id.constraint_layout);
            Snackbar snackbar = Snackbar.make(constraintLayout, "Developer: Shashi", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        return super.onOptionsItemSelected(item);
    }
}