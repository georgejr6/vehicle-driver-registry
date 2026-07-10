package gov.dmv.registry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A driver's license issued to a Person.
 *
 * RELATIONSHIP: we use {@code @OneToOne} here because, in this version of the
 * system, a person holds at most ONE license and a license belongs to exactly
 * one person. (Vehicle used @ManyToOne because a person can own many vehicles.)
 *
 * This entity also carries a little behaviour of its own - deciding whether it
 * is currently valid - which keeps that rule close to the data it concerns.
 */
@Entity
@Table(name = "driver_license")
public class DriverLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "holder_id", nullable = false, unique = true)
    private Person holder;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    protected DriverLicense() {
    }

    public DriverLicense(Person holder, LocalDate issueDate, LocalDate expiryDate,
                         LicenseStatus status) {
        this.holder = Objects.requireNonNull(holder, "holder is required");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate is required");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate is required");
        this.status = Objects.requireNonNull(status, "status is required");
        if (expiryDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("expiryDate cannot be before issueDate");
        }
    }

    // ---- Getters ---------------------------------------------------------

    public Long getId() {
        return id;
    }

    /**
     * A friendly, human-facing license number derived from the database id,
     * e.g. id 3 -> "DL-000003". Computed on demand, so there is nothing extra
     * to keep in sync. (Returns "DL-(unsaved)" only if called before saving.)
     */
    public String getLicenseNumber() {
        return id == null ? "DL-(unsaved)" : String.format("DL-%06d", id);
    }

    public Person getHolder() {
        return holder;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    // ---- Behaviour -------------------------------------------------------

    /** Usable today? Must be ACTIVE and not past its expiry date. */
    public boolean isValidOn(LocalDate today) {
        boolean notExpiredYet = !today.isAfter(expiryDate);
        return status == LicenseStatus.ACTIVE && notExpiredYet;
    }

    public void setStatus(LicenseStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus is required");
    }

    @Override
    public String toString() {
        return "DriverLicense{" + getLicenseNumber() + ", " + status
                + ", expires " + expiryDate + "}";
    }
}
