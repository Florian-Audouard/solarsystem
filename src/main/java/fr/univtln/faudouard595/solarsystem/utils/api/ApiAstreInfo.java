package fr.univtln.faudouard595.solarsystem.utils.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.math.ColorRGBA;

import fr.univtln.faudouard595.solarsystem.body.Body;
import fr.univtln.faudouard595.solarsystem.body.Body.TYPE;
import fr.univtln.faudouard595.solarsystem.body.Star;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiAstreInfo {

    private static String URL = "https://api.le-systeme-solaire.net/rest.php/bodies/";
    private HttpClient client;
    private Map<String, Body> astres;;
    private JsonNode bodyJsonNode;
    private static String filePath = "src/main/resources/Data/body.json";
    private ObjectMapper mapper;
    File file;
    List<String> usedId = List.of("id", "englishName", "meanRadius", "sideralRotation", "axialTilt", "bodyType",
            "semimajorAxis", "eccentricity", "sideralOrbit", "inclination", "aroundPlanet");

    public ApiAstreInfo() {
        this.client = HttpClient.newHttpClient();
        this.astres = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.file = new File(filePath);
    }

    public void createFile(Collection<String> names) {
        log.info("Creating File");
        ObjectNode fileNode = mapper.createObjectNode();
        for (String string : names) {
            Optional<JsonNode> optionalJsonNode = createRequest(string);
            if (optionalJsonNode.isPresent()) {
                JsonNode jsonNode = optionalJsonNode.get();
                ObjectNode usedNode = mapper.createObjectNode();
                for (String id : usedId) {
                    usedNode.set(id, jsonNode.get(id));
                }
                fileNode.set(string, usedNode);
            }
        }
        try {
            mapper.writeValue(file, fileNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyBodyJsonNode(Collection<String> names) {
        return names.stream().allMatch(
                name -> bodyJsonNode.has(name) && usedId.stream().allMatch(id -> bodyJsonNode.get(name).has(id)));
    }

    public void verifFile(Collection<String> names) {
        try {
            if (!file.exists()) {
                log.info("File not found");
                createFile(names);
            }
            bodyJsonNode = mapper.readTree(file);
            if (!verifyBodyJsonNode(names)) {
                log.info("File not up to date, deleting it");
                file.delete();
                createFile(names);
            }
            bodyJsonNode = mapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Body getBodies(List<DataCreationNeeded> data, TYPE type) {
        List<String> names = data.stream().map(DataCreationNeeded::getName).toList();
        verifFile(names);
        for (DataCreationNeeded entry : data) {
            String name = entry.getName();
            ColorRGBA color = entry.getColor();
            createBody(name, TYPE.SPHERE, color);
        }
        return astres.get(names.get(0));
    }

    private Optional<JsonNode> createRequest(String name) {
        log.info("fetching data for {}", name);
        Optional<JsonNode> jsonNode = Optional.empty();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + name))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = response.body();
            return Optional.of(mapper.readTree(jsonResponse));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    public Body createBody(String name, TYPE type, ColorRGBA color) {
        JsonNode JsonNode = bodyJsonNode.get(name);
        Body body = null;

        String id = JsonNode.get("id").asText();
        String nameBody = JsonNode.get("englishName").asText();
        float size = JsonNode.get("meanRadius").floatValue();
        float rotationPeriod = JsonNode.get("sideralRotation").floatValue();
        float rotationInclination = JsonNode.get("axialTilt").floatValue();
        String bodyType = JsonNode.get("bodyType").asText();
        if (bodyType.equals("Star")) {
            body = new Star(nameBody, size, rotationPeriod, rotationInclination, type, color);
        } else {
            double semimajorAxis = JsonNode.get("semimajorAxis").doubleValue();
            float eccentricity = JsonNode.get("eccentricity").floatValue();
            float orbitalPeriod = JsonNode.get("sideralOrbit").floatValue();
            float orbitalInclination = JsonNode.get("inclination").floatValue();
            JsonNode around = JsonNode.get("aroundPlanet");
            Body ref;
            if (around.isNull()) {
                ref = astres.get("soleil");
            } else {
                ref = astres.get(around.get("planet").asText());
            }

            body = ref.addPlanet(nameBody, size, semimajorAxis, eccentricity, orbitalPeriod, rotationPeriod,
                    orbitalInclination,
                    rotationInclination, type, color);

        }
        astres.put(id, body);
        return body;
    }

}
