package be.kdg.banditgamesbackend.mlanalytics.adapter.out;

import be.kdg.banditgamesbackend.mlanalytics.domain.MLPredictionStats;
import be.kdg.banditgamesbackend.mlanalytics.domain.ModelStats;
import be.kdg.banditgamesbackend.mlanalytics.port.out.LoadMLPredictionsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PythonMLServiceAdapter implements LoadMLPredictionsPort {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;

    public PythonMLServiceAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MLPredictionStats loadMLPredictionStats() {
        try {
            String url = mlServiceUrl + "/api/admin/predictions/stats";
            log.info("Fetching ML prediction stats from: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("stats")) {
                log.warn("No stats found in Python ML service response");
                return new MLPredictionStats(new ArrayList<>(), null, null);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> statsArray = (List<Map<String, Object>>) response.get("stats");

            List<ModelStats> modelStatsList = new ArrayList<>();
            LocalDateTime firstPrediction = null;
            LocalDateTime lastPrediction = null;

            for (Map<String, Object> stat : statsArray) {
                String predictionType = (String) stat.get("prediction_type");
                String modelType = (String) stat.get("model_type");
                Number totalPredictions = (Number) stat.get("total_predictions");
                Number avgResponseTime = (Number) stat.get("avg_response_time_ms");

                modelStatsList.add(new ModelStats(
                    predictionType,
                    modelType,
                    totalPredictions != null ? totalPredictions.longValue() : 0L,
                    avgResponseTime != null ? avgResponseTime.doubleValue() : 0.0
                ));

                // Get first and last prediction times
                String firstPredStr = (String) stat.get("first_prediction");
                String lastPredStr = (String) stat.get("last_prediction");

                if (firstPredStr != null) {
                    LocalDateTime first = LocalDateTime.parse(firstPredStr, DateTimeFormatter.ISO_DATE_TIME);
                    if (firstPrediction == null || first.isBefore(firstPrediction)) {
                        firstPrediction = first;
                    }
                }

                if (lastPredStr != null) {
                    LocalDateTime last = LocalDateTime.parse(lastPredStr, DateTimeFormatter.ISO_DATE_TIME);
                    if (lastPrediction == null || last.isAfter(lastPrediction)) {
                        lastPrediction = last;
                    }
                }
            }

            return new MLPredictionStats(modelStatsList, firstPrediction, lastPrediction);

        } catch (RestClientException e) {
            log.error("Failed to fetch ML prediction stats from Python service at {}: {}",
                mlServiceUrl, e.getMessage());
            // Return empty stats instead of failing - ML service might not be running
            return new MLPredictionStats(new ArrayList<>(), null, null);
        } catch (Exception e) {
            log.error("Unexpected error while fetching ML prediction stats", e);
            return new MLPredictionStats(new ArrayList<>(), null, null);
        }
    }
}