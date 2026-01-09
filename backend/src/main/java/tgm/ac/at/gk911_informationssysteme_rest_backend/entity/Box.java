package tgm.ac.at.gk911_informationssysteme_rest_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "box", schema = "venlab")
public class Box {

    @Id
    @Column(name = "b_id", length = 4)
    @JsonProperty("b_id")
    private String id;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @ColumnDefault("40")
    @Column(name = "num_max")
    @JsonProperty("num_max")
    private Integer numMax;

    @ColumnDefault("1")
    @Column(name = "type")
    private Integer type;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getNumMax() {
        return numMax;
    }

    public void setNumMax(Integer numMax) {
        this.numMax = numMax;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
