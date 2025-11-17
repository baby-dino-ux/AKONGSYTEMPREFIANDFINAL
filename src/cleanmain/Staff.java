package cleanmain;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Staff {
    private final Scanner sc = new Scanner(System.in);
    private final config db = new config();
    private final int staffId;

    public Staff(int staffId) {
        this.staffId = staffId;
    }

    public void showDashboard() {
        int choiceStaff = 0;
        do {
            printMenu();
            System.out.print("\nEnter your choice: ");
            choiceStaff = readInt();

            switch (choiceStaff) {
                case 1: viewServicePackages(); break;
                case 2: viewEmployeeAvailability(); break;
                case 3: createBookingWithTask(); break;
                case 4: viewMyBookings(); break;
                case 5: generateReceipt(); break;
                case 6: System.out.println("Logging out... Returning to main menu."); return;
                default: System.out.println("❌ Invalid choice! Please enter 1–6.");
            }
        } while (choiceStaff != 6);
    }

    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       STAFF DASHBOARD                 ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("1. View Service Packages");
        System.out.println("2. View Employee Availability");
        System.out.println("3. Create Booking (Auto-creates Task)");
        System.out.println("4. View My Bookings");
        System.out.println("5. Generate Receipt for Completed Task");
        System.out.println("6. Logout");
    }

    private void viewServicePackages() {
        System.out.println("\n=== SERVICE PACKAGES ===");
        String query = "SELECT s_id, s_name, s_price FROM tbl_service";
        String[] headers = {"ID", "Name", "Price"};
        String[] cols = {"s_id", "s_name", "s_price"};
        db.viewRecords(query, headers, cols);
        waitForReturn();
    }

    private void viewEmployeeAvailability() {
        System.out.println("\n=== EMPLOYEE AVAILABILITY ===");
        String query = "SELECT u.u_id, u.u_name, e.e_role, e.e_status FROM tbl_user u JOIN tbl_employee e ON u.u_id = e.user_id WHERE u.u_type = 'Employee' AND u.u_status = 'Approved'";
        String[] headers = {"ID", "Name", "Role", "Status"};
        String[] cols = {"u_id", "u_name", "e_role", "e_status"};
        db.viewRecords(query, headers, cols);
        waitForReturn();
    }

    private void createBookingWithTask() {
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("    CREATE BOOKING (AUTO-GENERATES TASK)");
        System.out.println("═══════════════════════════════════════════════");
        
        // Get customer information
        System.out.print("Enter Customer Name: ");
        String custName = readLine();
        if (custName.isEmpty()) {
            System.out.println("❌ Customer name cannot be empty!");
            waitForReturn();
            return;
        }
        
        System.out.print("Enter Customer Email: ");
        String custEmail = readLine();
        if (custEmail.isEmpty()) {
            System.out.println("❌ Customer email cannot be empty!");
            waitForReturn();
            return;
        }
        
        System.out.print("Enter Customer Address: ");
        String custAddress = readLine();
        
        System.out.print("Enter Customer Contact Number: ");
        String custContact = readLine();
        if (custContact.isEmpty()) {
            System.out.println("❌ Customer contact cannot be empty!");
            waitForReturn();
            return;
        }

        System.out.println("\nAVAILABLE SERVICES:");
        String servQuery = "SELECT s_id, s_name, s_price FROM tbl_service";
        String[] servHeaders = {"ID", "Name", "Price"};
        String[] servCols = {"s_id", "s_name", "s_price"};
        db.viewRecords(servQuery, servHeaders, servCols);
        
        System.out.print("\nEnter Service ID: ");
        int serviceId = readInt();
        if (!isIdValid("tbl_service", "s_id", serviceId)) {
            System.out.println("❌ Invalid Service ID!");
            waitForReturn();
            return;
        }

        System.out.println("\nAVAILABLE EMPLOYEES:");
        String availableEmpQuery = "SELECT u.u_id, u.u_name, e.e_role, e.e_status FROM tbl_user u JOIN tbl_employee e ON u.u_id = e.user_id WHERE e.e_status = 'Available' AND u.u_type = 'Employee' AND u.u_status = 'Approved'";
        String[] aEmpHeaders = {"ID", "Name", "Role", "Status"};
        String[] aEmpCols = {"u_id", "u_name", "e_role", "e_status"};
        db.viewRecords(availableEmpQuery, aEmpHeaders, aEmpCols);

        System.out.print("\nEnter Employee ID to assign: ");
        int employeeId = readInt();
        if (!isEmployeeValid(employeeId)) {
            System.out.println("❌ Invalid Employee ID or employee not available!");
            waitForReturn();
            return;
        }

        System.out.print("Enter Booking Date (YYYY-MM-DD): ");
        String bookingDate = readLine();
        if (bookingDate.isEmpty()) {
            System.out.println("❌ Booking date cannot be empty!");
            waitForReturn();
            return;
        }
        
        System.out.print("Enter Task Notes (optional): ");
        String taskNotes = readLine();

        try {
            // Create booking with customer info and status "Confirmed" (FIXED: was "Ongoing")
            String addBooking = "INSERT INTO tbl_booking(cust_name, cust_email, cust_address, cust_contact, service_id, employee_id, b_date, b_status, staff_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            db.addRecord(addBooking, custName, custEmail, custAddress, custContact, serviceId, employeeId, bookingDate, "Confirmed", staffId);

            // Get the newly created booking ID
            String getBookingId = "SELECT b_id FROM tbl_booking WHERE cust_email = ? AND service_id = ? AND employee_id = ? AND b_date = ? ORDER BY b_id DESC LIMIT 1";
            List<Map<String, Object>> result = db.fetchRecords(getBookingId, custEmail, serviceId, employeeId, bookingDate);
            
            if (result.isEmpty()) {
                throw new Exception("Failed to retrieve booking ID.");
            }
            
            int bookingId = Integer.parseInt(result.get(0).get("b_id").toString());

            // Automatically create task for the employee
            String addTask = "INSERT INTO tbl_task(booking_id, employee_id, t_date, t_notes, t_status) VALUES (?, ?, ?, ?, ?)";
            db.addRecord(addTask, bookingId, employeeId, bookingDate, taskNotes, "Pending");

            System.out.println("\n✅ Booking and Task Created Successfully!");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("Booking ID:  " + bookingId);
            System.out.println("Customer:    " + custName);
            System.out.println("Service ID:  " + serviceId);
            System.out.println("Employee ID: " + employeeId);
            System.out.println("Date:        " + bookingDate);
            System.out.println("Status:      Confirmed");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("\nNote: Task automatically assigned to employee.");
            waitForReturn();
        } catch (Exception ex) {
            System.out.println("❌ Failed to create booking and task: " + ex.getMessage());
            waitForReturn();
        }
    }

    private void viewMyBookings() {
        System.out.println("\n=== MY BOOKINGS ===");
        String query = "SELECT b.b_id, b.cust_name, b.cust_contact, s.s_name, u.u_name as employee, b.b_date, b.b_status " +
            "FROM tbl_booking b " +
            "JOIN tbl_service s ON b.service_id = s.s_id " +
            "JOIN tbl_user u ON b.employee_id = u.u_id " +
            "WHERE b.staff_id = ? ORDER BY b.b_date DESC";
        List<Map<String, Object>> bookings = db.fetchRecords(query, staffId);
        if (bookings == null || bookings.isEmpty()) {
            System.out.println("No bookings found.");
        } else {
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════");
            System.out.printf("%-5s %-20s %-15s %-20s %-15s %-12s %-10s%n", "ID", "Customer", "Contact", "Service", "Employee", "Date", "Status");
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════");
            for (Map<String, Object> booking : bookings) {
                System.out.printf("%-5s %-20s %-15s %-20s %-15s %-12s %-10s%n",
                    safeGet(booking, "b_id"),
                    truncate(safeGet(booking, "cust_name"), 20),
                    truncate(safeGet(booking, "cust_contact"), 15),
                    truncate(safeGet(booking, "s_name"), 20),
                    truncate(safeGet(booking, "employee"), 15),
                    safeGet(booking, "b_date"),
                    safeGet(booking, "b_status"));
            }
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════");
        }
        waitForReturn();
    }

    private void generateReceipt() {
        System.out.println("\n=== GENERATE RECEIPT ===");
        
        // Show completed tasks that haven't been receipted yet (FIXED: was checking for 'Ongoing')
        String completedQuery = "SELECT t.t_id, b.b_id, b.cust_name, b.cust_email, b.cust_contact, s.s_name, s.s_price, u.u_name as employee, t.t_date " +
            "FROM tbl_task t " +
            "JOIN tbl_booking b ON t.booking_id = b.b_id " +
            "JOIN tbl_service s ON b.service_id = s.s_id " +
            "JOIN tbl_user u ON t.employee_id = u.u_id " +
            "WHERE b.staff_id = ? AND t.t_status = 'Completed' AND b.b_status = 'Confirmed' ORDER BY t.t_date DESC";
        List<Map<String, Object>> completed = db.fetchRecords(completedQuery, staffId);
        
        if (completed.isEmpty()) {
            System.out.println("No completed tasks available for receipt generation.");
            waitForReturn();
            return;
        }
        
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-8s %-10s %-20s %-25s %-12s%n", "Task ID", "Book ID", "Customer", "Service", "Price");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        for (Map<String, Object> task : completed) {
            System.out.printf("%-8s %-10s %-20s %-25s %-12s%n",
                safeGet(task, "t_id"),
                safeGet(task, "b_id"),
                truncate(safeGet(task, "cust_name"), 20),
                truncate(safeGet(task, "s_name"), 25),
                safeGet(task, "s_price"));
        }
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter Task ID to generate receipt (0 to cancel): ");
        int taskId = readInt();
        if (taskId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        
        // Get task details
        String receiptQuery = "SELECT t.t_id, b.b_id, b.cust_name, b.cust_email, b.cust_address, b.cust_contact, s.s_name, s.s_price, u.u_name as employee, t.t_date, b.b_date " +
            "FROM tbl_task t " +
            "JOIN tbl_booking b ON t.booking_id = b.b_id " +
            "JOIN tbl_service s ON b.service_id = s.s_id " +
            "JOIN tbl_user u ON t.employee_id = u.u_id " +
            "WHERE t.t_id = ? AND b.staff_id = ? AND t.t_status = 'Completed'";
        List<Map<String, Object>> receipt = db.fetchRecords(receiptQuery, taskId, staffId);
        
        if (receipt.isEmpty()) {
            System.out.println("❌ Invalid Task ID or task not completed!");
            waitForReturn();
            return;
        }
        
        Map<String, Object> data = receipt.get(0);
        int bookingId = Integer.parseInt(data.get("b_id").toString());
        
        // Print receipt
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         CLEANING SERVICE RECEIPT                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("Receipt Date: " + java.time.LocalDate.now());
        System.out.println("Booking ID:   " + safeGet(data, "b_id"));
        System.out.println("Task ID:      " + safeGet(data, "t_id"));
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("CUSTOMER INFORMATION:");
        System.out.println("  Name:    " + safeGet(data, "cust_name"));
        System.out.println("  Email:   " + safeGet(data, "cust_email"));
        System.out.println("  Address: " + safeGet(data, "cust_address"));
        System.out.println("  Contact: " + safeGet(data, "cust_contact"));
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("SERVICE DETAILS:");
        System.out.println("  Service:       " + safeGet(data, "s_name"));
        System.out.println("  Employee:      " + safeGet(data, "employee"));
        System.out.println("  Booking Date:  " + safeGet(data, "b_date"));
        System.out.println("  Completed:     " + safeGet(data, "t_date"));
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("TOTAL AMOUNT:  PHP " + safeGet(data, "s_price"));
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("         Thank you for your business!");
        System.out.println("════════════════════════════════════════════════════");
        
        // Update booking status to Completed
        String updateBooking = "UPDATE tbl_booking SET b_status = 'Completed' WHERE b_id = ?";
        db.updateRecord(updateBooking, bookingId);
        
        System.out.println("\n✅ Receipt generated and booking marked as Completed!");
        waitForReturn();
    }

    private void waitForReturn() {
        System.out.println("\nPress Enter to continue to the menu.");
        sc.nextLine();
    }

    private int readInt() {
        try {
            String line = sc.nextLine();
            return Integer.parseInt(line.trim());
        } catch (Exception ex) {
            return -1;
        }
    }

    private String readLine() {
        try {
            String line = sc.nextLine();
            return line.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String safeGet(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : v.toString();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private boolean isIdValid(String table, String col, int id) {
        String sql = "SELECT " + col + " FROM " + table + " WHERE " + col + " = ?";
        List<Map<String, Object>> res = db.fetchRecords(sql, id);
        return !res.isEmpty();
    }

    private boolean isEmployeeValid(int id) {
        String sql = "SELECT u.u_id FROM tbl_user u JOIN tbl_employee e ON u.u_id = e.user_id WHERE u.u_id = ? AND u.u_type = 'Employee' AND u.u_status = 'Approved' AND e.e_status = 'Available'";
        List<Map<String, Object>> res = db.fetchRecords(sql, id);
        return !res.isEmpty();
    }
}