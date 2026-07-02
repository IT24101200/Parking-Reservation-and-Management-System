package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Ticket;
import com.sliit.parking_reservation_and_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by customer
    List<Ticket> findByCustomer(User customer);

    // Find tickets by customer ID
    List<Ticket> findByCustomerUserID(Long customerID);

    // Find tickets by status
    List<Ticket> findByStatus(Ticket.TicketStatus status);

    // Find tickets by priority
    List<Ticket> findByPriority(Ticket.TicketPriority priority);

    // Find tickets by customer and status
    List<Ticket> findByCustomerAndStatus(User customer, Ticket.TicketStatus status);

    // Find tickets by customer ID and status
    List<Ticket> findByCustomerUserIDAndStatus(Long customerID, Ticket.TicketStatus status);

    // Find tickets by title containing (case insensitive search)
    List<Ticket> findByTitleContainingIgnoreCase(String title);

    // Find tickets by customer and title containing
    List<Ticket> findByCustomerAndTitleContainingIgnoreCase(User customer, String title);

    // Find tickets by category
    List<Ticket> findByCategory(String category);

    // Custom query to find tickets by customer with ordering
    @Query("SELECT t FROM Ticket t WHERE t.customer = :customer ORDER BY t.createdAt DESC")
    List<Ticket> findByCustomerOrderByCreatedAtDesc(@Param("customer") User customer);

    // Custom query to find tickets by customer ID with ordering
    @Query("SELECT t FROM Ticket t WHERE t.customer.userId = :customerID ORDER BY t.createdAt DESC")
    List<Ticket> findByCustomerUserIDOrderByCreatedAtDesc(@Param("customerID") Long customerID);

    // Count tickets by customer
    long countByCustomer(User customer);

    // Count tickets by customer and status
    long countByCustomerAndStatus(User customer, Ticket.TicketStatus status);

    // Support Officer Methods

    // Find all tickets ordered by creation date (newest first)
    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findAllByOrderByCreatedAtDesc();

    // Find tickets by status ordered by creation date
    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Ticket> findByStatusOrderByCreatedAtDesc(@Param("status") Ticket.TicketStatus status);

    // Find tickets by priority ordered by creation date
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority ORDER BY t.createdAt DESC")
    List<Ticket> findByPriorityOrderByCreatedAtDesc(@Param("priority") Ticket.TicketPriority priority);

    // Search tickets by title or description (case insensitive)
    @Query("SELECT t FROM Ticket t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY t.createdAt DESC")
    List<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
            @Param("searchTerm") String titleSearch, @Param("searchTerm") String descriptionSearch);

    // Count tickets by status
    long countByStatus(Ticket.TicketStatus status);

    // Find tickets by customer and priority
    List<Ticket> findByCustomerAndPriority(User customer, Ticket.TicketPriority priority);

    // Find tickets by status and priority
    List<Ticket> findByStatusAndPriority(Ticket.TicketStatus status, Ticket.TicketPriority priority);
}