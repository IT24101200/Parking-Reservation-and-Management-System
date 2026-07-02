package com.sliit.parking_reservation_and_management_system.dto;

import com.sliit.parking_reservation_and_management_system.entity.Ticket;
import java.time.LocalDateTime;

public class TicketDTO {

    private Long ticketID;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private Long customerID;
    private String customerName;
    private String customerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public TicketDTO() {
    }

    // Constructor for creating new tickets
    public TicketDTO(String title, String description, String status, String priority, String category,
            Long customerID) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.customerID = customerID;
    }

    // Constructor from Ticket entity
    public TicketDTO(Ticket ticket) {
        this.ticketID = ticket.getTicketID();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus().toString();
        this.priority = ticket.getPriority().toString();
        this.category = ticket.getCategory();
        this.customerID = ticket.getCustomer().getUserID();
        this.customerName = ticket.getCustomer().getFirstName() + " " + ticket.getCustomer().getLastName();
        this.customerEmail = ticket.getCustomer().getEmail();
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
    }

    // ==========================
    // Getters and Setters
    // ==========================
    public Long getTicketID() {
        return ticketID;
    }

    public void setTicketID(Long ticketID) {
        this.ticketID = ticketID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Long customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods for display
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.toString() : "";
    }

    public String getFormattedUpdatedAt() {
        return updatedAt != null ? updatedAt.toString() : "";
    }

    public String getStatusDisplay() {
        if (status == null)
            return "";
        return status.replace("_", " ");
    }

    public String getPriorityDisplay() {
        if (priority == null)
            return "";
        return priority.toLowerCase();
    }
}