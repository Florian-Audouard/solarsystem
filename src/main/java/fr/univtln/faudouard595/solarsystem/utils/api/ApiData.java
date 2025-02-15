package fr.univtln.faudouard595.solarsystem.utils.api;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ApiData {
    private static String URL = "https://api.le-systeme-solaire.net/rest.php/bodies/";
    private String url;
    private int numberOfMoons;

    public ApiData createUrl(String name) {
        this.url = URL + name;
        return this;
    }

    public static ApiData convert(DataCreationNeeded data) {
        return new ApiData(URL + data.getName(), data.getNumberOfMoons());
    }
}
