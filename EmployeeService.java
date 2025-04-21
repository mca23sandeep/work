

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.query.QueryBuilders;

import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Percentiles;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.action.update.UpdateRequest;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import org.elasticsearch.rest.RestStatus;


@Service
public class EmployeeService {

    @Autowired
    private RestHighLevelClient client;

    public SearchResponse searchEmployeesByName() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        MatchPhraseQueryBuilder matchPhrase1 = new MatchPhraseQueryBuilder("empname", input);
        MatchPhraseQueryBuilder matchPhrase2 = new MatchPhraseQueryBuilder("empname", "thew a");
        boolQuery.should(matchPhrase1);
        boolQuery.should(matchPhrase2);
        boolQuery.minimumShouldMatch(1);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
	
	
	 public SearchResponse getEmployeesOnSpecificLeaveDates() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        TermsQueryBuilder termsQuery1 = QueryBuilders.termsQuery(
                "leaves.leave_start_date",
                "2025-01-01||/M", 
                "2025-01-31||/M"
        );
        TermsQueryBuilder termsQuery2 = QueryBuilders.termsQuery(
                "leaves.leave_start_date",
                "2025-08-01||/M", 
                "2025-08-31||/M"
        );
        boolQuery.must(termsQuery1);
        boolQuery.must(termsQuery2);
        NestedQueryBuilder nestedQuery = new NestedQueryBuilder("leaves", boolQuery);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(nestedQuery);
        searchRequest.source(sourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
	
	
	 private double getTop10PercentSalary() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
         SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(
            AggregationBuilders.percentiles("top_10_percent_salary")
                .field("salary.amount")
                .percents(90)
        );
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Percentiles top10PercentSalary = aggregations.get("top_10_percent_salary");
        return top10PercentSalary.getPercentile(90);
    }
	
	
	private SearchResponse getEmployeesBasedOnTopSalary(double topSalary) throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
         BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(new RangeQueryBuilder("joinDate")
                .gte("now/y")
                .lte("now+3M/y"));
            NestedQueryBuilder addressQuery = new NestedQueryBuilder("address",
                new BoolQueryBuilder()
                        .should(new WildcardQueryBuilder("address.city", "San*"))
                        .should(new WildcardQueryBuilder("address.city", "*ton"))
                        .minimumShouldMatch(1)
        );
        boolQuery.must(addressQuery);
        boolQuery.should(new BoolQueryBuilder()
                .filter(new RangeQueryBuilder("salary.amount").gte(topSalary)));

