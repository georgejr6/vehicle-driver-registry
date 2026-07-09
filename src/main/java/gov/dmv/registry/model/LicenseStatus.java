package gov.dmv.registry.model;

/**
 * The lifecycle state a driver's license can be in. A license moves between
 * these over time (e.g. ACTIVE -> SUSPENDED -> ACTIVE, or ACTIVE -> EXPIRED).
 *
 * Modelling this as an enum makes our business rules crystal clear: later, a
 * rule like "you may only drive if your license is ACTIVE" becomes a simple,
 * readable check instead of comparing loose text strings.
 */
public enum LicenseStatus {
    /** Valid and usable right now. */
    ACTIVE,

    /** Past its expiry date - needs renewal before it can be used again. */
    EXPIRED,

    /** Temporarily blocked (e.g. too many violations). Can be reinstated. */
    SUSPENDED,

    /** Permanently cancelled by the authority. */
    REVOKED
}
