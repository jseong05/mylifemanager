package com.mlm;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherClient {
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;

    public interface WeatherCallback {
        void onSuccess(String weatherInfo);
        void onError(String error);
    }

    public static void fetchWeatherByCity(String city, WeatherCallback callback) {
        String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=kr&appid=%s", city, API_KEY);
        requestWeather(urlString, callback);
    }

    public static void fetchWeatherByLocation(double lat, double lon, WeatherCallback callback) {
        String urlString = String.format(Locale.US, "https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&units=metric&lang=kr&appid=%s", lat, lon, API_KEY);
        requestWeather(urlString, callback);
    }

    // 앱이 살아있는 동안 계속 재사용하는 워커 스레드 (매 요청마다 새로 안 만듦)
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static void requestWeather(String urlString, WeatherCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());

        EXECUTOR.execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Gson gson = new Gson();
                    WeatherResponse weatherResponse = gson.fromJson(response.toString(), WeatherResponse.class);
                    if (weatherResponse != null && weatherResponse.main != null && weatherResponse.weather != null && !weatherResponse.weather.isEmpty()) {
                        String mainWeather = weatherResponse.weather.get(0).main;
                        String emoji = getWeatherEmoji(mainWeather);
                        String cityName = translateCityName(weatherResponse.name);
                        String desc = weatherResponse.weather.get(0).description;

                        String result = String.format(Locale.getDefault(), "%s %s %.1f°C · %s", 
                                emoji, 
                                cityName, 
                                weatherResponse.main.temp, 
                                desc);
                        handler.post(() -> callback.onSuccess(result));
                    } else {
                        handler.post(() -> callback.onError("날씨 파싱 오류"));
                    }
                } else {
                    handler.post(() -> callback.onError("API 오류 (" + responseCode + ")"));
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError("네트워크 오류: " + e.getMessage()));
            }
        });
    }

    private static String getWeatherEmoji(String main) {
        if (main == null) return "🌤️";
        switch (main.toLowerCase(Locale.ROOT)) {
            case "clear": return "☀️";
            case "clouds": return "🌤️";
            case "rain":
            case "drizzle": return "🌧️";
            case "thunderstorm": return "⛈️";
            case "snow": return "❄️";
            case "mist":
            case "fog":
            case "haze": return "🌫️";
            default: return "🌤️";
        }
    }

    private static String translateCityName(String name) {
        if (name == null) return "내 위치";
        switch (name.toLowerCase(Locale.ROOT)) {
            case "seoul": return "서울";
            case "busan": return "부산";
            case "incheon": return "인천";
            case "daegu": return "대구";
            case "daejeon": return "대전";
            case "gwangju": return "광주";
            case "ulsan": return "울산";
            case "suwon-si":
            case "suwon": return "수원";
            case "jeju-do":
            case "jeju": return "제주";
            case "sejong": return "세종";
            default: return name;
        }
    }
}
