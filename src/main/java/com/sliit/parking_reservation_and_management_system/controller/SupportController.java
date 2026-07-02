package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.dto.TicketDTO;
import com.sliit.parking_reservation_and_management_system.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private TicketService ticketService;

    // Main support dashboard
    @GetMapping("/dashboard")
    public String supportDashboard(Model model,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        try {
            List<TicketDTO> tickets;

            if (status != null && !status.trim().isEmpty()) {
                tickets = ticketService.getTicketsByStatus(status);
            } else if (search != null && !search.trim().isEmpty()) {
                tickets = ticketService.searchAllTickets(search);
            } else {
                tickets = ticketService.getAllTickets();
            }

            model.addAttribute("tickets", tickets);
            model.addAttribute("statuses", ticketService.getAllStatuses());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchTerm", search);

            // Add ticket statistics
            model.addAttribute("totalTickets", ticketService.getTotalTicketCount());
            model.addAttribute("openTickets", ticketService.getTicketCountByStatus("OPEN"));
            model.addAttribute("inProgressTickets", ticketService.getTicketCountByStatus("IN_PROGRESS"));
            model.addAttribute("resolvedTickets", ticketService.getTicketCountByStatus("RESOLVED"));
            model.addAttribute("closedTickets", ticketService.getTicketCountByStatus("CLOSED"));

            return "support-dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading tickets: " + e.getMessage());
            return "support-dashboard";
        }
    }

    // Get ticket details (AJAX endpoint)
    @GetMapping("/ticket/{id}")
    @ResponseBody
    public ResponseEntity<TicketDTO> getTicketDetails(@PathVariable Long id) {
        try {
            TicketDTO ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update ticket status
    @PostMapping("/ticket/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            ticketService.updateTicketStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Ticket status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error updating ticket: " + e.getMessage()));
        }
    }

    // Add reply to ticket
    @PostMapping("/ticket/{id}/reply")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addReplyToTicket(
            @PathVariable Long id,
            @RequestParam String reply) {
        try {
            ticketService.addReplyToTicket(id, reply, "SUPPORT");
            return ResponseEntity.ok(Map.of("message", "Reply added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error adding reply: " + e.getMessage()));
        }
    }

    // Get tickets by priority (AJAX endpoint)
    @GetMapping("/tickets/priority/{priority}")
    @ResponseBody
    public ResponseEntity<List<TicketDTO>> getTicketsByPriority(@PathVariable String priority) {
        try {
            List<TicketDTO> tickets = ticketService.getTicketsByPriority(priority);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Assign ticket to support officer
    @PostMapping("/ticket/{id}/assign")
    @ResponseBody
    public ResponseEntity<Map<String, String>> assignTicket(
            @PathVariable Long id,
            @RequestParam String assignee) {
        try {
            // This would require adding assignee field to Ticket entity
            // For now, we'll update the status to IN_PROGRESS
            ticketService.updateTicketStatus(id, "IN_PROGRESS");
            return ResponseEntity.ok(Map.of("message", "Ticket assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error assigning ticket: " + e.getMessage()));
        }
    }

    // Get ticket statistics (AJAX endpoint)
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTicketStatistics() {
        try {
            Map<String, Object> stats = Map.of(
                    "total", ticketService.getTotalTicketCount(),
                    "open", ticketService.getTicketCountByStatus("OPEN"),
                    "inProgress", ticketService.getTicketCountByStatus("IN_PROGRESS"),
                    "resolved", ticketService.getTicketCountByStatus("RESOLVED"),
                    "closed", ticketService.getTicketCountByStatus("CLOSED"));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Export tickets to CSV
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportTicketsToCSV(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        try {
            List<TicketDTO> tickets;

            if (status != null && !status.trim().isEmpty()) {
                tickets = ticketService.getTicketsByStatus(status);
            } else if (search != null && !search.trim().isEmpty()) {
                tickets = ticketService.searchAllTickets(search);
            } else {
                tickets = ticketService.getAllTickets();
            }

            StringBuilder csvContent = new StringBuilder();
            // CSV Header
            csvContent.append(
                    "ID,Customer Name,Customer Email,Title,Category,Priority,Status,Description,Created Date,Updated Date\n");

            // CSV Data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (TicketDTO ticket : tickets) {
                csvContent.append("\"").append(ticket.getTicketID()).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getCustomerName())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getCustomerEmail())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getTitle())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getCategory())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getPriority())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getStatus())).append("\",");
                csvContent.append("\"").append(escapeCSV(ticket.getDescription())).append("\",");
                csvContent.append("\"")
                        .append(ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(formatter) : "")
                        .append("\",");
                csvContent.append("\"")
                        .append(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().format(formatter) : "")
                        .append("\"");
                csvContent.append("\n");
            }

            String filename = "support_tickets_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.toString());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting tickets: " + e.getMessage());
        }
    }

    // Helper method to escape CSV values
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape double quotes by doubling them
        return value.replace("\"", "\"\"");
    }
}