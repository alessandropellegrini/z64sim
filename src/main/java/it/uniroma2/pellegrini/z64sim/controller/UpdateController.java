/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Dispatcher;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();
    private static UpdateController instance = null;

    private static boolean checkCompleted = false;
    private static String upstreamVersion = null;

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
            if (status >= 200 && status < 299) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(String.valueOf(response));
                upstreamVersion = (String) jsonResponse.get("name");
            }
            con.disconnect();
            log.trace(response.toString());

        } catch(IOException | ParseException e) {
            log.warn(e.getMessage());
        } finally {
            checkCompleted = true;
            Dispatcher.dispatch(Events.UPDATE_CHECK_COMPLETED);
        }
    }

    public static void init() {
        instance = new UpdateController();
        new Thread(() -> instance.getLatestVersion()).start();
    }

    public static boolean isCheckCompleted() {
        return checkCompleted;
    }

    public static String getUpstreamVersion() {
        return upstreamVersion;
    }

    public static boolean isUpdateAvailable() {
        return !PropertyBroker.getPropertyValue("z64sim.version").equals(upstreamVersion);
    }

    @Override
    public boolean dispatch(Events command) {
        return false;
    }
}
