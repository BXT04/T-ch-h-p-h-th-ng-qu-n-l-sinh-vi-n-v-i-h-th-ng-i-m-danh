package com.student.integration;

import com.student.integration.generator.MessyDataGenerator;

/**
 * Main entry point cho application
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  STUDENT INTEGRATION SYSTEM - ETL Pipeline  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        
        if (args.length > 0 && "generate".equals(args[0])) {
            // Generate messy data
            System.out.println("Mode: GENERATE MESSY DATA");
            MessyDataGenerator.main(new String[]{});
        } else {
            System.out.println("Usage:");
            System.out.println("  mvn exec:java -Dexec.args=\"generate\"  - Generate 20k messy CSV data");
            System.out.println("  mvn exec:java                          - Run ETL pipeline (coming soon)");
        }
    }
}