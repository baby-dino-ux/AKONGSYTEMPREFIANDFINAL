package cleanmain;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Employee {
    private final Scanner sc = new Scanner(System.in);
    private final config db = new config();
    private final int userId;

    public Employee(int userId) {
        this.userId = userId;
    }

    public void showDashboard() {
        int choice = 0;
        do {
            printMenu();
            System.out.print("\nEnter your choice: ");
            choice = readInt();

            switch (choice) {
                case 1: viewMyTasks(); break;
                case 2: acceptTask(); break;
                case 3: markTaskComplete(); break;
                case 4: manageMyStatus(); break;
                case 5: viewMyStatus(); break;
                case 6: System.out.println("Logging out... Returning to main menu."); return;
                default: System.out.println("❌ Invalid choice! Please enter 1–6.");
            }
        } while (choice != 6);
    }

    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║       EMPLOYEE DASHBOARD              ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println("1. View My Tasks");
        System.out.println("2. Accept Task");
        System.out.println("3. Mark Task as Completed");
        System.out.println("4. Manage My Status (Available/Busy)");
        System.out.println("5. View My Status Summary");
        System.out.println("6. Logout");
    }

    private void viewMyTasks() {
        System.out.println("\n=== MY TASKS ===");
        String taskQuery = "SELECT t.t_id, s.s_name, t.t_status, t.t_date, b.cust_name, b.cust_contact, t.t_notes " +
                "FROM tbl_task t " +
                "JOIN tbl_booking b ON t.booking_id = b.b_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "WHERE t.employee_id = ? ORDER BY t.t_date DESC";
        List<Map<String, Object>> tasks = db.fetchRecords(taskQuery, userId);
        
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks assigned to you.");
        } else {
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n", "ID", "Service", "Status", "Customer", "Contact", "Date", "Notes");
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            for (Map<String, Object> task : tasks) {
                System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n",
                    safeGet(task, "t_id"),
                    truncate(safeGet(task, "s_name"), 20),
                    safeGet(task, "t_status"),
                    truncate(safeGet(task, "cust_name"), 20),
                    truncate(safeGet(task, "cust_contact"), 15),
                    safeGet(task, "t_date"),
                    truncate(safeGet(task, "t_notes"), 25));
            }
            System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        }
        waitForReturn();
    }

    private void acceptTask() {
        System.out.println("\n=== ACCEPT TASK ===");
        
        // Check current status
        String statusCheck = "SELECT e_status FROM tbl_employee WHERE user_id = ?";
        List<Map<String, Object>> statusResult = db.fetchRecords(statusCheck, userId);
        if (!statusResult.isEmpty()) {
            String currentStatus = statusResult.get(0).get("e_status").toString();
            if (currentStatus.equals("Busy")) {
                System.out.println("⚠️  Your status is currently 'Busy'.");
                System.out.println("You can still accept tasks, but consider managing your workload.");
            }
        }
        
        // Show pending tasks
        String pendingQuery = "SELECT t.t_id, s.s_name, t.t_status, t.t_date, b.cust_name, b.cust_contact, t.t_notes " +
                "FROM tbl_task t " +
                "JOIN tbl_booking b ON t.booking_id = b.b_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "WHERE t.employee_id = ? AND t.t_status = 'Pending' ORDER BY t.t_date DESC";
        List<Map<String, Object>> tasks = db.fetchRecords(pendingQuery, userId);
        
        if (tasks.isEmpty()) {
            System.out.println("No pending tasks to accept.");
            waitForReturn();
            return;
        }
        
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n", "ID", "Service", "Status", "Customer", "Contact", "Date", "Notes");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        for (Map<String, Object> task : tasks) {
            System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n",
                safeGet(task, "t_id"),
                truncate(safeGet(task, "s_name"), 20),
                safeGet(task, "t_status"),
                truncate(safeGet(task, "cust_name"), 20),
                truncate(safeGet(task, "cust_contact"), 15),
                safeGet(task, "t_date"),
                truncate(safeGet(task, "t_notes"), 25));
        }
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter Task ID to accept (0 to cancel): ");
        int taskId = readInt();
        if (taskId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        
        if (!isTaskValidForEmployee(taskId)) {
            System.out.println("❌ Invalid Task ID or task not assigned to you!");
            waitForReturn();
            return;
        }
        
        // Accept task by updating status to 'Approved' (FIXED: was 'Accepted')
        String updateQuery = "UPDATE tbl_task SET t_status = 'Approved' WHERE t_id = ? AND employee_id = ?";
        db.updateRecord(updateQuery, taskId, userId);
        
        // Update employee status to Busy
        String updateStatus = "UPDATE tbl_employee SET e_status = 'Busy' WHERE user_id = ?";
        db.updateRecord(updateStatus, userId);
        
        System.out.println("✅ Task accepted! Your status is now 'Busy'.");
        waitForReturn();
    }

    private void markTaskComplete() {
        System.out.println("\n=== MARK TASK AS COMPLETED ===");
        
        // Show approved tasks (FIXED: was checking for 'Accepted')
        String approvedQuery = "SELECT t.t_id, s.s_name, t.t_status, t.t_date, b.cust_name, b.cust_contact, t.t_notes " +
                "FROM tbl_task t " +
                "JOIN tbl_booking b ON t.booking_id = b.b_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "WHERE t.employee_id = ? AND t.t_status = 'Approved' ORDER BY t.t_date DESC";
        List<Map<String, Object>> tasks = db.fetchRecords(approvedQuery, userId);
        
        if (tasks.isEmpty()) {
            System.out.println("No approved tasks to complete.");
            waitForReturn();
            return;
        }
        
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n", "ID", "Service", "Status", "Customer", "Contact", "Date", "Notes");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        for (Map<String, Object> task : tasks) {
            System.out.printf("%-5s %-20s %-12s %-20s %-15s %-12s %-25s%n",
                safeGet(task, "t_id"),
                truncate(safeGet(task, "s_name"), 20),
                safeGet(task, "t_status"),
                truncate(safeGet(task, "cust_name"), 20),
                truncate(safeGet(task, "cust_contact"), 15),
                safeGet(task, "t_date"),
                truncate(safeGet(task, "t_notes"), 25));
        }
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter Task ID to mark as completed (0 to cancel): ");
        int taskId = readInt();
        if (taskId == 0) {
            System.out.println("Operation cancelled.");
            waitForReturn();
            return;
        }
        
        if (!isTaskValidForEmployee(taskId)) {
            System.out.println("❌ Invalid Task ID or task not assigned to you!");
            waitForReturn();
            return;
        }
        
        String updateQuery = "UPDATE tbl_task SET t_status = 'Completed' WHERE t_id = ? AND employee_id = ?";
        db.updateRecord(updateQuery, taskId, userId);
        
        // Check if employee has any more approved or pending tasks (FIXED: was checking for 'Accepted')
        String checkTasks = "SELECT COUNT(*) as cnt FROM tbl_task WHERE employee_id = ? AND t_status IN ('Approved', 'Pending')";
        List<Map<String, Object>> result = db.fetchRecords(checkTasks, userId);
        int remainingTasks = (!result.isEmpty()) ? Integer.parseInt(result.get(0).get("cnt").toString()) : 0;
        
        if (remainingTasks == 0) {
            // Automatically set to Available if no more tasks
            String updateStatus = "UPDATE tbl_employee SET e_status = 'Available' WHERE user_id = ?";
            db.updateRecord(updateStatus, userId);
            System.out.println("✅ Task marked as completed! Your status is now 'Available'.");
        } else {
            System.out.println("✅ Task marked as completed! You still have " + remainingTasks + " task(s) remaining.");
            System.out.println("Your status remains 'Busy'.");
        }
        waitForReturn();
    }

    private void manageMyStatus() {
        System.out.println("\n=== MANAGE MY STATUS ===");
        
        // Show current status
        String statusQuery = "SELECT e_status FROM tbl_employee WHERE user_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(statusQuery, userId);
        if (!result.isEmpty()) {
            String currentStatus = result.get(0).get("e_status").toString();
            System.out.println("Current Status: " + currentStatus);
        }
        
        // Check active tasks (FIXED: was checking for 'Accepted')
        String taskCount = "SELECT COUNT(*) as cnt FROM tbl_task WHERE employee_id = ? AND t_status IN ('Approved', 'Pending')";
        List<Map<String, Object>> taskResult = db.fetchRecords(taskCount, userId);
        int activeTasks = (!taskResult.isEmpty()) ? Integer.parseInt(taskResult.get(0).get("cnt").toString()) : 0;
        
        System.out.println("Active Tasks: " + activeTasks);
        System.out.println("\nNote: You can manually set your status based on how many tasks you wish to accept today.");
        System.out.println("Setting status to 'Busy' will signal that you're at capacity.");
        
        System.out.println("\nSelect new status:");
        System.out.println("1. Available");
        System.out.println("2. Busy");
        System.out.print("Enter choice (1-2): ");
        int choice = readInt();
        
        String newStatus;
        if (choice == 1) {
            newStatus = "Available";
        } else if (choice == 2) {
            newStatus = "Busy";
        } else {
            System.out.println("❌ Invalid choice!");
            waitForReturn();
            return;
        }
        
        String updateStatus = "UPDATE tbl_employee SET e_status = ? WHERE user_id = ?";
        db.updateRecord(updateStatus, newStatus, userId);
        System.out.println("✅ Your status has been updated to: " + newStatus);
        waitForReturn();
    }

    private void viewMyStatus() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║     EMPLOYEE STATUS SUMMARY            ║");
        System.out.println("╚═══════════════════════════════════════╝");
        
        String statusQuery = "SELECT e.e_status, e.e_role, " +
                "(SELECT COUNT(*) FROM tbl_task t WHERE t.employee_id = ? AND t.t_status = 'Pending') as pending_tasks, " +
                "(SELECT COUNT(*) FROM tbl_task t WHERE t.employee_id = ? AND t.t_status = 'Approved') as approved_tasks, " +
                "(SELECT COUNT(*) FROM tbl_task t WHERE t.employee_id = ? AND t.t_status = 'Completed') as completed_tasks " +
                "FROM tbl_user u " +
                "JOIN tbl_employee e ON u.u_id = e.user_id " +
                "WHERE u.u_id = ?";
        List<Map<String, Object>> status = db.fetchRecords(statusQuery, userId, userId, userId, userId);
        
        if (!status.isEmpty()) {
            Map<String, Object> s = status.get(0);
            System.out.println("Current Status:     " + safeGet(s, "e_status"));
            System.out.println("Role:               " + safeGet(s, "e_role"));
            System.out.println("════════════════════════════════════════");
            System.out.println("Pending Tasks:      " + safeGet(s, "pending_tasks"));
            System.out.println("Approved Tasks:     " + safeGet(s, "approved_tasks"));
            System.out.println("Completed Tasks:    " + safeGet(s, "completed_tasks"));
            System.out.println("════════════════════════════════════════");
        }
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

    private boolean isTaskValidForEmployee(int taskId) {
        String sql = "SELECT t_id FROM tbl_task WHERE t_id = ? AND employee_id = ?";
        List<Map<String, Object>> res = db.fetchRecords(sql, taskId, userId);
        return !res.isEmpty();
    }
}