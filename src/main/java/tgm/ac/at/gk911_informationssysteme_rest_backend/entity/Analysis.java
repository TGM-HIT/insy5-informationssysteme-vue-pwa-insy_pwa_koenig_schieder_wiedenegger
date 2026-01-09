package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "analysis", schema = "venlab")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_seq")
    @SequenceGenerator(name = "analysis_seq", sequenceName = "venlab.analysis_a_id_seq", allocationSize = 1)
    @Column(name = "a_id", nullable = false)
    @JsonProperty("a_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "s_id", referencedColumnName = "s_id", insertable = false, updatable = false),
            @JoinColumn(name = "s_stamp", referencedColumnName = "s_stamp", insertable = false, updatable = false)
    })
    @JsonIgnore
    private Sample sample;

    @Column(name = "s_id", length = 13)
    @JsonProperty("s_id")
    private String sId;

    @Column(name = "s_stamp")
    @JsonProperty("s_stamp")
    private LocalDateTime sStamp;

    @Column(name = "pol", precision = 8, scale = 2)
    private BigDecimal pol;

    @Column(name = "nat", precision = 8, scale = 2)
    private BigDecimal nat;

    @Column(name = "kal", precision = 8, scale = 2)
    private BigDecimal kal;

    @Column(name = "an", precision = 8, scale = 2)
    private BigDecimal an;

    @Column(name = "glu", precision = 8, scale = 2)
    private BigDecimal glu;

    @Column(name = "dry", precision = 8, scale = 2)
    private BigDecimal dry;

    @Column(name = "date_in")
    @JsonProperty("date_in")
    private LocalDateTime dateIn;

    @Column(name = "date_out")
    @JsonProperty("date_out")
    private LocalDateTime dateOut;

    @Column(name = "weight_mea", precision = 8, scale = 2)
    @JsonProperty("weight_mea")
    private BigDecimal weightMea;

    @Column(name = "weight_nrm", precision = 8, scale = 2)
    @JsonProperty("weight_nrm")
    private BigDecimal weightNrm;

    @Column(name = "weight_cur", precision = 8, scale = 2)
    @JsonProperty("weight_cur")
    private BigDecimal weightCur;

    @Column(name = "weight_dif", precision = 8, scale = 2)
    @JsonProperty("weight_dif")
    private BigDecimal weightDif;

    @Column(name = "density", precision = 8, scale = 2)
    private BigDecimal density;

    @Size(max = 15)
    @ColumnDefault("'----------'")
    @Column(name = "a_flags", length = 15)
    @JsonProperty("a_flags")
    private String aFlags;

    @ColumnDefault("1")
    @Column(name = "lane")
    private Integer lane;

    @Size(max = 255)
    @Column(name = "comment")
    private String comment;

    @Column(name = "date_exported")
    @JsonProperty("date_exported")
    private LocalDateTime dateExported;

    public LocalDateTime getDateExported() {
        return dateExported;
    }

    public void setDateExported(LocalDateTime dateExported) {
        this.dateExported = dateExported;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getLane() {
        return lane;
    }

    public void setLane(Integer lane) {
        this.lane = lane;
    }

    public String getAFlags() {
        return aFlags;
    }

    public void setAFlags(String aFlags) {
        this.aFlags = aFlags;
    }

    public BigDecimal getDensity() {
        return density;
    }

    public void setDensity(BigDecimal density) {
        this.density = density;
    }

    public BigDecimal getWeightDif() {
        return weightDif;
    }

    public void setWeightDif(BigDecimal weightDif) {
        this.weightDif = weightDif;
    }

    public BigDecimal getWeightCur() {
        return weightCur;
    }

    public void setWeightCur(BigDecimal weightCur) {
        this.weightCur = weightCur;
    }

    public BigDecimal getWeightNrm() {
        return weightNrm;
    }

    public void setWeightNrm(BigDecimal weightNrm) {
        this.weightNrm = weightNrm;
    }

    public BigDecimal getWeightMea() {
        return weightMea;
    }

    public void setWeightMea(BigDecimal weightMea) {
        this.weightMea = weightMea;
    }

    public LocalDateTime getDateOut() {
        return dateOut;
    }

    public void setDateOut(LocalDateTime dateOut) {
        this.dateOut = dateOut;
    }

    public LocalDateTime getDateIn() {
        return dateIn;
    }

    public void setDateIn(LocalDateTime dateIn) {
        this.dateIn = dateIn;
    }

    public BigDecimal getDry() {
        return dry;
    }

    public void setDry(BigDecimal dry) {
        this.dry = dry;
    }

    public BigDecimal getGlu() {
        return glu;
    }

    public void setGlu(BigDecimal glu) {
        this.glu = glu;
    }

    public BigDecimal getAn() {
        return an;
    }

    public void setAn(BigDecimal an) {
        this.an = an;
    }

    public BigDecimal getKal() {
        return kal;
    }

    public void setKal(BigDecimal kal) {
        this.kal = kal;
    }

    public BigDecimal getNat() {
        return nat;
    }

    public void setNat(BigDecimal nat) {
        this.nat = nat;
    }

    public BigDecimal getPol() {
        return pol;
    }

    public void setPol(BigDecimal pol) {
        this.pol = pol;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getSId() {
        return sId;
    }

    public void setSId(String sId) {
        this.sId = sId;
    }

    public LocalDateTime getSStamp() {
        return sStamp;
    }

    public void setSStamp(LocalDateTime sStamp) {
        this.sStamp = sStamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
