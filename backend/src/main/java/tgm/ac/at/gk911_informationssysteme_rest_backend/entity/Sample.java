package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "sample", schema = "venlab")
public class Sample {

    @EmbeddedId
    private SampleId id;

    // Flache Felder für JSON (ohne @JsonUnwrapped)
    @Transient
    @JsonProperty("s_id")
    public String getSId() {
        return id != null ? id.getsId() : null;
    }

    @Transient
    @JsonProperty("s_stamp")
    public LocalDateTime getSStamp() {
        return id != null ? id.getsStamp() : null;
    }

    @Column(name = "name")
    private String name;

    @Column(name = "weight_net", precision = 8, scale = 2)
    @JsonProperty("weight_net")
    private BigDecimal weightNet;

    @Column(name = "weight_bru", precision = 8, scale = 2)
    @JsonProperty("weight_bru")
    private BigDecimal weightBru;

    @Column(name = "weight_tar", precision = 8, scale = 2)
    @JsonProperty("weight_tar")
    private BigDecimal weightTar;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "distance", precision = 8, scale = 2)
    private BigDecimal distance;

    @Column(name = "date_crumbled")
    @JsonProperty("date_crumbled")
    private LocalDateTime dateCrumbled;

    @Column(name = "s_flags", length = 10)
    @JsonProperty("s_flags")
    private String sFlags;

    @Column(name = "lane")
    private Integer lane;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date_exported")
    @JsonProperty("date_exported")
    private LocalDateTime dateExported;

    // Getters und Setters
    public SampleId getId() {
        return id;
    }

    public void setId(SampleId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeightNet() {
        return weightNet;
    }

    public void setWeightNet(BigDecimal weightNet) {
        this.weightNet = weightNet;
    }

    public BigDecimal getWeightBru() {
        return weightBru;
    }

    public void setWeightBru(BigDecimal weightBru) {
        this.weightBru = weightBru;
    }

    public BigDecimal getWeightTar() {
        return weightTar;
    }

    public void setWeightTar(BigDecimal weightTar) {
        this.weightTar = weightTar;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public LocalDateTime getDateCrumbled() {
        return dateCrumbled;
    }

    public void setDateCrumbled(LocalDateTime dateCrumbled) {
        this.dateCrumbled = dateCrumbled;
    }

    public String getsFlags() {
        return sFlags;
    }

    public void setsFlags(String sFlags) {
        this.sFlags = sFlags;
    }

    public Integer getLane() {
        return lane;
    }

    public void setLane(Integer lane) {
        this.lane = lane;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDateExported() {
        return dateExported;
    }

    public void setDateExported(LocalDateTime dateExported) {
        this.dateExported = dateExported;
    }
}
