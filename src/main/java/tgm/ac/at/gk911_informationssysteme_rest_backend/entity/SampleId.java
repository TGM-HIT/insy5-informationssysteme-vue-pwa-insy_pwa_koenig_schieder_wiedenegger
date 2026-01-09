package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class SampleId implements Serializable {

    @Column(name = "s_id", length = 13)
    @JsonProperty("s_id")
    private String sId;

    @Column(name = "s_stamp")
    @JsonProperty("s_stamp")
    private LocalDateTime sStamp;

    public SampleId() {}

    public SampleId(String sId, LocalDateTime sStamp) {
        this.sId = sId;
        this.sStamp = sStamp;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public LocalDateTime getsStamp() {
        return sStamp;
    }

    public void setsStamp(LocalDateTime sStamp) {
        this.sStamp = sStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleId sampleId = (SampleId) o;
        return Objects.equals(sId, sampleId.sId) && Objects.equals(sStamp, sampleId.sStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sId, sStamp);
    }
}
