package org.kaelth4s.castlekeeper.bot.client;

import org.kaelth4s.castlekeeper.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CastleKeeperApiClient {

    private final RestClient restClient;

    public CastleKeeperApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // ---------- Materials ----------
    public List<MaterialResponse> getMaterials() { return getList("/materials", new ParameterizedTypeReference<List<MaterialResponse>>() {}); }
    public MaterialResponse getMaterial(Long id) { return get("/materials/{id}", MaterialResponse.class, id); }
    public MaterialResponse createMaterial(MaterialRequest r) { return post("/materials", r, MaterialResponse.class); }
    public MaterialResponse updateMaterial(Long id, MaterialRequest r) { return put("/materials/{id}", r, MaterialResponse.class, id); }
    public void deleteMaterial(Long id) { delete("/materials/{id}", id); }

    // ---------- Authors ----------
    public List<AuthorResponse> getAuthors() { return getList("/authors", new ParameterizedTypeReference<List<AuthorResponse>>() {}); }
    public AuthorResponse getAuthor(Long id) { return get("/authors/{id}", AuthorResponse.class, id); }
    public AuthorResponse createAuthor(AuthorRequest r) { return post("/authors", r, AuthorResponse.class); }
    public AuthorResponse updateAuthor(Long id, AuthorRequest r) { return put("/authors/{id}", r, AuthorResponse.class, id); }
    public void deleteAuthor(Long id) { delete("/authors/{id}", id); }

    // ---------- Author Types ----------
    public List<AuthorTypeResponse> getAuthorTypes() { return getList("/author-types", new ParameterizedTypeReference<List<AuthorTypeResponse>>() {}); }
    public AuthorTypeResponse getAuthorType(Long id) { return get("/author-types/{id}", AuthorTypeResponse.class, id); }
    public AuthorTypeResponse createAuthorType(AuthorTypeRequest r) { return post("/author-types", r, AuthorTypeResponse.class); }
    public AuthorTypeResponse updateAuthorType(Long id, AuthorTypeRequest r) { return put("/author-types/{id}", r, AuthorTypeResponse.class, id); }
    public void deleteAuthorType(Long id) { delete("/author-types/{id}", id); }

    // ---------- Castles ----------
    public List<CastleResponse> getCastles() { return getList("/castles", new ParameterizedTypeReference<List<CastleResponse>>() {}); }
    public CastleResponse getCastle(Long id) { return get("/castles/{id}", CastleResponse.class, id); }
    public CastleResponse getRandomCastle() { return get("/castles/random", CastleResponse.class); }
    public CastleResponse createCastle(CastleRequest r) { return post("/castles", r, CastleResponse.class); }
    public CastleResponse updateCastle(Long id, CastleRequest r) { return put("/castles/{id}", r, CastleResponse.class, id); }
    public void deleteCastle(Long id) { delete("/castles/{id}", id); }

    // ---------- Reconstructions ----------
    public List<ReconstructionResponse> getReconstructions() { return getList("/reconstructions", new ParameterizedTypeReference<List<ReconstructionResponse>>() {}); }
    public ReconstructionResponse getReconstruction(Long id) { return get("/reconstructions/{id}", ReconstructionResponse.class, id); }
    public ReconstructionResponse createReconstruction(ReconstructionRequest r) { return post("/reconstructions", r, ReconstructionResponse.class); }
    public ReconstructionResponse updateReconstruction(Long id, ReconstructionRequest r) { return put("/reconstructions/{id}", r, ReconstructionResponse.class, id); }
    public void deleteReconstruction(Long id) { delete("/reconstructions/{id}", id); }

    // ---------- Helpers ----------
    private <T> T get(String uri, Class<T> clazz, Object... vars) { return restClient.get().uri(uri, vars).retrieve().body(clazz); }
    private <T> T getList(String uri, ParameterizedTypeReference<T> ref, Object... vars) { return restClient.get().uri(uri, vars).retrieve().body(ref); }
    private <T> T post(String uri, Object body, Class<T> clazz, Object... vars) { return restClient.post().uri(uri, vars).body(body).retrieve().body(clazz); }
    private <T> T put(String uri, Object body, Class<T> clazz, Object... vars) { return restClient.put().uri(uri, vars).body(body).retrieve().body(clazz); }
    private void delete(String uri, Object... vars) { restClient.delete().uri(uri, vars).retrieve().toBodilessEntity(); }
}
