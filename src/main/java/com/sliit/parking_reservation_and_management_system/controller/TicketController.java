package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.dto.TicketDTO;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import com.sliit.parking_reservation_and_management_system.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    // Helper method to get customer ID from Spring Security authentication
    private Long getCustomerIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName(); // This is the email (username)
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return userOpt.get().getUserId();
    }

    // ==========================
    // Web Page Mappings
    // ==========================

    // Show create ticket form
    @GetMapping("/create")
    public String showCreateTicketForm(Model model) {
        try {
            getCustomerIdFromAuthentication(); // Validate authentication
            model.addAttribute("ticket", new TicketDTO());
            model.addAttribute("priorities", ticketService.getAllPriorities());
            return "create-ticket";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // Handle create ticket form submission
    @PostMapping("/create")
    public String createTicket(@ModelAttribute TicketDTO ticketDTO,
            RedirectAttributes redirectAttributes) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            ticketDTO.setCustomerID(customerID);
            ticketDTO.setStatus("OPEN"); // Default status

            if (ticketDTO.getPriority() == null || ticketDTO.getPriority().isEmpty()) {
                ticketDTO.setPriority("MEDIUM"); // Default priority
            }

            ticketService.createTicket(ticketDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket created successfully!");
            return "redirect:/customer/tickets/my-tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating ticket: " + e.getMessage());
            return "redirect:/customer/tickets/create";
        }
    }

    // Show my tickets page
    @GetMapping("/my-tickets")
    public String showMyTickets(Model model,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            List<TicketDTO> tickets;

            if (status != null && !status.trim().isEmpty()) {
                tickets = ticketService.getTicketsByCustomerAndStatus(customerID, status);
            } else if (search != null && !search.trim().isEmpty()) {
                tickets = ticketService.searchTicketsByTitle(customerID, search);
            } else {
                tickets = ticketService.getTicketsByCustomer(customerID);
            }

            model.addAttribute("tickets", tickets);
            model.addAttribute("statuses", ticketService.getAllStatuses());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchTerm", search);

            // Add ticket counts
            model.addAttribute("totalTickets", ticketService.getTicketCountByCustomer(customerID));
            model.addAttribute("openTickets", ticketService.getTicketCountByCustomerAndStatus(customerID, "OPEN"));
            model.addAttribute("inProgressTickets",
                    ticketService.getTicketCountByCustomerAndStatus(customerID, "IN_PROGRESS"));
            model.addAttribute("resolvedTickets",
                    ticketService.getTicketCountByCustomerAndStatus(customerID, "RESOLVED"));

            return "my-tickets";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // Show edit ticket form
    @GetMapping("/edit/{ticketID}")
    public String showEditTicketForm(@PathVariable Long ticketID, Model model) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            TicketDTO ticket = ticketService.getTicketByIdAndCustomer(ticketID, customerID);
            model.addAttribute("ticket", ticket);
            model.addAttribute("priorities", ticketService.getAllPriorities());
            model.addAttribute("statuses", ticketService.getAllStatuses());
            return "edit-ticket";
        } catch (Exception e) {
            return "redirect:/customer/tickets/my-tickets";
        }
    }

    // Handle edit ticket form submission
    @PostMapping("/edit/{ticketID}")
    public String editTicket(@PathVariable Long ticketID,
            @ModelAttribute TicketDTO ticketDTO,
            RedirectAttributes redirectAttributes) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            ticketService.updateTicket(ticketID, ticketDTO, customerID);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket updated successfully!");
            return "redirect:/customer/tickets/my-tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating ticket: " + e.getMessage());
            return "redirect:/customer/tickets/edit/" + ticketID;
        }
    }

    // Delete ticket
    @PostMapping("/delete/{ticketID}")
    public String deleteTicket(@PathVariable Long ticketID,
            RedirectAttributes redirectAttributes) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            ticketService.deleteTicket(ticketID, customerID);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting ticket: " + e.getMessage());
        }
        return "redirect:/customer/tickets/my-tickets";
    }

    // View individual ticket details
    @GetMapping("/view/{ticketID}")
    public String viewTicket(@PathVariable Long ticketID, Model model) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            TicketDTO ticket = ticketService.getTicketByIdAndCustomer(ticketID, customerID);
            model.addAttribute("ticket", ticket);
            model.addAttribute("priorities", ticketService.getAllPriorities());
            return "view-ticket";
        } catch (Exception e) {
            return "redirect:/customer/tickets/my-tickets";
        }
    }

    // ==========================
    // REST API Endpoints
    // ==========================

    // Get all tickets for the logged-in customer
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<TicketDTO>> getMyTicketsAPI() {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            List<TicketDTO> tickets = ticketService.getTicketsByCustomer(customerID);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create ticket via API
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<TicketDTO> createTicketAPI(@RequestBody TicketDTO ticketDTO) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            ticketDTO.setCustomerID(customerID);
            if (ticketDTO.getStatus() == null || ticketDTO.getStatus().isEmpty()) {
                ticketDTO.setStatus("OPEN");
            }
            if (ticketDTO.getPriority() == null || ticketDTO.getPriority().isEmpty()) {
                ticketDTO.setPriority("MEDIUM");
            }
            TicketDTO createdTicket = ticketService.createTicket(ticketDTO);
            return ResponseEntity.ok(createdTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get ticket by ID via API
    @GetMapping("/api/{ticketID}")
    @ResponseBody
    public ResponseEntity<TicketDTO> getTicketAPI(@PathVariable Long ticketID) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            TicketDTO ticket = ticketService.getTicketByIdAndCustomer(ticketID, customerID);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update ticket via API
    @PutMapping("/api/{ticketID}")
    @ResponseBody
    public ResponseEntity<TicketDTO> updateTicketAPI(@PathVariable Long ticketID,
            @RequestBody TicketDTO ticketDTO) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            TicketDTO updatedTicket = ticketService.updateTicket(ticketID, ticketDTO, customerID);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete ticket via API
    @DeleteMapping("/api/{ticketID}")
    @ResponseBody
    public ResponseEntity<Void> deleteTicketAPI(@PathVariable Long ticketID) {
        try {
            Long customerID = getCustomerIdFromAuthentication();
            ticketService.deleteTicket(ticketID, customerID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}