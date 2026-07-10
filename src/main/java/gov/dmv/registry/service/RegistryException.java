package gov.dmv.registry.service;

/**
 * Our own error type, thrown when a DMV business RULE is broken - for example,
 * registering a vehicle to an owner who does not exist, or issuing a license to
 * someone too young to drive.
 *
 * WHY A CUSTOM EXCEPTION?
 * A dedicated type makes our intent clear and lets calling code catch
 * specifically "a DMV rule was violated" separately from unexpected bugs. Our
 * web controller catches this and turns the message into a friendly red banner
 * on the page instead of a stack-trace crash.
 *
 * It "extends RuntimeException" (an "unchecked" exception), so the compiler does
 * not force try/catch everywhere - keeping the happy-path code clean. Also note:
 * throwing this inside a @Transactional service method makes Spring ROLL BACK
 * the transaction, so a rejected action leaves the database unchanged.
 */
public class RegistryException extends RuntimeException {

    public RegistryException(String message) {
        super(message);
    }
}
