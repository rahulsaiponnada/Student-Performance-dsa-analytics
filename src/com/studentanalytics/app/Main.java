
package com.studentanalytics.app;

import java.util.Scanner;

import com.studentanalytics.service.StudentService;
import com.studentanalytics.service.PerformanceService;
import com.studentanalytics.service.DsaService;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        StudentService student = new StudentService();
        PerformanceService performance = new PerformanceService();
        DsaService dsa = new DsaService();

        while (true) {

            System.out.println("\n======================================");
            System.out.println("       STUDENT ANALYTICS SYSTEM");
            System.out.println("======================================");

            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Exit");
            System.out.println("4. Add Marks");
            System.out.println("5. View Performance");
            System.out.println("6. Add DSA Problem");
            System.out.println("7. View DSA Problems");
            System.out.println("8. View DSA Progress");
            System.out.println("9. Update DSA Problem Status");
            System.out.println("10. Topic-wise DSA Analytics");
            System.out.println("11. Individual Student Report");
            System.out.println("12. Highest Performing Student");

            System.out.print("\nEnter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    student.addStudent();
                    break;

                case 2:
                    student.viewStudents();
                    break;

                case 3:
                    System.out.println("Thank you for using Student Analytics System!");
                    sc.close();
                    return;

                case 4:
                    performance.addMarks();
                    break;

                case 5:
                    performance.viewPerformance();
                    break;

                case 6:
                    dsa.addProblem();
                    break;

                case 7:
                    dsa.viewProblems();
                    break;

                case 8:
                    dsa.showProgress();
                    break;

                case 9:
                    dsa.updateProblemStatus();
                    break;

                case 10:
                    dsa.topicWiseProgress();
                    break;

                case 11:
                    performance.individualReport();
                    break;

                case 12:
                    performance.showTopper();
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}
