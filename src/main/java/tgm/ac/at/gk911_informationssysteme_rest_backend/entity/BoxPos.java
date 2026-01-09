package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "boxpos", schema = "venlab")
public class BoxPos {

    @EmbeddedId
    @JsonUnwrapped
    private BoxPosId id;

    @Column(name = "s_id", length = 13)
    @JsonProperty("s_id")
    private String sampleId;

    @Column(name = "s_stamp")
    @JsonProperty("s_stamp")
    private LocalDateTime sampleStamp;

    @Column(name = "date_exported")
    @JsonProperty("date_exported")
    private LocalDateTime dateExported;

    public BoxPosId getId() {
        return id;
    }

    public void setId(BoxPosId id) {
        this.id = id;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public LocalDateTime getSampleStamp() {
        return sampleStamp;
    }

    public void setSampleStamp(LocalDateTime sampleStamp) {
        this.sampleStamp = sampleStamp;
    }

    public LocalDateTime getDateExported() {
        return dateExported;
    }

    public void setDateExported(LocalDateTime dateExported) {
        this.dateExported = dateExported;
    }
}
