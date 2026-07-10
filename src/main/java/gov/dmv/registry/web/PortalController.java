package gov.dmv.registry.web;

import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.service.RegistryException;
import gov.dmv.registry.service.RegistryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * The WEB layer: it turns browser requests into calls on our service, then
 * hands data to an HTML template to render. This class replaces the old console
 * menu - notice the business logic is untouched; only the "front door" changed.
 *
 * KEY ANNOTATIONS:
 *   @Controller       marks this as a web component that returns HTML PAGES
 *                     (the view's name), as opposed to raw JSON.
 *   @GetMapping("/x") "when the browser asks for the page /x, run this method".
 *   @PostMapping("/x")"when a form is SUBMITTED to /x, run this method".
 *   Model             a bag of data we fill and the HTML template reads.
 *   RedirectAttributes lets us pass a one-time "flash" message across a redirect
 *                     (the Post/Redirect/Get pattern - so refreshing the page
 *                     doesn't re-submit the form).
 *
 * Each returned String (e.g. "people") is a TEMPLATE NAME. Spring looks for
 * src/main/resources/templates/people.html and renders it.
 */
@Controller
public class PortalController {

    // The service is injected by Spring through this constructor (same idea as
    // in RegistryService receiving its repositories).
    private final RegistryService registry;

    public PortalController(RegistryService registry) {
        this.registry = registry;
    }

    // ------------------------------------------------------------------
    //  DASHBOARD  (the home page at http://localhost:8080/)
    // ------------------------------------------------------------------
    @GetMapping("/")
    public String dashboard(Model model) {
        // model.addAttribute("name", value) makes 'value' available to the
        // template under ${name}.
        model.addAttribute("peopleCount", registry.listPeople().size());
        model.addAttribute("vehicleCount", registry.listVehicles().size());
        model.addAttribute("licenseCount", registry.listLicenses().size());
        return "index"; // -> templates/index.html
    }

    // ------------------------------------------------------------------
    //  PEOPLE
    // ------------------------------------------------------------------
    @GetMapping("/people")
    public String peoplePage(Model model) {
        model.addAttribute("people", registry.listPeople());
        return "people"; // -> templates/people.html
    }

    @PostMapping("/people")
    public String addPerson(@RequestParam String firstName,
                            @RequestParam String lastName,
                            // @DateTimeFormat tells Spring the text from the date
                            // input arrives as YYYY-MM-DD and to build a LocalDate.
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                            RedirectAttributes flash) {
        try {
            var person = registry.registerPerson(firstName, lastName, dateOfBirth);
            flash.addFlashAttribute("message", "Registered " + person.getFullName()
                    + " (id " + person.getId() + ").");
        } catch (RegistryException | IllegalArgumentException e) {
            // A rule/validation failed - show WHY on the next page load.
            flash.addFlashAttribute("error", e.getMessage());
        }
        // "redirect:" sends the browser to GET /people (Post/Redirect/Get).
        return "redirect:/people";
    }

    // ------------------------------------------------------------------
    //  VEHICLES
    // ------------------------------------------------------------------
    @GetMapping("/vehicles")
    public String vehiclesPage(Model model) {
        model.addAttribute("vehicles", registry.listVehicles());
        // People + the list of vehicle types feed the dropdowns in the forms.
        model.addAttribute("people", registry.listPeople());
        model.addAttribute("vehicleTypes", VehicleType.values());
        return "vehicles";
    }

    @PostMapping("/vehicles")
    public String addVehicle(@RequestParam Long ownerId,
                             @RequestParam String vin,
                             @RequestParam String make,
                             @RequestParam String model,
                             @RequestParam int year,
                             @RequestParam VehicleType type,
                             RedirectAttributes flash) {
        try {
            var vehicle = registry.registerVehicle(ownerId, vin, make, model, year, type);
            flash.addFlashAttribute("message", "Registered vehicle id " + vehicle.getId()
                    + " (" + vehicle.getMake() + " " + vehicle.getModel() + ").");
        } catch (RegistryException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    @PostMapping("/vehicles/{id}/transfer")
    public String transferVehicle(@PathVariable Long id,
                                  @RequestParam Long newOwnerId,
                                  RedirectAttributes flash) {
        try {
            registry.transferVehicle(id, newOwnerId);
            flash.addFlashAttribute("message", "Vehicle " + id + " transferred to person " + newOwnerId + ".");
        } catch (RegistryException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    // ------------------------------------------------------------------
    //  DRIVER LICENSES
    // ------------------------------------------------------------------
    @GetMapping("/licenses")
    public String licensesPage(Model model) {
        model.addAttribute("licenses", registry.listLicenses());
        model.addAttribute("people", registry.listPeople());
        return "licenses";
    }

    @PostMapping("/licenses")
    public String issueLicense(@RequestParam Long holderId, RedirectAttributes flash) {
        try {
            var license = registry.issueLicense(holderId);
            flash.addFlashAttribute("message", "Issued " + license.getLicenseNumber()
                    + " to " + license.getHolder().getFullName() + ".");
        } catch (RegistryException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/licenses";
    }

    @PostMapping("/licenses/{id}/suspend")
    public String suspendLicense(@PathVariable Long id, RedirectAttributes flash) {
        try {
            var license = registry.suspendLicense(id);
            flash.addFlashAttribute("message", license.getLicenseNumber() + " suspended.");
        } catch (RegistryException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/licenses";
    }
}
