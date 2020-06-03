package no.vegvesen.ixn.federation.model;

/*-
 * #%L
 * neighbour-model
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

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_subscriptions")
public class LocalSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsub_generator")
    @SequenceGenerator(name="locsub_generator", sequenceName = "locsub_seq")
    @Column(name="locsub_id")
    private Integer sub_id;

    @Enumerated(EnumType.STRING)
    private LocalSubscriptionStatus status = LocalSubscriptionStatus.REQUESTED;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locsub_dat_id", referencedColumnName = "dat_id", foreignKey = @ForeignKey(name = "fk_locsub_dat"))
    private DataType dataType;

    public LocalSubscription() {

    }

    public LocalSubscription(LocalSubscriptionStatus status, DataType dataType) {
        this.status = status;
        this.dataType = dataType;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, DataType dataType) {
        this.sub_id = id;
        this.status = status;
        this.dataType = dataType;
    }


    public void setStatus(LocalSubscriptionStatus status) {
        this.status = status;
    }

    public LocalSubscriptionStatus getStatus() {
        return status;
    }

    public boolean isSubscriptionWanted() {
        return status.equals(LocalSubscriptionStatus.REQUESTED)
                || status.equals(LocalSubscriptionStatus.CREATED);
    }

    public DataType getDataType() {
        return dataType;
    }

    public String selector() {
        return dataType.toSelector();
    }

    //TODO lag et objekt av selector??
    public String bindKey() {
        return "" + dataType.toSelector().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscription that = (LocalSubscription) o;
        return status == that.status &&
                Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, dataType);
    }

    public Integer getSub_id() {
        return sub_id;
    }

    @Override
    public String toString() {
        return "LocalSubscription{" +
                "sub_id=" + sub_id +
                ", status=" + status +
                ", dataType=" + dataType +
                '}';
    }

    public LocalSubscription withStatus(LocalSubscriptionStatus newStatus) {
        if (newStatus.equals(this.status)) {
            return this;
        } else {
            return new LocalSubscription(sub_id,newStatus,dataType);
        }
    }
}
