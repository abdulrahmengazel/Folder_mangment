package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Files")
public class Files implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Size", nullable = false)
    private Long size;

    @Column(name ="Type", nullable = false, length = 50)
    private String type;

    // تمت إضافة مسار الملف هنا ليتم حفظه في قاعدة البيانات
    @Column(name = "Path", nullable = false, length = 500)
    private String path;

    @Column(name = "Deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "Starred", nullable = false)
    private boolean starred = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "Created_At")
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "folder_id", referencedColumnName = "id")
    private Folders folder;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private Users owner;

    public Files() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // توحيد أسماء الدوال لتتوافق مع معايير JSF
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Folders getFolder() { return folder; }
    public void setFolder(Folders folder) { this.folder = folder; }

    public Users getOwner() { return owner; }
    public void setOwner(Users owner) { this.owner = owner; }
}