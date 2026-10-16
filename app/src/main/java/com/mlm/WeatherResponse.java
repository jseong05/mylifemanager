package com.mlm;

import java.util.List;

public class WeatherResponse {
    public List<Weather> weather;
    public Main main;
    public String name;

    public static class Weather {
        public String main;
        public String description;
    }

    public static class Main {
        public double temp;
    }
}
