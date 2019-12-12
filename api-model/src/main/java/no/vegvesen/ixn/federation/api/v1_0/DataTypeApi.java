package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "how")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Datex2DataTypeApi.class, name = Datex2DataTypeApi.DATEX_2),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTypeApi implements DataTypeI {

	private String how;
	private String where;

	public DataTypeApi() {
	}

	public DataTypeApi(String how, String where) {
		this.where = where;
		this.how = how;
	}

	@Override
	public String getWhere() {
		return this.where;
	}

	@Override
	public void setWhere(String where) {
		this.where = where;
	}

	@Override
	public String getHow() {
		return this.how;
	}

	@Override
	public void setHow(String how) {
		this.how = how;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataTypeApi that = (DataTypeApi) o;

		if (how != null ? !how.equals(that.how) : that.how != null) return false;
		return where != null ? where.equals(that.where) : that.where == null;
	}

	@Override
	public int hashCode() {
		int result = how != null ? how.hashCode() : 0;
		result = 31 * result + (where != null ? where.hashCode() : 0);
		return result;
	}
}
