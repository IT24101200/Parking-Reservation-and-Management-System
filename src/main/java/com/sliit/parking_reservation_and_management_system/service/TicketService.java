package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.dto.TicketDTO;
import com.sliit.parking_reservation_and_management_system.entity.Ticket;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.TicketRepository;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new ticket
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        // Find the customer
        Optional<User> customerOpt = userRepository.findById(ticketDTO.getCustomerID());
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found with ID: " + ticketDTO.getCustomerID());
        }

        User customer = customerOpt.get();

        // Create ticket entity
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setStatus(Ticket.TicketStatus.valueOf(ticketDTO.getStatus()));
        ticket.setPriority(Ticket.TicketPriority.valueOf(ticketDTO.getPriority()));
        ticket.setCategory(ticketDTO.getCategory());
        ticket.setCustomer(customer);

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        return new TicketDTO(savedTicket);
    }

    // Get all tickets for a specific customer
    public List<TicketDTO> getTicketsByCustomer(Long customerID) {
        List<Ticket> tickets = ticketRepository.findByCustomerUserIDOrderByCreatedAtDesc(customerID);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Get ticket by ID
    public TicketDTO getTicketById(Long ticketID) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }
        return new TicketDTO(ticketOpt.get());
    }

    // Get ticket by ID and ensure it belongs to the customer
    public TicketDTO getTicketByIdAndCustomer(Long ticketID, Long customerID) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }

        Ticket ticket = ticketOpt.get();
        if (!ticket.getCustomer().getUserId().equals(customerID)) {
            throw new RuntimeException("Ticket does not belong to the specified customer");
        }

        return new TicketDTO(ticket);
    }

    // Update an existing ticket
    public TicketDTO updateTicket(Long ticketID, TicketDTO ticketDTO, Long customerID) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }

        Ticket ticket = ticketOpt.get();

        // Ensure the ticket belongs to the customer
        if (!ticket.getCustomer().getUserId().equals(customerID)) {
            throw new RuntimeException("Ticket does not belong to the specified customer");
        }

        // Update ticket fields
        if (ticketDTO.getTitle() != null && !ticketDTO.getTitle().trim().isEmpty()) {
            ticket.setTitle(ticketDTO.getTitle());
        }
        if (ticketDTO.getDescription() != null) {
            ticket.setDescription(ticketDTO.getDescription());
        }
        if (ticketDTO.getStatus() != null && !ticketDTO.getStatus().trim().isEmpty()) {
            ticket.setStatus(Ticket.TicketStatus.valueOf(ticketDTO.getStatus()));
        }
        if (ticketDTO.getPriority() != null && !ticketDTO.getPriority().trim().isEmpty()) {
            ticket.setPriority(Ticket.TicketPriority.valueOf(ticketDTO.getPriority()));
        }
        if (ticketDTO.getCategory() != null) {
            ticket.setCategory(ticketDTO.getCategory());
        }

        // Save updated ticket
        Ticket updatedTicket = ticketRepository.save(ticket);
        return new TicketDTO(updatedTicket);
    }

    // Delete a ticket
    public void deleteTicket(Long ticketID, Long customerID) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }

        Ticket ticket = ticketOpt.get();

        // Ensure the ticket belongs to the customer
        if (!ticket.getCustomer().getUserId().equals(customerID)) {
            throw new RuntimeException("Ticket does not belong to the specified customer");
        }

        ticketRepository.delete(ticket);
    }

    // Get tickets by status for a customer
    public List<TicketDTO> getTicketsByCustomerAndStatus(Long customerID, String status) {
        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status);
        List<Ticket> tickets = ticketRepository.findByCustomerUserIDAndStatus(customerID, ticketStatus);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Search tickets by title for a customer
    public List<TicketDTO> searchTicketsByTitle(Long customerID, String searchTerm) {
        Optional<User> customerOpt = userRepository.findById(customerID);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found with ID: " + customerID);
        }

        List<Ticket> tickets = ticketRepository.findByCustomerAndTitleContainingIgnoreCase(customerOpt.get(),
                searchTerm);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Get ticket count for a customer
    public long getTicketCountByCustomer(Long customerID) {
        Optional<User> customerOpt = userRepository.findById(customerID);
        if (customerOpt.isEmpty()) {
            return 0;
        }
        return ticketRepository.countByCustomer(customerOpt.get());
    }

    // Get ticket count by status for a customer
    public long getTicketCountByCustomerAndStatus(Long customerID, String status) {
        Optional<User> customerOpt = userRepository.findById(customerID);
        if (customerOpt.isEmpty()) {
            return 0;
        }

        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status);
        return ticketRepository.countByCustomerAndStatus(customerOpt.get(), ticketStatus);
    }

    // Get all available ticket statuses
    public Ticket.TicketStatus[] getAllStatuses() {
        return Ticket.TicketStatus.values();
    }

    // Get all available ticket priorities
    public Ticket.TicketPriority[] getAllPriorities() {
        return Ticket.TicketPriority.values();
    }

    // Support Officer Methods

    // Get all tickets (for support officers)
    public List<TicketDTO> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Get tickets by status (for support officers)
    public List<TicketDTO> getTicketsByStatus(String status) {
        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status);
        List<Ticket> tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(ticketStatus);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Get tickets by priority (for support officers)
    public List<TicketDTO> getTicketsByPriority(String priority) {
        Ticket.TicketPriority ticketPriority = Ticket.TicketPriority.valueOf(priority);
        List<Ticket> tickets = ticketRepository.findByPriorityOrderByCreatedAtDesc(ticketPriority);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Search all tickets (for support officers)
    public List<TicketDTO> searchAllTickets(String searchTerm) {
        List<Ticket> tickets = ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                        searchTerm, searchTerm);
        return tickets.stream()
                .map(TicketDTO::new)
                .collect(Collectors.toList());
    }

    // Update ticket status (for support officers)
    public TicketDTO updateTicketStatus(Long ticketID, String status) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }

        Ticket ticket = ticketOpt.get();
        ticket.setStatus(Ticket.TicketStatus.valueOf(status));

        Ticket updatedTicket = ticketRepository.save(ticket);
        return new TicketDTO(updatedTicket);
    }

    // Add reply to ticket (for support officers)
    public void addReplyToTicket(Long ticketID, String reply, String replyType) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketID);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found with ID: " + ticketID);
        }

        Ticket ticket = ticketOpt.get();

        // For now, we'll add the reply to the description
        // In a real implementation, you'd have a separate TicketReply entity
        String currentDescription = ticket.getDescription();
        String timestamp = java.time.LocalDateTime.now().toString();
        String newDescription = currentDescription + "\n\n--- " + replyType + " REPLY (" + timestamp + ") ---\n"
                + reply;

        ticket.setDescription(newDescription);
        ticketRepository.save(ticket);
    }

    // Get total ticket count (for support officers)
    public long getTotalTicketCount() {
        return ticketRepository.count();
    }

    // Get ticket count by status (for support officers)
    public long getTicketCountByStatus(String status) {
        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status);
        return ticketRepository.countByStatus(ticketStatus);
    }
}