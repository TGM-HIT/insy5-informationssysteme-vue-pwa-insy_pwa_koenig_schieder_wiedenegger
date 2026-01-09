package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BoxPosId implements Serializable {

    @Column(name = "bpos_id")
    @JsonProperty("bpos_id")
    private Integer bposId;

    @Column(name = "b_id", length = 4)
    @JsonProperty("b_id")
    private String bId;

    public BoxPosId() {}

    public BoxPosId(Integer bposId, String bId) {
        this.bposId = bposId;
        this.bId = bId;
    }

    public Integer getBposId() {
        return bposId;
    }

    public void setBposId(Integer bposId) {
        this.bposId = bposId;
    }

    public String getbId() {
        return bId;
    }

    public void setbId(String bId) {
        this.bId = bId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxPosId boxPosId = (BoxPosId) o;
        return Objects.equals(bposId, boxPosId.bposId) && Objects.equals(bId, boxPosId.bId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bposId, bId);
    }
}
