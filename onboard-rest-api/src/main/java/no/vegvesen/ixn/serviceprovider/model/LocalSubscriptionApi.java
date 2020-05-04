package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;

public class LocalSubscriptionApi {
    private Integer id;
    private LocalSubscriptionStatusApi status;
    private DataTypeApi dataType;

    public LocalSubscriptionApi() {

    }

    public LocalSubscriptionApi(Integer id, LocalSubscriptionStatusApi status, DataTypeApi dataType) {
        this.id = id;
        this.status = status;
        this.dataType = dataType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusApi status) {
        this.status = status;
    }

    public DataTypeApi getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeApi dataType) {
        this.dataType = dataType;
    }
}
