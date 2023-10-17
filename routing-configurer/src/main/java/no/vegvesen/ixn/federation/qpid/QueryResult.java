package no.vegvesen.ixn.federation.qpid;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryResult {

    private ArrayList<HashMap<String,Object>> results;


    public ArrayList<HashMap<String, Object>> getResults() {
        return results;
    }

    public void setResults(ArrayList<HashMap<String, Object>> results) {
        this.results = results;
    }


    @Override
    public String toString() {
        return "QueryResult{" +
                "results=" + results +
                '}';
    }
}
