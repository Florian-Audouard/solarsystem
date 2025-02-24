package fr.univtln.faudouard595.solarsystem.utils.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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
public class ApiBodyInfo {

    private HttpClient client;
    private Map<String, Body> astres;;
    private JsonNode bodyJsonNode;
    private static String filePath = "src/main/resources/Data/body.json";
    private ObjectMapper mapper;
    private ObjectNode fileNode;
    File file;
    List<String> usedId = List.of("id", "englishName", "meanRadius", "sideralRotation", "axialTilt", "bodyType",
            "semimajorAxis", "eccentricity", "sideralOrbit", "inclination", "orbitAround", "longAscNode",
            "argPeriapsis", "mainAnomaly", "perihelion");

    public ApiBodyInfo() {
        this.client = HttpClient.newHttpClient();
        this.astres = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.file = new File(filePath);
    }

    public void fillFileNodeList(Collection<ApiData> urls) {
        for (ApiData url : urls) {
            Optional<JsonNode> optionalJsonNode = createRequest(url.getUrl());
            if (optionalJsonNode.isPresent()) {
                JsonNode jsonNode = optionalJsonNode.get();
                ObjectNode usedNode = mapper.createObjectNode();
                for (String id : usedId) {
                    usedNode.set(id, jsonNode.get(id));
                }
                fileNode.set(jsonNode.get("id").asText(), usedNode);
                JsonNode moons = jsonNode.get("moons");
                if (!moons.isNull()) {
                    Collection<ApiData> moonNames = moons.findValuesAsText("rel").stream().limit(url.getNumberOfMoons())
                            .map(
                                    name -> new ApiData(name, 0))
                            .toList();
                    fillFileNodeList(moonNames);
                }
                JsonNode aroundPlanet = jsonNode.get("aroundPlanet");
                String ref = "soleil";
                if (!aroundPlanet.isNull()) {
                    ref = aroundPlanet.get("planet").asText();
                }
                if (jsonNode.get("id").asText().equals("soleil")) {
                    ref = null;
                }
                usedNode.put("orbitAround", ref);
            }
        }

    }

    public void createFile(Collection<DataCreationNeeded> data) {
        log.info("Creating File");
        fileNode = mapper.createObjectNode();
        Collection<ApiData> urlName = data.stream()
                .map(ApiData::convert)
                .toList();
        fillFileNodeList(urlName);
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

    public void verifFile(Collection<DataCreationNeeded> data) {
        try {
            if (!file.exists()) {
                log.info("File not found");
                createFile(data);
            }
            bodyJsonNode = mapper.readTree(file);
            List<String> namesList = data.stream().map(DataCreationNeeded::getName).toList();
            if (!verifyBodyJsonNode(namesList)) {
                log.info("File not up to date, deleting it");
                file.delete();
                createFile(data);
            }
            bodyJsonNode = mapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Body getBodies(List<DataCreationNeeded> data, TYPE type) {
        verifFile(data);
        for (DataCreationNeeded entry : data) {
            String name = entry.getName();
            ColorRGBA color = entry.getColor();
            createBody(name, entry.isRing(), TYPE.SPHERE, color);
        }
        List<String> remaining = bodyJsonNode.findValuesAsText("id").stream()
                .filter(id -> !astres.containsKey(id))
                .toList();
        remaining.forEach(id -> createBody(id, false, type, null));
        return astres.get(data.get(0).getName());
    }

    private Optional<JsonNode> createRequest(String url) {
        log.info("fetching data for {}", url);
        Optional<JsonNode> jsonNode = Optional.empty();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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

    public Body createBody(String name, boolean ring, TYPE type, ColorRGBA color) {
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
            float longAscNode = JsonNode.get("longAscNode").floatValue();
            float argPeriapsis = JsonNode.get("argPeriapsis").floatValue();
            float mainAnomaly = JsonNode.get("mainAnomaly").floatValue();
            Body ref = astres.get(JsonNode.get("orbitAround").asText());
            if (color == null) {
                color = ref.getColor();
            }
            body = ref.addPlanet(nameBody, size, semimajorAxis, eccentricity, orbitalPeriod, rotationPeriod,
                    orbitalInclination,
                    rotationInclination, longAscNode, argPeriapsis, mainAnomaly, type, color, ring);
        }
        astres.put(id, body);
        return body;
    }

}
