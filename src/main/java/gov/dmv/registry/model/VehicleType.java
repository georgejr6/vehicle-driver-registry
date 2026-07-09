// The "package" line tells Java WHERE this file lives in the folder tree.
// It must match the folders: gov/dmv/registry/model. Every .java file starts
// with its package.
package gov.dmv.registry.model;

/**
 * An "enum" (enumeration) is a special type whose value can only be ONE of a
 * fixed, known list. It is perfect for categories that never change at runtime.
 *
 * A vehicle in our registry must be exactly one of these kinds. Using an enum
 * (instead of a plain String like "car") means the compiler stops us from ever
 * storing a typo such as "cra" or "Automobile" - only these five values exist.
 *
 * NOTE: In Java, the text between /** ... *\/ is a "Javadoc" comment - the
 * official way to document a class so tools and IntelliJ can show it as help.
 */
public enum VehicleType {
    CAR,
    TRUCK,
    MOTORCYCLE,
    BUS,
    TRAILER
}
