package gov.dmv.registry.service;

/**
 * Our own custom error type, thrown when a DMV business RULE is broken - for
 * example, trying to register a vehicle to an owner who does not exist, or
 * issuing a license to someone too young to drive.
 *
 * WHY MAKE OUR OWN EXCEPTION?
 * Java has many built-in error types, but a dedicated one makes our intent
 * clear and lets calling code catch specifically "a DMV rule was violated"
 * separately from unexpected programming bugs.
 *
 * It "extends RuntimeException", which makes it an "unchecked" exception - the
 * compiler does not force every caller to wrap calls in try/catch. That keeps
 * our happy-path code clean; we catch it deliberately where we want to (for
 * example, in the console menu, to show a friendly message instead of crashing).
 */
public class RegistryException extends RuntimeException {

    public RegistryException(String message) {
        super(message); // hand the explanation text up to the base class.
    }
}
