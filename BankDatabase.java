/*
// Authors: Christian Dees & Aitiana Mondragon & Crystina Rivera
// Date: October 23, 2024
// Course: CS 3331 - Advanced Object-Oriented Programming - Fall 2024
// Instructor: Dr. Bhanukiran Gurijala
// Assignment: Programming Assignment 1 (Project Part 1)
// Lab Description: This lab is meant to demonstrate our knowledge in object-oriented concepts such as inheritance, polymorphism, UML diagrams, and more through coding our own implementation of a bank system of which deposits, withdraws, transfer, and pays. This lab also included concepts of logging, testing, debugging, file reading, and JavaDoc.
// Honesty Statement: We affirm that we have completed this assignment entirely on our own, without any assistance from outside sources, including peers, experts, online resources, or other means. All code and ideas were that of our own work, and we have followed proper academic integrity.
 */
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.TreeSet;

public class BankDatabase {

    private static BankDatabase instance;
    public static  Dictionary<String, Customer> customers = new Hashtable<>();
    public static  Dictionary<Integer, Checking> checkingAccounts = new Hashtable<>();
    public static  Dictionary<Integer, Savings> savingAccounts = new Hashtable<>();
    public static final Dictionary<Integer, Credit> creditAccounts = new Hashtable<>();
    public static  TreeSet<Integer> customerIDs = new TreeSet<>();
    public static final TreeSet<Integer> checkingAccNums = new TreeSet<>();
    public static final TreeSet<Integer> savingsAccNums = new TreeSet<>();
    public static final TreeSet<Integer> creditAccNums = new TreeSet<>();

    private BankDatabase(){
        /* prevent instantiation */
    }

    /**
     * Returns a dictionary containing all customers in the bank database.
     *
     * @return A Dictionary where the key is a String (customer ID or name) and
     *         the value is a Customer object.
     */
    public Dictionary<String, Customer> getCustomers() {
        return customers;
    }

    /**
     * Returns a TreeSet of all checking account numbers in the bank database.
     *
     * @return A TreeSet containing unique Integer checking account numbers.
     */
    public TreeSet<Integer> getCheckingAccNums() {
        return checkingAccNums;
    }

    /**
     * Returns a TreeSet of all savings account numbers in the bank database.
     *
     * @return A TreeSet containing unique Integer savings account numbers.
     */
    public TreeSet<Integer> getSavingsAccNums() {
        return savingsAccNums;
    }

    /**
     * Returns a TreeSet of all credit account numbers in the bank database.
     *
     * @return A TreeSet containing unique Integer credit account numbers.
     */
    public TreeSet<Integer> getCreditAccNums() {
        return creditAccNums;
    }

    /**
     * Returns a dictionary containing all checking accounts in the bank database.
     *
     * @return A Dictionary where the key is an Integer (account number) and
     *         the value is a Checking object representing the checking account.
     */
    public Dictionary<Integer, Checking> getCheckingAccounts() {
        return checkingAccounts;
    }

    /**
     * Returns a dictionary containing all savings accounts in the bank database.
     *
     * @return A Dictionary where the key is an Integer (account number) and
     *         the value is a Savings object representing the savings account.
     */
    public Dictionary<Integer, Savings> getSavingsAccounts() {
        return savingAccounts;
    }

    /**
     * Returns a dictionary containing all credit accounts in the bank database.
     *
     * @return A Dictionary where the key is an Integer (account number) and
     *         the value is a Credit object representing the credit account.
     */
    public Dictionary<Integer, Credit> getCreditAccounts() {
        return creditAccounts;
    }

    /**
     * Returns a TreeSet of all customer IDs in the bank database.
     *
     * @return A TreeSet containing unique Integer customer IDs.
     */
    public TreeSet<Integer> getCustomerIDs() {
        return customerIDs;
    }

    /**
     * Returns the singleton instance of the BankDatabase class. This method ensures
     * that only one instance of the BankDatabase exists in the system.
     *
     * @return The singleton instance of BankDatabase.
     */
    public static synchronized BankDatabase getInstance() {
        if (instance == null) {
            instance = new BankDatabase();
        }
        return instance;
    }

    /**
     * Add a customer given values mapped to headers.
     *
     * @param record    values mapped to headers.
     * @return          true if success/false if failed.
     */
    boolean addCustomer(Dictionary<String, String> record) {
        String[] headers = {"Identification Number", "First Name", "Last Name", "Date of Birth", "Address", "Phone Number", "Checking Account Number", "Checking Starting Balance", "Savings Account Number", "Savings Starting Balance", "Credit Account Number", "Credit Max", "Credit Starting Balance"};
        int idNum = Integer.parseInt(record.get(headers[0]));
        String firstName = record.get(headers[1]).toLowerCase();
        String lastName = record.get(headers[2]).toLowerCase();
        String dob = record.get(headers[3]);
        String address = record.get(headers[4]).replace("\"", "");
        String phoneNum = record.get(headers[5]).replaceAll("[()\\s-]", "");
        int checkingAccNum = Integer.parseInt(record.get(headers[6]));
        double checkingStartBalance = Double.parseDouble(record.get(headers[7]));
        int savingsAccNum = Integer.parseInt(record.get(headers[8]));
        double savingsStartBalance = Double.parseDouble(record.get(headers[9]));
        int creditAccNum = Integer.parseInt(record.get(headers[10]));
        int creditMax = Integer.parseInt(record.get(headers[11]));
        double creditStartBalance = Double.parseDouble(record.get(headers[12]));
        /*
            FUTURE: REMOVE THIS BECAUSE DUMB
         */
        FileHandler fh = new FileHandler();
        if (customers.get(firstName+lastName)!=null){
            System.out.println("-----A Customer with that name already exists!-----");
            fh.appendLog("EPMB_Error_Log", "Failed to add user with id: " + idNum + ". Reason: User with that name already exists.");
            return false;
        }
        // add customer and its accounts
        boolean rc = customerIDs.add(idNum);
        if(!rc){
            fh.appendLog("EPMB_Error_Log", "Failed to add user with id: " + idNum + ". Reason: User with that id already exists.");
        } else {
            Customer newCustomer = new Customer(idNum, firstName, lastName, dob, address, phoneNum);
            Checking checkingAcc = new Checking(checkingAccNum, checkingStartBalance);
            Savings savingsAcc = new Savings(savingsAccNum, savingsStartBalance);
            Credit creditAcc = new Credit(creditAccNum, creditStartBalance, creditMax);
            if (fh.addAccountToMaps(checkingAcc)) newCustomer.addAccount(checkingAcc);
            if (fh.addAccountToMaps(savingsAcc)) newCustomer.addAccount(savingsAcc);
            if (fh.addAccountToMaps(creditAcc)) newCustomer.addAccount(creditAcc);
            customers.put(firstName+lastName, newCustomer);
        }
        return rc;
    }
}