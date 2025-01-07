package com.mycompany.myapp;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Container;
import com.codename1.ui.Label;
import com.codename1.ui.Display;
import com.codename1.ui.TextArea;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WeatherService {

    private static final String API_KEY = "91c830d7b8036479a7cc88408b35cd1e"; // Replace with your API key

    private static final String YOUR_API_KEY = "ZHVbYseDKB4J1UUE9AYeA7U9yih8Gfoa";

    public void fetchWeatherData(String city, Label weatherInfo) {
        String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY;

        ConnectionRequest weatherRequest = new ConnectionRequest() {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                JSONParser parser = new JSONParser();
                Map<String, Object> jsonResponse = parser.parseJSON(new InputStreamReader(input));

                Map<String, Object> main = (Map<String, Object>) jsonResponse.get("main");
                if (main != null) {
                    Double temperature = (Double) main.get("temp");
                    Double humidity = (Double) main.get("humidity");
                    List<Map<String, Object>> weatherList = (List<Map<String, Object>>) jsonResponse.get("weather");
                    String weatherDescription = weatherList != null && !weatherList.isEmpty()
                            ? weatherList.get(0).get("description").toString() : "No description available";

                    String weatherData = "Current Temperature: " + temperature + "K  \n"
                            + "Humidity: " + humidity + "%   "
                            + "Description: " + weatherDescription;
                    weatherInfo.setText(weatherData);
                } else {
                    weatherInfo.setText("Main data not found.");
                }
            }

            /*@Override
            protected void handleErrorResponseCode(int code) {
                weatherInfo.setText("Error fetching weather data. Code: " + code);
            }*/
        };

        weatherRequest.setUrl(weatherApiUrl);
        weatherRequest.setFailSilently(true);

        NetworkManager.getInstance().addToQueue(weatherRequest);
    }

    public void fetch7DayForecast(String city, Container weatherInfo) {
        String geocodeUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city.toLowerCase() + "&appid=" + API_KEY;

        weatherInfo.removeAll();

        System.out.println("geocodeUrl :- " + geocodeUrl);

        ConnectionRequest request = new ConnectionRequest() {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                JSONParser parser = new JSONParser();
                Map<String, Object> jsonResponse = parser.parseJSON(new InputStreamReader(input));

                Map<String, Object> main = (Map<String, Object>) jsonResponse.get("coord");
                if (main != null) {
                    Double lat = (Double) main.get("lat");
                    Double lon = (Double) main.get("lon");

                    fetchWeatherData(lat, lon, weatherInfo);

                } else {
                    weatherInfo.add(new Label("No Data found at here"));

                    weatherInfo.revalidate();

                }
            }

            @Override
            protected void handleException(Exception err) {
                weatherInfo.add(new Label("Error fetching coordinates: " + err.getMessage()));
            
                weatherInfo.revalidate();
                
            }
        };

        request.setUrl(geocodeUrl);
        request.setHttpMethod("GET");
        request.setFailSilently(true);
        NetworkManager.getInstance().addToQueue(request);
    }

    private void fetchWeatherData(double lat, double lng, Container weatherLabel) {
        String weatherUrl = "https://api.tomorrow.io/v4/weather/forecast?location=" + lat + "," + lng
                + "&apikey=" + YOUR_API_KEY + "&timesteps=1d&units=metric"; // Adjust 'timesteps' as needed

        System.out.println("weather url :- " + weatherUrl);

        ConnectionRequest request = new ConnectionRequest() {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                JSONParser parser = new JSONParser();
                StringBuilder jsonResponse = new StringBuilder();
                int ch;
                while ((ch = input.read()) != -1) {
                    jsonResponse.append((char) ch);
                }

                // Log the response for debugging
                System.out.println("Response: " + jsonResponse.toString());

                Map<String, Object> response = parser.parseJSON(new StringReader(jsonResponse.toString()));

                // Extract relevant weather data
                Map<String, Object> timelinesObj = (Map<String, Object>) response.get("timelines");
                if (timelinesObj != null && timelinesObj.containsKey("daily")) {
                    List<Map<String, Object>> dailyForecasts = (List<Map<String, Object>>) timelinesObj.get("daily");
                    StringBuilder weatherInfo = new StringBuilder();

                    for (Map<String, Object> dailyData : dailyForecasts) {
                        String time = (String) dailyData.get("time");
                        Map<String, Object> values = (Map<String, Object>) dailyData.get("values");

                        double temperatureAvg = (Double) values.get("temperatureAvg");
                        double humidityAvg = (Double) values.get("humidityAvg");
                        double windSpeedAvg = (Double) values.get("windSpeedAvg");

                        // Append data to the weatherInfo StringBuilder
                        /*weatherInfo.append("Date: ").append(time).append("\n")
                                .append("Average Temperature: ").append(temperatureAvg).append("°C\n")
                                .append("Average Humidity: ").append(humidityAvg).append("%\n")
                                .append("Average Wind Speed: ").append(windSpeedAvg).append(" m/s\n")
                                .append("-----------------------------\n");*/
                        weatherLabel.add(new Label("Date :- " + time));
                        weatherLabel.add(new Label("Average Temperature :- " + temperatureAvg));
                        weatherLabel.add(new Label("Average Humidity :- " + humidityAvg));
                        weatherLabel.add(new Label("Wind average Speed :- " + windSpeedAvg));
                        weatherLabel.add(new Label("-----------------------------"));

                        weatherLabel.revalidate();
                        
                    }

                    //weatherLabel.setText(weatherInfo.toString());
                } else {
                    weatherLabel.add(new Label("No daily forecasts available."));
                
                    weatherLabel.revalidate();
                    
                }

            }

            @Override
            protected void handleException(Exception err) {
                weatherLabel.add(new Label("Failed to fetch weather data: " + err.getMessage()));
            
                weatherLabel.revalidate();
                
            }
        };

        request.setUrl(weatherUrl);
        request.setHttpMethod("GET");
        request.setFailSilently(true);
        NetworkManager.getInstance().addToQueue(request);
    }

}
