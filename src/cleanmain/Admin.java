package cleanmain;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Admin {

    private final Scanner sc = new Scanner(System.in);
    private final config db = new config();

    /**
     * Main Admin Dashboard
     */
    public void Admin() {
        int choiceAdmin = 0;

        do {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║       ADMIN DASHBOARD                 ║");
            System.out.println("╚═══════════════════════════════════════╝");
            System.out.println("\n┌─── USER MANAGEMENT ───────────────────┐");
            System.out.println("│ 1. View All Users                     │");
            System.out.println("│ 2. Approve Pending Users              │");
            System.out.println("└───────────────────────────────────────┘");
            System.out.println("\n┌─── EMPLOYEE MANAGEMENT ───────────────┐");
            System.out.println("│ 3. View Employees                     │");
            System.out.println("│ 4. Add Employee                       │");
            System.out.println("│ 5. Update Employee                    │");
            System.out.println("│ 6. Delete Employee                    │");
            System.out.println("│ 7. Mark Employee Available            │");
            System.out.println("└───────────────────────────────────────┘");
            System.out.println("\n┌─── SERVICE MANAGEMENT ────────────────┐");
            System.out.println("│ 8. View Services                      │");
            System.out.println("│ 9. Add Service                        │");
            System.out.println("│ 10. Update Service                    │");
            System.out.println("│ 11. Delete Service                    │");
            System.out.println("└───────────────────────────────────────┘");
            System.out.println("\n┌─── BOOKING MANAGEMENT ────────────────┐");
            System.out.println("│ 12. View All Bookings                 │");
            System.out.println("└───────────────────────────────────────┘");
            System.out.println("\n┌─── SYSTEM ────────────────────────────┐");
            System.out.println("│ 13. Logout                            │");
            System.out.println("└───────────────────────────────────────┘");
            System.out.print("\nEnter your choice: ");

            choiceAdmin = readInt();

            switch (choiceAdmin) {
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    approvePendingUsers();
                    break;
                case 3:
                    viewEmployees();
                    break;
                case 4:
                    addEmployee();
                    break;
                case 5:
                    updateEmployee();
                    break;
                case 6:
                    deleteEmployee();
                    break;
                case 7:
                    markEmployeeAvailable();
                    break;
                case 8:
                    viewServices();
                    break;
                case 9:
                    addService();
                    break;
                case 10:
                    updateService();
                    break;
                case 11:
                    deleteService();
                    break;
                case 12:
                    viewBookings();
                    break;
                case 13:
                    System.out.println("\n✅ Logging out... Returning to main menu.");
                    return;
                default:
                    System.out.println("❌ Invalid choice! Please enter 1–13.");
            }
        } while (choiceAdmin != 13);
    }

    // ================================
    // USER MANAGEMENT
    // ================================

    /**
     * 1️⃣ View all registered users
     */
    private void viewAllUsers() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║         USER LIST                     ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String userQuery = "SELECT * FROM tbl_user";
        String[] userHeaders = {"ID", "Name", "Email", "Address", "Contact", "Type", "Status"};
        String[] userCols = {"u_id", "u_name", "u_email", "u_address", "u_contact", "u_type", "u_status"};
        db.viewRecords(userQuery, userHeaders, userCols);
    }

    /**
     * 2️⃣ Approve pending users
     */
    private void approvePendingUsers() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       PENDING USERS                   ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String pendingQuery = "SELECT * FROM tbl_user WHERE u_status = 'Pending'";
        String[] headers = {"ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        db.viewRecords(pendingQuery, headers, cols);

        System.out.print("\nEnter User ID to approve (0 to cancel): ");
        int uid = readInt();

        if (uid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        String approve = "UPDATE tbl_user SET u_status = ? WHERE u_id = ?";
        try {
            db.updateRecord(approve, "Approve", uid);
            System.out.println("✅ User approved successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to approve user: " + ex.getMessage());
        }
    }

    // ================================
    // EMPLOYEE MANAGEMENT
    // ================================

    /**
     * 3️⃣ View all employees
     */
    private void viewEmployees() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       EMPLOYEE LIST                   ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String empQuery = "SELECT * FROM tbl_employee";
        String[] empHeaders = {"ID", "Name", "Role", "Status"};
        String[] empCols = {"e_id", "e_name", "e_role", "e_status"};
        db.viewRecords(empQuery, empHeaders, empCols);
    }

    /**
     * 4️⃣ Add a new employee
     */
    private void addEmployee() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       ADD NEW EMPLOYEE                ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        System.out.print("Enter Employee Name: ");
        String name = readLine();

        System.out.print("Enter Employee Role: ");
        String role = readLine();

        String sql = "INSERT INTO tbl_employee(e_name, e_role, e_status) VALUES (?, ?, ?)";
        try {
            db.addRecord(sql, name, role, "Available");
            System.out.println("✅ Employee added successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to add employee: " + ex.getMessage());
        }
    }

    /**
     * 5️⃣ Update employee information
     */
    private void updateEmployee() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       UPDATE EMPLOYEE                 ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        viewEmployees();
        
        System.out.print("\nEnter Employee ID to update (0 to cancel): ");
        int eid = readInt();
        
        if (eid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Check if employee exists
        String checkQuery = "SELECT * FROM tbl_employee WHERE e_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(checkQuery, eid);
        
        if (result.isEmpty()) {
            System.out.println("❌ Employee ID not found!");
            return;
        }

        System.out.println("\nWhat would you like to update?");
        System.out.println("1. Name");
        System.out.println("2. Role");
        System.out.println("3. Status");
        System.out.println("4. Update All");
        System.out.print("Enter choice: ");
        int updateChoice = readInt();

        try {
            switch (updateChoice) {
                case 1:
                    System.out.print("Enter new Name: ");
                    String newName = readLine();
                    db.updateRecord("UPDATE tbl_employee SET e_name = ? WHERE e_id = ?", newName, eid);
                    System.out.println("✅ Employee name updated successfully!");
                    break;
                    
                case 2:
                    System.out.print("Enter new Role: ");
                    String newRole = readLine();
                    db.updateRecord("UPDATE tbl_employee SET e_role = ? WHERE e_id = ?", newRole, eid);
                    System.out.println("✅ Employee role updated successfully!");
                    break;
                    
                case 3:
                    System.out.println("Select Status:");
                    System.out.println("1. Available");
                    System.out.println("2. Busy");
                    System.out.print("Enter choice: ");
                    int statusChoice = readInt();
                    String newStatus = (statusChoice == 1) ? "Available" : "Busy";
                    db.updateRecord("UPDATE tbl_employee SET e_status = ? WHERE e_id = ?", newStatus, eid);
                    System.out.println("✅ Employee status updated successfully!");
                    break;
                    
                case 4:
                    System.out.print("Enter new Name: ");
                    String name = readLine();
                    System.out.print("Enter new Role: ");
                    String role = readLine();
                    System.out.println("Select Status:");
                    System.out.println("1. Available");
                    System.out.println("2. Busy");
                    System.out.print("Enter choice: ");
                    int sChoice = readInt();
                    String status = (sChoice == 1) ? "Available" : "Busy";
                    
                    db.updateRecord("UPDATE tbl_employee SET e_name = ?, e_role = ?, e_status = ? WHERE e_id = ?", 
                                  name, role, status, eid);
                    System.out.println("✅ Employee updated successfully!");
                    break;
                    
                default:
                    System.out.println("❌ Invalid choice!");
            }
        } catch (Exception ex) {
            System.out.println("❌ Failed to update employee: " + ex.getMessage());
        }
    }

    /**
     * 6️⃣ Delete an employee
     */
    private void deleteEmployee() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       DELETE EMPLOYEE                 ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        viewEmployees();
        
        System.out.print("\nEnter Employee ID to delete (0 to cancel): ");
        int eid = readInt();
        
        if (eid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Check if employee has any bookings
        String checkBookings = "SELECT COUNT(*) as count FROM tbl_booking WHERE employee_id = ?";
        List<Map<String, Object>> bookingCheck = db.fetchRecords(checkBookings, eid);
        
        if (!bookingCheck.isEmpty()) {
            int bookingCount = Integer.parseInt(bookingCheck.get(0).get("count").toString());
            if (bookingCount > 0) {
                System.out.println("⚠️  Warning: This employee has " + bookingCount + " booking(s)!");
                System.out.print("Are you sure you want to delete? (yes/no): ");
                String confirm = readLine();
                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
            }
        }

        System.out.print("Type 'DELETE' to confirm deletion: ");
        String confirm = readLine();
        
        if (confirm.equals("DELETE")) {
            String deleteSql = "DELETE FROM tbl_employee WHERE e_id = ?";
            try {
                db.deleteRecord(deleteSql, eid);
                System.out.println("✅ Employee deleted successfully!");
            } catch (Exception ex) {
                System.out.println("❌ Failed to delete employee: " + ex.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    /**
     * 7️⃣ Mark employee available again
     */
    private void markEmployeeAvailable() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       BUSY EMPLOYEES                  ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String busyQuery = "SELECT * FROM tbl_employee WHERE e_status = 'Busy'";
        String[] headers = {"ID", "Name", "Role", "Status"};
        String[] cols = {"e_id", "e_name", "e_role", "e_status"};
        db.viewRecords(busyQuery, headers, cols);

        System.out.print("\nEnter Employee ID to mark as Available (0 to cancel): ");
        int eid = readInt();

        if (eid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        String update = "UPDATE tbl_employee SET e_status = ? WHERE e_id = ?";
        try {
            db.updateRecord(update, "Available", eid);
            System.out.println("✅ Employee marked as Available!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to update employee: " + ex.getMessage());
        }
    }

    // ================================
    // SERVICE MANAGEMENT
    // ================================

    /**
     * 8️⃣ View all services
     */
    private void viewServices() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       SERVICE LIST                    ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String svcQuery = "SELECT * FROM tbl_service";
        String[] svcHeaders = {"ID", "Name", "Description", "Price"};
        String[] svcCols = {"s_id", "s_name", "s_description", "s_price"};
        db.viewRecords(svcQuery, svcHeaders, svcCols);
    }

    /**
     * 9️⃣ Add a new service
     */
    private void addService() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       ADD NEW SERVICE                 ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        System.out.print("Enter Service Name: ");
        String sName = readLine();

        System.out.print("Enter Description: ");
        String desc = readLine();

        System.out.print("Enter Price (₱): ");
        double price = readDouble();

        String sql = "INSERT INTO tbl_service(s_name, s_description, s_price) VALUES (?, ?, ?)";
        try {
            db.addRecord(sql, sName, desc, price);
            System.out.println("✅ Service added successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to add service: " + ex.getMessage());
        }
    }

    /**
     * 🔟 Update service information
     */
    private void updateService() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       UPDATE SERVICE                  ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        viewServices();
        
        System.out.print("\nEnter Service ID to update (0 to cancel): ");
        int sid = readInt();
        
        if (sid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Check if service exists
        String checkQuery = "SELECT * FROM tbl_service WHERE s_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(checkQuery, sid);
        
        if (result.isEmpty()) {
            System.out.println("❌ Service ID not found!");
            return;
        }

        System.out.println("\nWhat would you like to update?");
        System.out.println("1. Name");
        System.out.println("2. Description");
        System.out.println("3. Price");
        System.out.println("4. Update All");
        System.out.print("Enter choice: ");
        int updateChoice = readInt();

        try {
            switch (updateChoice) {
                case 1:
                    System.out.print("Enter new Service Name: ");
                    String newName = readLine();
                    db.updateRecord("UPDATE tbl_service SET s_name = ? WHERE s_id = ?", newName, sid);
                    System.out.println("✅ Service name updated successfully!");
                    break;
                    
                case 2:
                    System.out.print("Enter new Description: ");
                    String newDesc = readLine();
                    db.updateRecord("UPDATE tbl_service SET s_description = ? WHERE s_id = ?", newDesc, sid);
                    System.out.println("✅ Service description updated successfully!");
                    break;
                    
                case 3:
                    System.out.print("Enter new Price (₱): ");
                    double newPrice = readDouble();
                    db.updateRecord("UPDATE tbl_service SET s_price = ? WHERE s_id = ?", newPrice, sid);
                    System.out.println("✅ Service price updated successfully!");
                    break;
                    
                case 4:
                    System.out.print("Enter new Service Name: ");
                    String name = readLine();
                    System.out.print("Enter new Description: ");
                    String desc = readLine();
                    System.out.print("Enter new Price (₱): ");
                    double price = readDouble();
                    
                    db.updateRecord("UPDATE tbl_service SET s_name = ?, s_description = ?, s_price = ? WHERE s_id = ?", 
                                  name, desc, price, sid);
                    System.out.println("✅ Service updated successfully!");
                    break;
                    
                default:
                    System.out.println("❌ Invalid choice!");
            }
        } catch (Exception ex) {
            System.out.println("❌ Failed to update service: " + ex.getMessage());
        }
    }

    /**
     * 1️⃣1️⃣ Delete a service
     */
    private void deleteService() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       DELETE SERVICE                  ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        viewServices();
        
        System.out.print("\nEnter Service ID to delete (0 to cancel): ");
        int sid = readInt();
        
        if (sid == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Check if service has any bookings
        String checkBookings = "SELECT COUNT(*) as count FROM tbl_booking WHERE service_id = ?";
        List<Map<String, Object>> bookingCheck = db.fetchRecords(checkBookings, sid);
        
        if (!bookingCheck.isEmpty()) {
            int bookingCount = Integer.parseInt(bookingCheck.get(0).get("count").toString());
            if (bookingCount > 0) {
                System.out.println("⚠️  Warning: This service has " + bookingCount + " booking(s)!");
                System.out.print("Are you sure you want to delete? (yes/no): ");
                String confirm = readLine();
                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
            }
        }

        System.out.print("Type 'DELETE' to confirm deletion: ");
        String confirm = readLine();
        
        if (confirm.equals("DELETE")) {
            String deleteSql = "DELETE FROM tbl_service WHERE s_id = ?";
            try {
                db.deleteRecord(deleteSql, sid);
                System.out.println("✅ Service deleted successfully!");
            } catch (Exception ex) {
                System.out.println("❌ Failed to delete service: " + ex.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ================================
    // BOOKING MANAGEMENT
    // ================================

    /**
     * 1️⃣2️⃣ View all bookings
     */
    private void viewBookings() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       ALL BOOKINGS                    ║");
        System.out.println("╚═══════════════════════════════════════╝");
        String bQuery = "SELECT b.b_id, u.u_name, s.s_name, e.e_name, b.b_date, b.b_status " +
                "FROM tbl_booking b " +
                "JOIN tbl_user u ON b.user_id = u.u_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "JOIN tbl_employee e ON b.employee_id = e.e_id";
        String[] headers = {"Booking ID", "Customer", "Service", "Employee", "Date", "Status"};
        String[] cols = {"b_id", "u_name", "s_name", "e_name", "b_date", "b_status"};
        db.viewRecords(bQuery, headers, cols);
    }

    // ================================
    // Helper Input Functions
    // ================================

    private int readInt() {
        try {
            String line = sc.nextLine();
            return Integer.parseInt(line.trim());
        } catch (Exception ex) {
            return -1;
        }
    }

    private double readDouble() {
        try {
            String line = sc.nextLine();
            return Double.parseDouble(line.trim());
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private String readLine() {
        try {
            String line = sc.nextLine();
            if (line.isEmpty()) {
                line = sc.nextLine();
            }
            return line.trim();
        } catch (Exception ex) {
            return "";
        }
    }
}