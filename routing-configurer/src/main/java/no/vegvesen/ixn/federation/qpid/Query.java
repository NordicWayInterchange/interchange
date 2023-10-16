package no.vegvesen.ixn.federation.qpid;

public class Query {

   private String sql;


   public Query(String sql) {
       this.sql = sql;
   }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
