package cleanmain;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Admin {
    private final Scanner sc = new Scanner(System.in);
    private final config db = new config();

    public void showDashboard() {
        int choiceAdmin = 0;
        do {
            printMenu();
            System.out.print("\nEnter your choice: ");
            choiceAdmin = readInt();
            switch (choiceAdmin) {
                case 1: viewAllUsers(); break;
                case 2: viewUsersByType(); break;
                case 3: approvePendingUsers(); break;
                case 4: viewEmployeeDetails(); break;
                case 5: updateEmployeeRole(); break;
                case 6: viewEmployeeTasks(); break;
                case 7: viewServices(); break;
                case 8: addService(); break;
                case 9: updateService(); break;
                case 10: deleteService(); break;
                case 11: viewOngoingBookings(); break;
                case 12: viewCompletedBookings(); break;
                case 13: System.out.println("\n✅ Logging out... Returning to main menu."); return;
                default: System.out.println("❌ Invalid choice! Please enter 1–13.");
            }
        } while (choiceAdmin != 13);
    }

    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       ADMIN DASHBOARD                 ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("USER MANAGEMENT:");
        System.out.println("  1. View All Users");
        System.out.println("  2. View Users by Type");
        System.out.println("  3. Approve Pending Users");
        System.out.println("\nEMPLOYEE MANAGEMENT:");
        System.out.println("  4. View Employee Details");
        System.out.println("  5. Update Employee Role");
        System.out.println("  6. View Employee Tasks");
        System.out.println("\nSERVICE MANAGEMENT:");
        System.out.println("  7. View Services");
        System.out.println("  8. Add Service");
        System.out.println("  9. Update Service");
        System.out.println("  10. Delete Service");
        System.out.println("\nBOOKING MONITORING:");
        System.out.println("  11. View Ongoing Bookings");
        System.out.println("  12. View Completed Bookings");
        System.out.println("\n  13. Logout");
    }

    private void viewAllUsers() {
        System.out.println("\n=== ALL USERS ===");
        String userQuery = "SELECT * FROM tbl_user";
        String[] userHeaders = {"ID", "Name", "Email", "Address", "Contact", "Type", "Status"};
        String[] userCols = {"u_id", "u_name", "u_email", "u_address", "u_contact", "u_type", "u_status"};
        db.viewRecords(userQuery, userHeaders, userCols);
        waitForReturn();
    }

    private void viewUsersByType() {
        System.out.println("\n=== VIEW USERS BY TYPE ===");
        System.out.println("1. Admin");
        System.out.println("2. Staff");
        System.out.println("3. Employee");
        System.out.print("Enter choice (1-3): ");
        int choice = readInt();
        String type;
        switch (choice) {
            case 1: type = "Admin"; break;
            case 2: type = "Staff"; break;
            case 3: type = "Employee"; break;
            default: System.out.println("❌ Invalid choice!"); waitForReturn(); return;
        }
        String query = "SELECT u_id, u_name, u_email, u_address, u_contact, u_status FROM tbl_user WHERE u_type = ?";
        String[] headers = {"ID", "Name", "Email", "Address", "Contact", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_address", "u_contact", "u_status"};
        List<Map<String, Object>> users = db.fetchRecords(query, type);
        if (users.isEmpty()) {
            System.out.println("No " + type + " users found.");
        } else {
            System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
            for (String header : headers) {
                System.out.print(String.format("%-20s", header));
            }
            System.out.println();
            System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
            for (Map<String, Object> user : users) {
                for (String col : cols) {
                    System.out.print(String.format("%-20s", (user.get(col) != null ? user.get(col) : "")));
                }
                System.out.println();
            }
            System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        }
        waitForReturn();
    }

    private void approvePendingUsers() {
        System.out.println("\n=== APPROVE PENDING USERS ===");
        String pendingQuery = "SELECT * FROM tbl_user WHERE u_status = 'Pending'";
        String[] headers = {"ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        db.viewRecords(pendingQuery, headers, cols);

        System.out.print("\nEnter User ID to approve (0 to cancel): ");
        int uid = readInt();
        if (uid == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        if (!isIdValid("tbl_user", "u_id", uid)) {
            System.out.println("❌ Invalid User ID!");
            waitForReturn();
            return;
        }
        String approve = "UPDATE tbl_user SET u_status = 'Approved' WHERE u_id = ?";
        try {
            db.updateRecord(approve, uid);
            System.out.println("✅ User approved successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to approve user: " + ex.getMessage());
        }
        waitForReturn();
    }

    private void viewEmployeeDetails() {
        System.out.println("\n=== EMPLOYEE DETAILS ===");
        String query = "SELECT u.u_id, u.u_name, u.u_email, e.e_role, e.e_status " +
                    "FROM tbl_user u " +
                    "JOIN tbl_employee e ON u.u_id = e.user_id " +
                    "WHERE u.u_type = 'Employee'";
        String[] headers = {"ID", "Name", "Email", "Role", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "e_role", "e_status"};
        db.viewRecords(query, headers, cols);
      
    }

    private void updateEmployeeRole() {
        viewEmployeeDetails();
        
        System.out.print("\nEnter Employee ID to update (0 to cancel): ");
        int empId = readInt();
        if (empId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        if (!isIdValid("tbl_employee", "user_id", empId)) {
            System.out.println("❌ Invalid Employee ID!");
            waitForReturn();
            return;
        }
        System.out.print("Enter new role: ");
        String newRole = readLine();
        String updateQuery = "UPDATE tbl_employee SET e_role = ? WHERE user_id = ?";
        try {
            db.updateRecord(updateQuery, newRole, empId);
            System.out.println("✅ Employee role updated successfully!");
        } catch (Exception ex) {
            System.out.println("❌ Failed to update employee role: " + ex.getMessage());
        }
        waitForReturn();
    }

    private void viewEmployeeTasks() {
        viewEmployeeDetails();
        
        System.out.print("\nEnter Employee ID to view tasks (0 to cancel): ");
        int empId = readInt();
        if (empId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        if (!isIdValid("tbl_employee", "user_id", empId)) {
            System.out.println("❌ Invalid Employee ID!");
            waitForReturn();
            return;
        }
        
        System.out.println("\n=== EMPLOYEE TASKS ===");
        String query = "SELECT t.t_id, s.s_name, t.t_status, t.t_date, b.cust_name, b.cust_contact " +
                "FROM tbl_task t " +
                "JOIN tbl_booking b ON t.booking_id = b.b_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "WHERE t.employee_id = ?";
        String[] headers = {"Task ID", "Service", "Status", "Date", "Customer", "Contact"};
        String[] cols = {"t_id", "s_name", "t_status", "t_date", "cust_name", "cust_contact"};
        List<Map<String, Object>> tasks = db.fetchRecords(query, empId);
        if (tasks.isEmpty()) {
            System.out.println("No tasks found for this employee.");
        } else {
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
            for (String header : headers) {
                System.out.print(String.format("%-15s", header));
            }
            System.out.println();
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
            for (Map<String, Object> task : tasks) {
                for (String col : cols) {
                    String value = (task.get(col) != null ? task.get(col).toString() : "");
                    System.out.print(String.format("%-15s", truncate(value, 15)));
                }
                System.out.println();
            }
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
        }
        waitForReturn();
    }

    // ---- SERVICE MANAGEMENT ----

    private void viewServices() {
        System.out.println("\n=== SERVICES ===");
        String query = "SELECT s_id, s_name, s_price FROM tbl_service";
        String[] headers = {"ID", "Name", "Price"};
        String[] cols = {"s_id", "s_name", "s_price"};
        db.viewRecords(query, headers, cols);

    }

    private void addService() {
        System.out.println("\n=== ADD SERVICE ===");
        System.out.print("Enter service name: ");
        String name = readLine();
        System.out.print("Enter service price: ");
        String price = readLine();
        String sql = "INSERT INTO tbl_service(s_name, s_price) VALUES (?, ?)";
        db.addRecord(sql, name, price);
        System.out.println("\n✅ Service added successfully!");
        waitForReturn();
    }

    private void updateService() {
        System.out.println("\n=== UPDATE SERVICE ===");
        viewServices();
        System.out.print("\nEnter Service ID to update (0 to cancel): ");
        int sId = readInt();
        if (sId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        if (!isIdValid("tbl_service", "s_id", sId)) {
            System.out.println("❌ Invalid Service ID!");
            waitForReturn();
            return;
        }
        System.out.print("Enter new name: ");
        String name = readLine();
        System.out.print("Enter new price: ");
        String price = readLine();
        String sql = "UPDATE tbl_service SET s_name = ?, s_price = ? WHERE s_id = ?";
        db.updateRecord(sql, name, price, sId);
        System.out.println("\n✅ Service updated successfully!");
        waitForReturn();
    }

    private void deleteService() {
        System.out.println("\n=== DELETE SERVICE ===");
        viewServices();
        System.out.print("\nEnter Service ID to delete (0 to cancel): ");
        int sId = readInt();
        if (sId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        if (!isIdValid("tbl_service", "s_id", sId)) {
            System.out.println("❌ Invalid Service ID!");
            waitForReturn();
            return;
        }
        String sql = "DELETE FROM tbl_service WHERE s_id = ?";
        db.deleteRecord(sql, sId);
        System.out.println("\n✅ Service deleted successfully!");
        waitForReturn();
    }

    // ---- BOOKING MONITORING ----

    private void viewOngoingBookings() {
        System.out.println("\n=== ONGOING BOOKINGS ===");
        System.out.println("(Bookings currently being handled by employees)");
        // FIXED: Changed from 'Ongoing' to 'Confirmed' and 'Pending'
        String query = "SELECT b.b_id, b.cust_name, b.cust_contact, s.s_name, b.b_date, st.u_name as staff, e.u_name as employee, t.t_status " +
                "FROM tbl_booking b " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "JOIN tbl_user st ON b.staff_id = st.u_id " +
                "JOIN tbl_user e ON b.employee_id = e.u_id " +
                "LEFT JOIN tbl_task t ON b.b_id = t.booking_id " +
                "WHERE b.b_status IN ('Pending', 'Confirmed') " +
                "ORDER BY b.b_date DESC";
        String[] headers = {"ID", "Customer", "Contact", "Service", "Date", "Staff", "Employee", "Task Status"};
        String[] cols = {"b_id", "cust_name", "cust_contact", "s_name", "b_date", "staff", "employee", "t_status"};
        db.viewRecords(query, headers, cols);
        waitForReturn();
    }

    private void viewCompletedBookings() {
        System.out.println("\n=== COMPLETED BOOKINGS ===");
        System.out.println("(Tasks that have been finished and marked as done)");
        String query = "SELECT b.b_id, b.cust_name, b.cust_contact, s.s_name, s.s_price, b.b_date, st.u_name as staff, e.u_name as employee, t.t_date as completed_date " +
                "FROM tbl_booking b " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "JOIN tbl_user st ON b.staff_id = st.u_id " +
                "JOIN tbl_user e ON b.employee_id = e.u_id " +
                "LEFT JOIN tbl_task t ON b.b_id = t.booking_id " +
                "WHERE b.b_status = 'Completed' " +
                "ORDER BY t.t_date DESC";
        String[] headers = {"ID", "Customer", "Contact", "Service", "Price", "Booking Date", "Staff", "Employee", "Completed"};
        String[] cols = {"b_id", "cust_name", "cust_contact", "s_name", "s_price", "b_date", "staff", "employee", "completed_date"};
        db.viewRecords(query, headers, cols);
        waitForReturn();
    }

    // ---- HELPER FUNCTIONS ----

    private void waitForReturn() {
        System.out.println("\nPress Enter to return to the main menu...");
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
}