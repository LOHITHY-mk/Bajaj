package com.example.bfh.logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SqlSolver {

    /**
     * Determine which question applies based on the last two digits of regNo.
     * Odd -> Question 1, Even -> Question 2
     */
    public static String buildFinalQuery(String regNo) {
        int lastTwo = extractLastTwoDigits(regNo);
        boolean isOdd = (lastTwo % 2) == 1;
        if (isOdd) {
            return question1();
        } else {
            return question2();
        }
    }

    /**
     * Extract the last two digits from any alphanumeric registration number.
     * If not found, defaults to 1 (odd).
     */
    static int extractLastTwoDigits(String regNo) {
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            return Integer.parseInt(digits.substring(digits.length() - 2));
        } else if (digits.length() == 1) {
            return Integer.parseInt(digits);
        }
        return 1;
    }

    /**
     * Question 1 (Odd last two digits):
     * Find the highest salary not credited on the 1st of any month,
     * and return salary, full name, age, and department.
     *
     * Based on the provided schema and requirement.
     */
    static String question1() {
        // MySQL/MariaDB compatible; for Postgres use EXTRACT(DAY FROM ...).
        return ""
            + "SELECT p.amount AS SALARY,\\n"
            + "       CONCAT(e.first_name, ' ', e.last_name) AS NAME,\\n"
            + "       FLOOR(DATEDIFF(CURDATE(), e.dob) / 365.25) AS AGE,\\n"
            + "       d.department_name AS DEPARTMENT_NAME\\n"
            + "FROM payments p\\n"
            + "JOIN employee e ON e.emp_id = p.emp_id\\n"
            + "JOIN department d ON d.department_id = e.department\\n"
            + "WHERE DAY(p.payment_time) <> 1\\n"
            + "ORDER BY p.amount DESC\\n"
            + "LIMIT 1;";
    }

    /**
     * Question 2 (Even last two digits):
     * For each employee, count employees in the same department who are younger
     * (DOB later than theirs). Return emp_id, names, department, and count.
     */
    static String question2() {
        return ""
            + "SELECT e1.emp_id AS EMP_ID,\\n"
            + "       e1.first_name AS FIRST_NAME,\\n"
            + "       e1.last_name AS LAST_NAME,\\n"
            + "       d.department_name AS DEPARTMENT_NAME,\\n"
            + "       COALESCE(SUM(CASE WHEN e2.dob > e1.dob THEN 1 ELSE 0 END), 0) AS YOUNGER_EMPLOYEES_COUNT\\n"
            + "FROM employee e1\\n"
            + "JOIN department d ON d.department_id = e1.department\\n"
            + "LEFT JOIN employee e2\\n"
            + "       ON e2.department = e1.department\\n"
            + "      AND e2.emp_id <> e1.emp_id\\n"
            + "GROUP BY e1.emp_id, e1.first_name, e1.last_name, d.department_name\\n"
            + "ORDER BY e1.emp_id DESC;";
    }
}
