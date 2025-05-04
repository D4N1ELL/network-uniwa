import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

class CityClass {
    private int ref;
    private String cityLaNom;
    private String cityLaAcc;
    private String cityLaGen;
    private String cityLaDat;
    private String cityLaVoc;
    private String cityLaGender;
    private String cityEnNom;
    private String cityState;

    public CityClass(int ref, String cityLaNom, String cityLaAcc, String cityLaGen, String cityLaDat, String cityLaVoc, String cityLaGender, String cityEnNom, String cityState) {
        this.ref = ref;
        this.cityLaNom = cityLaNom;
        this.cityLaAcc = cityLaAcc;
        this.cityLaGen = cityLaGen;
        this.cityLaDat = cityLaDat;
        this.cityLaVoc = cityLaVoc;
        this.cityLaGender = cityLaGender;
        this.cityEnNom = cityEnNom;
        this.cityState = cityState;
    }

    public int getRef() {
        return ref;
    }

    public String getCityEnNom() {
        return cityEnNom;
    }

    @Override
    public String toString() {
        return "Ref: " + ref + ", City La Nom: " + cityLaNom + ", City La Acc: " + cityLaAcc + ", City La Gen: " + cityLaGen
                + ", City La Dat: " + cityLaDat + ", City La Voc: " + cityLaVoc + ", Gender: " + cityLaGender + ", English Name: " + cityEnNom
                + ", State: " + cityState;
    }
}

class EntityClass {
    private String entityLaNom;
    private String entityLaAcc;
    private String entityLaGen;
    private String entityLaDat;
    private String entityLaVoc;
    private String entityGender;
    private int cityRef;
    private String type;

    public EntityClass(String entityLaNom, String entityLaAcc, String entityLaGen, String entityLaDat,
                       String entityLaVoc, String entityGender, int cityRef, String type) {
        this.entityLaNom = entityLaNom;
        this.entityLaAcc = entityLaAcc;
        this.entityLaGen = entityLaGen;
        this.entityLaDat = entityLaDat;
        this.entityLaVoc = entityLaVoc;
        this.entityGender = entityGender;
        this.cityRef = cityRef;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getCityRef() {
        return cityRef;
    }

    @Override
    public String toString() {
        return "Entity La Nom: " + entityLaNom + ", Entity La Acc: " + entityLaAcc + ", Entity La Gen: " + entityLaGen
                + ", Entity La Dat: " + entityLaDat + ", Entity La Voc: " + entityLaVoc
                + ", Gender: " + entityGender + ", City Ref: " + cityRef + ", Type: " + type;
    }
}

public class Main {
    private static ArrayList<CityClass> ALC = new ArrayList<>();
    private static ArrayList<EntityClass> ALE = new ArrayList<>();

    public static void main(String[] args) {
        loadCities("src/cities.txt");
        loadEntities("src/namedEntities.txt");

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running on port 12345...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            String query = in.readLine();
            if (query.startsWith("City ")) {
                String cityName = query.substring(5).trim();
                String response = searchCity(cityName);
                out.println(response);
            } else {
                String[] parts = query.split(" ", 2);
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    String cityName = parts[1].trim();
                    String response = searchEntities(type, cityName);
                    out.println(response);
                } else {
                    out.println("Invalid query format.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private static String searchCity(String cityName) {
        for (CityClass city : ALC) {
            if (city.getCityEnNom().equalsIgnoreCase(cityName)) {
                return city.toString();
            }
        }
        return "City not found.";
    }

    private static String searchEntities(String type, String cityName) {
        CityClass matchingCity = null;
        for (CityClass city : ALC) {
            if (city.getCityEnNom().equalsIgnoreCase(cityName)) {
                matchingCity = city;
                break;
            }
        }

        if (matchingCity == null) {
            return "City not found.";
        }

        int cityRef = matchingCity.getRef();
        StringBuilder response = new StringBuilder();
        for (EntityClass entity : ALE) {
            if (entity.getType().equalsIgnoreCase(type) && entity.getCityRef() == cityRef) {
                response.append(entity).append("\n");
            }
        }

        return response.length() > 0 ? response.toString() : "No matching entities found for the specified city and type.";
    }

    private static void loadCities(String filename) {
        System.out.println("Loading cities from: " + filename);
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("#");
                if (parts.length == 9) {
                    int ref = Integer.parseInt(parts[0].trim());
                    String cityLaNom = parts[1].trim();
                    String cityLaAcc = parts[2].trim();
                    String cityLaGen = parts[3].trim();
                    String cityLaDat = parts[4].trim();
                    String cityLaVoc = parts[5].trim();
                    String cityLaGender = parts[6].trim();
                    String cityEnNom = parts[7].trim();
                    // Adjusted cityState parsing
                    String cityState = parts[8].trim();
                    ALC.add(new CityClass(ref, cityLaNom, cityLaAcc, cityLaGen, cityLaDat, cityLaVoc, cityLaGender, cityEnNom, cityState));
                    System.out.println("Loaded city: " + cityEnNom + " with Ref: " + ref);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading cities file: " + e.getMessage());
        }
    }

    private static void loadEntities(String filename) {
        System.out.println("[DEBUG] Starting to load named entities from file: " + filename);
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[DEBUG] Reading line: " + line);
                String[] parts = line.split("#");
                if (parts.length >= 8) {
                    try {
                        String entityLaNom = parts[0].trim();
                        String entityLaAcc = parts[1].trim();
                        String entityLaGen = parts[2].trim();
                        String entityLaDat = parts[3].trim();
                        String entityLaVoc = parts[4].trim();
                        String entityGender = parts[5].trim();
                        int cityRef = Integer.parseInt(parts[6].trim());
                        String type = parts[7].trim();
                        ALE.add(new EntityClass(entityLaNom, entityLaAcc, entityLaGen, entityLaDat, entityLaVoc, entityGender, cityRef, type));
                        System.out.println("[DEBUG] Successfully loaded entity: " + entityLaNom + ", Type: " + type + ", CityRef: " + cityRef);
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid number format in line: " + line);
                    }
                } else {
                    System.out.println("[ERROR] Invalid format in line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading named entities file: " + e.getMessage());
        }
    }
}

