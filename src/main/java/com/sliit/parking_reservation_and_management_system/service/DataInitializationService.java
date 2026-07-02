package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.entity.Ticket;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import com.sliit.parking_reservation_and_management_system.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeTickets();
    }

    private void initializeUsers() {
        // Check if users already exist to avoid duplicates
        if (userService.getAllUsers().isEmpty()) {
            System.out.println("Initializing sample data...");

            // Create Admin Users
            createUser("admin@parking.com", "admin123", "ADMIN", "Admin", "User", "0771234567");
            createUser("superadmin@parking.com", "super123", "ADMIN", "Super", "Admin", "0771234568");

            // Create Customer Users
            createUser("customer1@gmail.com", "customer123", "CUSTOMER", "John", "Doe", "0771234569");
            createUser("customer2@gmail.com", "customer123", "CUSTOMER", "Jane", "Smith", "0771234570");
            createUser("customer3@gmail.com", "customer123", "CUSTOMER", "Michael", "Johnson", "0771234571");
            createUser("customer4@gmail.com", "customer123", "CUSTOMER", "Emily", "Davis", "0771234572");
            createUser("customer5@gmail.com", "customer123", "CUSTOMER", "David", "Wilson", "0771234573");

            // Create Slot Manager Users
            createUser("slotmanager1@parking.com", "slot123", "PARKING_SLOT_MANAGER", "Sarah", "Brown", "0771234574");
            createUser("slotmanager2@parking.com", "slot123", "PARKING_SLOT_MANAGER", "Robert", "Taylor", "0771234575");

            // Create Finance Users
            createUser("finance1@parking.com", "finance123", "FINANCE_EXECUTIVE", "Lisa", "Anderson", "0771234576");
            createUser("finance2@parking.com", "finance123", "FINANCE_EXECUTIVE", "James", "Thomas", "0771234577");

            // Create Security Users
            createUser("security1@parking.com", "security123", "SECURITY_OFFICER", "Mark", "Jackson", "0771234578");
            createUser("security2@parking.com", "security123", "SECURITY_OFFICER", "Amanda", "White", "0771234579");

            // Create Support Users
            createUser("support1@parking.com", "support123", "CUSTOMER_SUPPORT_OFFICER", "Chris", "Harris",
                    "0771234580");
            createUser("support2@parking.com", "support123", "CUSTOMER_SUPPORT_OFFICER", "Nicole", "Martin",
                    "0771234581");

            System.out.println("Sample data initialization completed!");
            System.out.println("=== LOGIN CREDENTIALS ===");
            System.out.println("Admin: admin@parking.com / admin123");
            System.out.println("Customer: customer1@gmail.com / customer123");
            System.out.println("Slot Manager: slotmanager1@parking.com / slot123");
            System.out.println("Finance: finance1@parking.com / finance123");
            System.out.println("Security: security1@parking.com / security123");
            System.out.println("Support: support1@parking.com / support123");
            System.out.println("========================");
        } else {
            System.out.println("Users already exist in database. Skipping initialization.");
        }
    }

    private void createUser(String email, String password, String role, String firstName, String lastName,
            String phoneNumber) {
        try {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(password); // Will be hashed by UserService
            user.setRole(role);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setStatus("ACTIVE");

            userService.saveUser(user);
            System.out.println("Created user: " + email + " with role: " + role);
        } catch (Exception e) {
            System.err.println("Failed to create user " + email + ": " + e.getMessage());
        }
    }

    private void initializeTickets() {
        // Check if tickets already exist
        if (ticketRepository.count() == 0) {
            System.out.println("Creating sample tickets...");

            // Find customer users to assign tickets to
            User customer1 = userRepository.findByEmail("customer1@gmail.com").orElse(null);
            User customer2 = userRepository.findByEmail("customer2@gmail.com").orElse(null);
            User customer3 = userRepository.findByEmail("customer3@gmail.com").orElse(null);

            if (customer1 != null) {
                createTicket(customer1, "Parking Slot Reservation Issue",
                        "I booked a parking slot for tomorrow but haven't received a confirmation email. Could you please check my reservation status?",
                        Ticket.TicketStatus.OPEN, Ticket.TicketPriority.MEDIUM, "RESERVATION_ISSUE");

                createTicket(customer1, "Payment Gateway Error",
                        "I'm getting an error when trying to make payment for my parking reservation. The payment fails at the last step.",
                        Ticket.TicketStatus.IN_PROGRESS, Ticket.TicketPriority.HIGH, "PAYMENT_PROBLEM");
            }

            if (customer2 != null) {
                createTicket(customer2, "Unable to Access Account",
                        "I forgot my password and the reset link is not working. Please help me regain access to my account.",
                        Ticket.TicketStatus.OPEN, Ticket.TicketPriority.MEDIUM, "ACCOUNT_ISSUE");

                createTicket(customer2, "Parking Slot Occupied",
                        "When I arrived at my reserved parking slot B12, it was already occupied by another vehicle. Please resolve this issue.",
                        Ticket.TicketStatus.RESOLVED, Ticket.TicketPriority.HIGH, "PARKING_ISSUE");
            }

            if (customer3 != null) {
                createTicket(customer3, "Billing Discrepancy",
                        "I was charged twice for the same parking reservation on March 15th. Please refund the duplicate charge.",
                        Ticket.TicketStatus.OPEN, Ticket.TicketPriority.MEDIUM, "BILLING_INQUIRY");

                createTicket(customer3, "Mobile App Not Working",
                        "The mobile app keeps crashing when I try to view my reservations. This has been happening for the past 3 days.",
                        Ticket.TicketStatus.CLOSED, Ticket.TicketPriority.LOW, "TECHNICAL_SUPPORT");
            }

            System.out.println("Sample tickets created successfully!");
        } else {
            System.out.println("Tickets already exist in database. Skipping ticket initialization.");
        }
    }

    private void createTicket(User customer, String title, String description,
            Ticket.TicketStatus status, Ticket.TicketPriority priority, String category) {
        try {
            Ticket ticket = new Ticket();
            ticket.setTitle(title);
            ticket.setDescription(description);
            ticket.setStatus(status);
            ticket.setPriority(priority);
            ticket.setCategory(category);
            ticket.setCustomer(customer);

            ticketRepository.save(ticket);
            System.out.println("Created ticket: " + title + " for customer: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to create ticket " + title + ": " + e.getMessage());
        }
    }
}