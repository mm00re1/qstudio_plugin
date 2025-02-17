package com.timestored.plugins;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.JOptionPane;
import java.io.FileReader;
import java.util.logging.Logger;

public class MyCustomAuth implements DatabaseAuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(MyCustomAuth.class.getName());

    @Override
    public ConnectionDetails getonConnectionDetails(ConnectionDetails cd) {
        LOGGER.info("MyCustomAuth: getonConnectionDetails called for host: " 
                    + cd.getHost() + ":" + cd.getPort());

        // 1) Parse username to extract environment
        //    E.g. user "maurice_DEV" => environment="DEV"
        String user = cd.getUsername(); // e.g. "maurice_DEV"
        String environment = parseEnvironment(user);

        // 2) Load the JSON file and retrieve the token/password
        String tokenOrPassword = null;
        try (FileReader reader = new FileReader("C:/qstudio/creds.json")) {
            JSONObject json = new JSONObject(new JSONTokener(reader));
            tokenOrPassword = json.getString(environment); 
            LOGGER.info("Found token for environment " + environment);
        } catch (Exception e) {
            LOGGER.severe("Error reading creds file: " + e.getMessage());
            // fallback to prompt or handle error
            tokenOrPassword = JOptionPane.showInputDialog(
                "Could not read creds.json.\nEnter Password for environment '" + environment + "':");
        }

        // 3) Return new connection details with the same host/port, but new password
        ConnectionDetails newCd = new ConnectionDetails(
            cd.getHost(),
            cd.getPort(),
            cd.getDatabase(),
            user,                  // Keep the same user name
            tokenOrPassword        // Use environment-based token
        );

        LOGGER.info("MyCustomAuth: Returning new connection details.");
        return newCd;
    }

    @Override
    public String getName() {
        LOGGER.info("MyCustomAuth: getName() called.");
        return "MyCustomAuthPrompt";
    }

    /**
     * Helper method to parse environment from username (e.g. "maurice_DEV" -> "DEV").
     * Adjust if your naming convention differs.
     */
    private String parseEnvironment(String username) {
        int underscoreIndex = username.lastIndexOf('_');
        if (underscoreIndex == -1 || underscoreIndex == username.length() - 1) {
            // If there's no underscore, or underscore is last char,
            // fallback to some default environment or handle it properly
            return "DEV"; // or throw an exception
        }
        return username.substring(underscoreIndex + 1); // "DEV"
    }
}

