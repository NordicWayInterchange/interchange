package no.vegvesen.ixn.serviceprovider.model;

/*-
 * #%L
 * onboard-rest-api
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
