import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;


@Service
public class SirenFederateService {

    @Value("${elasticsearch.url}")
    private String elasticsearchUrl;

    @Autowired
    private final RestTemplate restTemplate;

   

    public String getSirenFederateQueryResponse(String[] indices, String[][] filters, String[] fields) {
        String url = elasticsearchUrl + "/_search";

        // Construct the request body
        /**String requestJson = "{\n" +
                "  \"query\": {\n" +
                "    \"type\": \"join\",\n" +
                "    \"indices\": [\"employees\", \"departments\", \"worklog\"],\n" +
                "    \"filters\": [\n" +
                "      {\n" +
                "        \"source\": \"employees\",\n" +
                "        \"destination\": \"departments\",\n" +
                "        \"source_field\": \"department_id\",\n" +
                "        \"destination_field\": \"department_id\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"source\": \"employees\",\n" +
                "        \"destination\": \"worklog\",\n" +
                "        \"source_field\": \"employee_id\",\n" +
                "        \"destination_field\": \"employee_id\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"fields\": {\n" +
                "      \"employee_id\": \"employees.employee_id\",\n" +
                "      \"name\": \"employees.name\",\n" +
                "      \"working_hours\": \"employees.working_hours\",\n" +
                "      \"department_name\": \"departments.name\",\n" +
                "      \"worklogs\": {\n" +
                "        \"worklog_id\": \"worklog.worklog_id\",\n" +
                "        \"date\": \"worklog.date\",\n" +
                "        \"hours_worked\": \"worklog.hours_worked\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";**/
       String requestJson =buildSirenFederateQuery(indices,filters,fields);
        // Create the request entity with headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        // Send the request to Elasticsearch
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        return response.getBody(); // Returning the response body as a string
    }
	
		
	public String buildSirenFederateQuery(String[] indices, String[][] filters, String[] fields) {
        JSONObject query = new JSONObject();
        JSONObject queryDetails = new JSONObject();
        queryDetails.put("type", "join");
        queryDetails.put("indices", new JSONArray(indices));
        JSONArray filterArray = new JSONArray();
        for (String[] filter : filters) {
            JSONObject filterObject = new JSONObject();
            filterObject.put("source", filter[0]);
            filterObject.put("destination", filter[1]);
            filterObject.put("source_field", filter[2]);
            filterObject.put("destination_field", filter[3]);
            filterArray.put(filterObject);
        }
        queryDetails.put("filters", filterArray);
        JSONObject fieldsObj = new JSONObject();
        for (String field : fields) {
            for (String index : indices) {
                fieldsObj.put(field, index + "." + field);
            }
        }
        queryDetails.put("fields", fieldsObj);
        query.put("query", queryDetails);

        return query.toString();
    }
}