        boolQuery.should(new BoolQueryBuilder()
                .mustNot(new RangeQueryBuilder("leaves")));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);

        searchRequest.source(sourceBuilder);

        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    
    public SearchResponse getFilteredEmployeesTask3() throws IOException {
        double topSalary = getTop10PercentSalary();
      return getEmployeesBasedOnTopSalary(topSalary);
    }
	
	
	
	
	
	
	

	
	public SearchResponse getFilteredAllEmployeesTask1() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
         BoolQueryBuilder boolQuery = new BoolQueryBuilder();
           boolQuery.must(new RangeQueryBuilder("joinDate").gte("now-6M/M").lte("now/M"));
           estedQueryBuilder addressNestedQuery = new NestedQueryBuilder("address", 
            new BoolQueryBuilder()
                .should(new MatchQueryBuilder("address.state", "California"))
                .should(new MatchQueryBuilder("address.state", "Texas"))
                .should(new MatchQueryBuilder("address.state", "Florida"))
        );
        boolQuery.must(addressNestedQuery);
        BoolQueryBuilder filterQuery = new BoolQueryBuilder();
        NestedQueryBuilder salaryNestedQuery = new NestedQueryBuilder("salary", 
            new RangeQueryBuilder("salary.amount").gte(70000).lte(100000)
        );
        filterQuery.should(salaryNestedQuery);
        NestedQueryBuilder sickLeaveNestedQuery = new NestedQueryBuilder("leaves", 
            new BoolQueryBuilder()
                .must(new MatchQueryBuilder("leaves.leave_type", "sick"))
                .must(new RangeQueryBuilder("leaves.number_of_days").gt(3))
                .must(new RangeQueryBuilder("leaves.leave_start_date").gte("now-1y/y").lte("now/y"))
        );
        filterQuery.should(sickLeaveNestedQuery);
        boolQuery.filter(filterQuery);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }
	
	
	
	
	 public SearchResponse getFilteredEmployeesTask2() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data"); 
         SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
           BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        NestedQueryBuilder leavesNestedQuery = new NestedQueryBuilder("leaves", 
            new BoolQueryBuilder()
                .must(new MatchQueryBuilder("leaves.leave_type", "vacation"))
                .must(new RangeQueryBuilder("leaves.leave_start_date").gte("now-3M/M").lt("now/M"))
        );
        boolQuery.must(leavesNestedQuery);
        BoolQueryBuilder filterQuery = new BoolQueryBuilder();
        NestedQueryBuilder addressNestedQuery = new NestedQueryBuilder("address", 
            new BoolQueryBuilder()
                .should(new MatchQueryBuilder("address.country", "Canada"))
                .should(new MatchQueryBuilder("address.country", "USA"))
                .minimumShouldMatch(1)
        );
        filterQuery.must(addressNestedQuery);
        NestedQueryBuilder salaryNestedQuery = new NestedQueryBuilder("salary", 
            new RangeQueryBuilder("salary.amount").gt(80000)
        );
        filterQuery.should(salaryNestedQuery);
        NestedQueryBuilder sickLeaveNestedQuery = new NestedQueryBuilder("leaves", 
            new BoolQueryBuilder()
                .must(new MatchQueryBuilder("leaves.leave_type", "sick"))
                .must(new RangeQueryBuilder("leaves.number_of_days").gt(7))
                .must(new RangeQueryBuilder("leaves.leave_start_date").gte("now/y"))
        );
        filterQuery.should(sickLeaveNestedQuery);
        boolQuery.filter(filterQuery);
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }
	
    public SearchResponse getFilteredEmployeesTask3() throws IOException {
        double topSalary = getTop10PercentSalary_v2();
      return searchEmployees_v2(topSalary);
    }
	
	 public double getTop10PercentSalary_v2() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
        searchRequest.source().aggregation("top_10_percent_salary", AggregationBuilders.percentiles("salary_percentiles")
                .field("salary.amount")
                .percentiles(90));
         SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
         Percentiles percentiles = response.getAggregations().get("top_10_percent_salary");
        Map<String, Double> percentilesMap = percentiles.getValues();
        return percentilesMap.get("90.0");
    }

    public SearchResponse searchEmployees_v2(double top10PercentSalary) throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees_data");
         BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        
        boolQuery.must(
            query -> query.range(r -> r.field("joinDate").gte("now/y").lte("now+3M/y"))
        );
        
        boolQuery.must(
            query -> query.nested(n -> n.path("address").query(q -> q.bool(b -> b.should(
                    s -> s.wildcard(w -> w.field("address.city").value("San*")),
                    t -> t.wildcard(w -> w.field("address.city").value("*ton"))
            ))))
        );

        boolQuery.should(
            query -> query.bool(b -> b.filter(
                f -> f.range(r -> r.field("salary.amount").gte(top10PercentSalary))
            ))
        );
        
        boolQuery.should(
            query -> query.bool(b -> b.mustNot(
                m -> m.exists(e -> e.field("leaves"))
            ))
        );
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        return response;
    }
	
	
	
	
	public boolean upsertEmployee(String id, String jsonData) throws IOException {
        // Create an update request
        UpdateRequest updateRequest = new UpdateRequest("employees_data", id)
                .doc(jsonData, XContentType.JSON)  // Update the document
                .upsert(jsonData, XContentType.JSON);  // If the document doesn't exist, insert it

        // Execute the update request
        boolean isUpdated = false;
        try {
            // Send the update request to Elasticsearch
            client.update(updateRequest, RequestOptions.DEFAULT);
            isUpdated = true;
        } catch (IOException e) {
            // Handle exception (for example, log the error or rethrow)
            e.printStackTrace();
        }

        return isUpdated;
    }
	
}
