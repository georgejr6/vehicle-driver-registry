package gov.dmv.registry.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A driver's license issued to a Person. Like Vehicle, it points at its holder
 * by id ({@code holderId}) rather than storing a whole Person.
 *
 * A license has a lifecycle (see {@link LicenseStatus}) and an expiry date, so
 * this class carries a little behaviour of its own - deciding whether it is
 * currently valid.
 */
public class DriverLicense {

    /** The printed license number, e.g. "DL-1". Unique across the registry. */
    private final String number;

    /** id of the Person who holds this license. */
    private final String holderId;

    private final LocalDate issueDate;
    private final LocalDate expiryDate;

    // Status can change over time (renewals, suspensions), so it is not final.
    private LicenseStatus status;

    public DriverLicense(String number, String holderId, LocalDate issueDate,
                         LocalDate expiryDate, LicenseStatus status) {
        this.number = Objects.requireNonNull(number, "number is required");
        this.holderId = Objects.requireNonNull(holderId, "holderId is required");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate is required");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate is required");
        this.status = Objects.requireNonNull(status, "status is required");

        // A quick sanity rule: a license cannot expire before it was issued.
        if (expiryDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("expiryDate cannot be before issueDate");
        }
    }

    // ---- Getters ---------------------------------------------------------

    public String getNumber() {
        return number;
    }

    public String getHolderId() {
        return holderId;
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

    /**
     * Is this license usable on the given day? It must be ACTIVE *and* not past
     * its expiry date. Passing "today" in (instead of reading the clock here)
     * keeps the logic testable.
     */
    public boolean isValidOn(LocalDate today) {
        boolean notExpiredYet = !today.isAfter(expiryDate); // today <= expiry
        return status == LicenseStatus.ACTIVE && notExpiredYet;
    }

    /** Change the license state (used by the service layer for suspend/renew). */
    public void setStatus(LicenseStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus is required");
    }

    @Override
    public String toString() {
        return "DriverLicense{" + number + ", holder " + holderId
                + ", " + status + ", expires " + expiryDate + "}";
    }
}
