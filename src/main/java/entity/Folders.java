package entity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Folders")
public class Folders implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "Created_At", nullable = false)
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private Users owner;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id", referencedColumnName = "id")
    private Folders parentFolder;

    public Folders(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Users getOwner() {
        return owner;
    }

    public void setOwner(Users owner) {
        this.owner = owner;
    }

    public Folders getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folders parentFolder) {
        this.parentFolder = parentFolder;
    }
}
