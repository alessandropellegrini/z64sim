package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();
    private static UpdateController instance = null;

    private static final String connectionUrl = "https://api.github.com/repos/alessandropellegrini/z64sim/releases/latest";

    private UpdateController() {
    }

    private void getLatestVersion() {
        log.trace(PropertyBroker.getMessageFromBundle("update.checking"));

        try {
            URL url = new URL(connectionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("accept", "application/vnd.github+json");
            con.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

            int status = con.getResponseCode();

            StringBuilder response = new StringBuilder();
            if (status > 200 && status < 299) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            con.disconnect();
            log.trace(response.toString());

        } catch(IOException e) {
            log.warn(e.getMessage());
        }
    }

    public static void init() {
        instance = new UpdateController();
        new Thread(() -> instance.getLatestVersion()).start();
    }

    @Override
    public boolean dispatch(Events command) {
        return false;
    }
}
