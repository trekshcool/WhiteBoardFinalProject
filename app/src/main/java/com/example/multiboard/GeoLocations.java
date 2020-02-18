package com.example.multiboard;

import java.util.ArrayList;
import java.util.List;

//create a class that returns a list of preset locations
public class GeoLocations {

    private List<GeoLocation> locations = new ArrayList<>();

    public GeoLocations(){
       locations.add(new GeoLocation("Fuller Labs", 42.275040,-71.806377,  34.94f));
       locations.add(new GeoLocation("Atwater Kent", 42.275262,-71.807008 , 37.83f));
       locations.add(new GeoLocation("Kaven Hall", 42.274915,-71.805822,  29.00f));
       locations.add(new GeoLocation("George C. Cordon Libary", 42.274230,-71.806369, 32.53f));
    }

    public List<GeoLocation> getLocations() {
        return locations;
    }
}
    // source: https://www.mapdevelopers.com/draw-circle-tool.php?circles=%5B%5B29.88%2C42.2750396%2C-71.8063774%2C%22%23AAAAAA%22%2C%22%23000000%22%2C0.4%5D%5D
    // Fuller labs: 42.275040,-71.806377, 34.94
    // Atwater Kent: 42.275262,-71.807008 Radius: 37.83 Meters
    //Kaven Hall: Position: 42.274915,-71.805822 Radius: 29.00 Meters
    //George C. Cordon Libary:  Position: 42.274230,-71.806369 Radius: 32.53 Meters