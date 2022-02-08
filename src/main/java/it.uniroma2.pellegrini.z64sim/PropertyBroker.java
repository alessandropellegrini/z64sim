package it.uniroma2.pellegrini.z64sim;

import java.io.IOException;
import java.util.Properties;

public class PropertyBroker {
    private static PropertyBroker instance = null;
    private final Properties properties = new Properties();

    private PropertyBroker() {
        try {
            this.properties.load(getClass().getClassLoader().getResourceAsStream("z64sim.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPropertyValue(String propertyName) {
        if(instance == null)
            instance = new PropertyBroker();
        return instance.properties.getProperty(propertyName);
    }
}
