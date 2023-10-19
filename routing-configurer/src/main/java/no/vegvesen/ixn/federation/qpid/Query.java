package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Query {

   private String sql;

   private String dateTimeFormat;

   private String dateTimePattern;



   public Query(String sql) {
       this.sql = sql;
   }

    public Query(String sql, String dateTimeFormat, String dateTimePattern) {
        this.sql = sql;
        this.dateTimeFormat = dateTimeFormat;
        this.dateTimePattern = dateTimePattern;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }

    public void setDateTimePattern(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }
}
