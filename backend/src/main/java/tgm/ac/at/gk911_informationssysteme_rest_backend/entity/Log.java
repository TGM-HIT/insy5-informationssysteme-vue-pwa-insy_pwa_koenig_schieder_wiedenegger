package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "log", schema = "venlab")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ColumnDefault("now()")
    @Column(name = "date_created")
    private Instant dateCreated;

    @Size(max = 10)
    @Column(name = "level", length = 10)
    private String level;

    @Size(max = 255)
    @Column(name = "info")
    private String info;

    @Column(name = "s_id", length = 13)
    private String sId;

    @Column(name = "s_stamp")
    private Instant sStamp;

    @Column(name = "a_id")
    private Long aId;

    @Column(name = "date_exported")
    private Instant dateExported;

    // Beziehungen für interne Nutzung
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "s_id", referencedColumnName = "s_id", insertable = false, updatable = false),
            @JoinColumn(name = "s_stamp", referencedColumnName = "s_stamp", insertable = false, updatable = false)
    })
    @JsonIgnore
    private Sample sample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_id", insertable = false, updatable = false)
    @JsonIgnore
    private Analysis a;

    // Getters und Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSId() {
        return sId;
    }

    public void setSId(String sId) {
        this.sId = sId;
    }

    public Instant getSStamp() {
        return sStamp;
    }

    public void setSStamp(Instant sStamp) {
        this.sStamp = sStamp;
    }

    public Long getAId() {
        return aId;
    }

    public void setAId(Long aId) {
        this.aId = aId;
    }

    public Instant getDateExported() {
        return dateExported;
    }

    public void setDateExported(Instant dateExported) {
        this.dateExported = dateExported;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Analysis getA() {
        return a;
    }

    public void setA(Analysis a) {
        this.a = a;
    }
}
