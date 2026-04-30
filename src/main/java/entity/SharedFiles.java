package entity;


import enums.PermissionEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "SharedFiles")
public class SharedFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id", nullable = false)
    private Files file;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = false)
    private Users recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "Permission", nullable = false)
    private PermissionEnum permission;

    public SharedFiles() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Files getFile() {
        return file;
    }

    public void setFile(Files file) {
        this.file = file;
    }

    public Users getRecipient() {
        return recipient;
    }

    public void setRecipient(Users recipient) {
        this.recipient = recipient;
    }

    public PermissionEnum getPermission() {
        return permission;
    }

    public void setPermission(PermissionEnum permission) {
        this.permission = permission;
    }
}
