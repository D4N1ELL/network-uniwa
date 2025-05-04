import java.io.*;
import java.net.*;
import java.util.*;

public class ServerApp {

    private static final int SERVER_PORT = 12345;
    private static final ArrayList<City> ALC = new ArrayList<>();
    private static final ArrayList<NamedEntity> ALE = new ArrayList<>();

    public static void main(String[] args) {
        // 1. Load data from files
        loadCities("cities1.txt");
        loadNamedEntities("data/namedEntities.txt");

        // 2. Start the server
        startServer();
    }

    private static void loadCities(String filename) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Format:
                // Ref#city_ro_nom#city_ro_acc#city_ro_gen#city_ro_dat#city_en_gender#city_en_nom#city_state
                String[] tokens = line.split("#");
                int ref = Integer.parseInt(tokens[0]);
                String cityRoNom = tokens[1];
                String cityRoAcc = tokens[2];
                String cityRoGen = tokens[3];
                String cityRoDat = tokens[4];
                String cityEnGender = tokens[5];
                String cityEnNom = tokens[6];
                String cityState = (tokens.length > 7) ? tokens[7] : "";

                City cityObj = new City(ref, cityRoNom, cityRoAcc, cityRoGen,
                        cityRoDat, cityEnGender, cityEnNom, cityState);
                ALC.add(cityObj);
            }
            System.out.println("Loaded " + ALC.size() + " cities.");
        } catch (IOException e) {
        }
    }

    private static void loadNamedEntities(String filename) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Format:
                // ent_ro_nom#ent_ro_acc#ent_ro_gen#ent_ro_dat#ent_en_gender#city_ref#type
                String[] tokens = line.split("#");
                String entRoNom = tokens[0];
                String entRoAcc = tokens[1];
                String entRoGen = tokens[2];
                String entRoDat = tokens[3];
                String entEnGender = tokens[4];

                int cityRef = Integer.parseInt(tokens[5]);
                String type = tokens[6];

                NamedEntity entityObj = new NamedEntity(
                        entRoNom, entRoAcc, entRoGen, entRoDat, entEnGender,
                        cityRef, type
                );
                ALE.add(entityObj);
            }
            System.out.println("Loaded " + ALE.size() + " named entities.");
        } catch (IOException e) {
        }
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server listening on port " + SERVER_PORT);

            while (true) {
                Socket incoming = serverSocket.accept();
                // For multiple clients, spawn a new thread (or use a thread pool).
                handleClient(incoming);
            }
        } catch (IOException e) {
        }
    }

    private static void handleClient(Socket incoming) {
        new Thread(() -> {
            try (
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(incoming.getInputStream(), "UTF-8")
                    ); PrintStream out = new PrintStream(
                    incoming.getOutputStream(), true, "UTF-8"
            )) {
                out.println("Welcome to the Moldova Info Service. "
                        + "Type 'City Chisinau' or 'Sights Balti', etc. Type 'exit' to quit.");

                String request;
                while ((request = in.readLine()) != null) {
                    request = request.trim();
                    if (request.equalsIgnoreCase("exit")) {
                        out.println("Goodbye!");
                        break;
                    }
                    String response = processRequest(request);
                    out.println(response);
                }
            } catch (IOException e) {
            }
        }).start();
    }

    private static String processRequest(String request) {
        // Example: "City Chisinau" or "Sights Balti"
        String[] parts = request.split("\\s+");
        if (parts.length < 2) {
            return "Invalid format. Try: 'City Balti' or 'Hospitals Cahul'.";
        }

        String keyword = parts[0]; // "City" or "Hospitals"/"Sights"/"Universities"/"Airports" etc.
        String cityName = parts[1]; // For single-word city names. Adapt for multi-word if needed.

        if (keyword.equalsIgnoreCase("City")) {
            // Show details of the city
            for (City c : ALC) {
                if (c.getCityEnNom().equalsIgnoreCase(cityName)) {
                    return c.getAllAttributes();
                }
            }
            return "No city found with name: " + cityName;
        } else {
            // We assume it's a named-entity type
            // 1) Find the city ref
            int cityRef = -1;
            for (City c : ALC) {
                if (c.getCityEnNom().equalsIgnoreCase(cityName)) {
                    cityRef = c.getRef();
                    break;
                }
            }
            if (cityRef == -1) {
                return "City not found: " + cityName;
            }

            // 2) Look for named entities of that type in that city
            StringBuilder sb = new StringBuilder();
            for (NamedEntity e : ALE) {
                if (e.getCityRef() == cityRef && e.getType().equalsIgnoreCase(keyword)) {
                    sb.append(e.getAllAttributes()).append("\n");
                }
            }
            if (sb.length() == 0) {
                return "No " + keyword + " found for city " + cityName;
            }
            return sb.toString().trim();
        }
    }
}