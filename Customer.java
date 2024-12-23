/*
// Authors: Christian Dees & Aitiana Mondragon & Cristina Rivera
// Date: November 12, 2024
// Course: CS 3331 - Advanced Object-Oriented Programming - Fall 2024
// Instructor: Dr. Bhanukiran Gurijala
// Assignment: Programming Assignment 2 (Project Part 2)
// Lab Description: This lab is meant to demonstrate our knowledge in object-oriented concepts such as inheritance, polymorphism, UML diagrams, interfaces, design patterns, and more through coding our own implementation of a bank system of which deposits, withdraws, transfer, pays, and generates various files. This lab also included concepts of logging, testing with JUnit, debugging, file reading, error handling and JavaDoc.
// Honesty Statement: We affirm that we have completed this assignment entirely on our own, without any assistance from outside sources, including peers, experts, online resources, or other means. All code and ideas were that of our own work, and we have followed proper academic integrity.
*/
import java.util.*;

/**
 * Represents a customer with their accounts, dob, address, phone number, and unique id number
 */
public class Customer implements Person{

    /**
     * customer's unique id number
     */
    int idNum;

    /**
     * customer's first name
     */
    String firstName;

    /**
     * customer's last name
     */
    String lastName;

    /**
     * customer's date of birth
     */
    String dob;

    /**
     * customer's address
     */
    String address;

    /**
     * customer's phone number
     */
    String phoneNum;

    /**
     * customer's credit score
     */
    int creditScore;

    /**
     * customer's owned accounts
     */
    ArrayList<Account> accounts = new ArrayList<>();

    /**
     * customer's password
     */
    String password;

    /**
     * Constructs a new Customer with the specified attributes.
     *
     * @param idNum         The unique id number of a customer.
     * @param firstName     The customer's first name.
     * @param lastName      The customer's last name.
     * @param dob           The customer's date of birth.
     * @param address       The customer's address of residence.
     * @param phoneNum      The customer's phone number.
     */
    Customer(int idNum, String firstName, String lastName, String dob, String address, String phoneNum, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNum = idNum;
        this.dob = dob;
        this.address = address;
        this.phoneNum = phoneNum;
        this.creditScore = 0;
        this.password = password;
    }

    /**
     * Get id int.
     *
     * @return the int
     */
    public int getId(){
        return this.idNum;
    }

    /**
     * Get first name string.
     *
     * @return the string
     */
    public String getFirstName(){
        return this.firstName;
    }

    /**
     * Get last name string.
     *
     * @return the string
     */
    public String getLastName(){
        return this.lastName;
    }

    /**
     * Get dob string.
     *
     * @return the string
     */
    public String getDob(){
        String[] parts = this.dob.split("-");
        if (parts.length > 1)parts[1] = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
        return String.join("-", parts);
    }

    /**
     * Get address string.
     *
     * @return the string
     */
    public String getAddress(){
        return this.address;
    }

    /**
     * Get phone number string.
     *
     * @return the string formatted
     */
    public String getPhoneNum(){
        return String.format("(%s) %s-%s", this.phoneNum.substring(0, 3), this.phoneNum.substring(3, 6), this.phoneNum.substring(6));
    }

    /**
     * Get accounts array list.
     *
     * @return the array list
     */
    public ArrayList<Account> getAccounts(){
        return this.accounts;
    }

    /**
     * Get the customer's credit score.
     *
     * @return customer's credit score.
     */
    public int getCreditScore() { return this.creditScore; }

    /**
     * Prints an account's information if it exists.
     *
     * @param viewBalance   Print the balance if true, don't if false.
     */
    public void viewAccounts(boolean viewBalance) {
        boolean creditAccountExists = accounts.stream().anyMatch(account -> "Credit".equals(account.getType()));
        // handle printing the credit account
        if (creditAccountExists) {
            Credit creditAccount = (Credit) accounts.stream().filter(account -> "Credit".equals(account.getType())).findFirst().orElse(null);
            if (creditAccount != null) creditAccount.printHeader(viewBalance);
        } else if (!accounts.isEmpty()) accounts.getFirst().printHeader(viewBalance);
        accounts.forEach(account -> account.printAccount(viewBalance, false));
    }

    /**
     * Prints an account's information if it exists.
     *
     * @param account       Account to be viewed
     * @param viewBalance   Print the balance if true, don't if false.
     */
    public void viewAccount(Account account, boolean viewBalance) {
        // only print if owned by customer
        if (this.accounts.contains(account)) account.printAccount(viewBalance, viewBalance);
        else System.out.println("This account does not belong to this owner!");
    }

    /**
     * Add account.
     *
     * @param account the account
     */
    public void addAccount(Account account){
        this.accounts.add(account);
        if (account.getType().equals("Credit")){
            this.creditScore = generateCreditScore((Credit) account);
        }
    }

    /**
     * Transfers money from one customer's account to another account under the same customer.
     *
     * @param src       The source account that the amount will be withdrawn from.
     * @param dst       The destination account that the amount will be deposited to.
     * @param amount    The amount of money to be transferred.
     *
     * @return          The successfulness of money being transferred.
     * **/
    public boolean transfer(Account src, Account dst, double amount) {
        // check if valid transfer
        if (!this.accounts.contains(src) || !this.accounts.contains(dst) || src.equals(dst)) {
            System.out.println("\nWarning: The customer must own both accounts to transfer funds between them and the accounts cannot be the same.");
            return false;
        }
        // check if valid withdraw
        boolean rc = src.withdraw(amount) && dst.deposit(amount);
        String transferMsg = "Transfer of funds to " + dst.getType() + " [id=" + dst.getAccountNumber() + "]";
        String receiveMsg = "Transfer of funds from " + src.getType() + " [id=" + src.getAccountNumber() + "]";
        if (rc) {
            src.addTransaction(transferMsg, amount);
            dst.addTransaction(receiveMsg, amount);
            System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *  Transfer Successful  *  *  *  *  *  *  *  *  *  *  *");
        }
        else System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *    Transfer Failed    *  *  *  *  *  *  *  *  *  *  *");
        // print appropriate accounts when finished
        src.printAccount(true, true);
        dst.printAccount(true, false);
        return rc;
    }

    /**
     * Sends money from one customer's account to another customer's account.
     *
     * @param src       The source account that the amount will be withdrawn from.
     * @param dst       The destination account that the amount will be deposited to.
     * @param amount    The amount of money to be sent.
     * @param toCustomer The customer receiving funds.
     *
     * @return          The successfulness of money being sent.
     * **/
    public boolean send(Account src, Account dst, double amount, Customer toCustomer) {
        // check if valid send
        if (this.accounts.contains(src) && this.accounts.contains(dst)) {
            System.out.println("\nWarning: Customers cannot send funds to themselves; please use the transfer option instead.");
            return false;
        }
        // check if valid withdraw
        boolean rc = src.withdraw(amount) && dst.deposit(amount);
        String transactionMessage = "Sent funds to " + toCustomer.getFullName();
        String depositMessage = "Received funds from " + this.getFullName();
        if (rc) {
            dst.addTransaction(depositMessage, amount);
            src.addTransaction(transactionMessage, amount);
            System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *    Send Successful    *  *  *  *  *  *  *  *  *  *  *");
        } else System.out.println("*  *  *  *  *  *  *  *  *  *  *  *      Send Failed      *  *  *  *  *  *  *  *  *  *  *");
        // print appropriate accounts when finished
        src.printAccount(true, true);
        dst.printAccount(false, false);
        return rc;
    }

    /**
     * Withdraw an amount from an account, if customer owns it.
     *
     * @param src               source account.
     * @param amount            amount to be withdrawn.
     * @return                  true if success/false if failed.
     */
    public boolean withdraw(Account src, double amount) {
        // check if customer owns this account
        if (!this.accounts.contains(src)) {
            System.out.println("\nWarning: This account does not belong to this customer.");
            return false;
        }
        // check if valid withdraw
        boolean rc = src.withdraw(amount);
        if (rc) {
            String transactionMessage = "Withdrawal of funds";
            src.addTransaction(transactionMessage, amount);
            System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *  Withdraw Successful  *  *  *  *  *  *  *  *  *  *  *");
        }
        else System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *    Withdraw Failed    *  *  *  *  *  *  *  *  *  *  *");
        // print customer's affected account
        src.printAccount(true, true);
        return rc;
    }

    /**
     * Deposit an amount into an account if owned by customer.
     *
     * @param src               source account to have a deposited amount.
     * @param amount            amount to be deposited.
     * @return                  true if success/false if failed.
     */
    public boolean deposit(Account src, double amount) {
        // check if customer owns this account
        if (!this.accounts.contains(src)) {
            System.out.println("\nWarning: Deposits are only permitted into accounts owned by the customer.");
            return false;
        }
        // check if valid deposit
        boolean rc = src.deposit(amount);
        if (rc) {
            String transactionMessage = "Deposit of funds";
            src.addTransaction(transactionMessage, amount);
            System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *  Deposit Successful   *  *  *  *  *  *  *  *  *  *  *");
        } else System.out.println("\n*  *  *  *  *  *  *  *  *  *  *  *     Deposit Failed    *  *  *  *  *  *  *  *  *  *  *");
        // print appropriate accounts when finished
        src.printAccount(true, true);
        return rc;
    }

    /**
     * Generated random credit score for a customer.
     *
     * @param account   based on their credit account limit.
     * @return          the credit score.
     */
    private int generateCreditScore(Credit account) {
        double limit = account.getCreditMax();
        Random random = new Random();
        int lower, upper;
        // get upper and lower limits based on credit limit
        if (limit >= 100 && limit <= 699) {
            lower = 0; upper = 580;
        } else if (limit >= 700 && limit <= 4999) {
            lower = 581; upper = 669;
        } else if (limit >= 5000 && limit <= 7499) {
            lower = 670; upper = 739;
        } else if (limit >= 7500 && limit <= 15999) {
            lower = 740; upper = 799;
        } else if (limit >= 16000 && limit <= 25000) {
            lower = 800; upper = 850;
        } else {
            return 0;
        }
        // return user's randomly generated credit score
        return random.nextInt(upper - lower + 1) + lower;
    }

    /**
     * Check if a given password is the same as the customer's set password.
     *
     * @param attempt   entered password.
     * @return          if passwords match.
     */
    public boolean verifyPassword(String attempt) {return attempt.equals(this.password);}

    /**
     * Get customers formatted full name.
     *
     * @return customers formatted full name.
     */
    @Override
    public String getFullName() {
        String firstName = this.getFirstName();
        String lastName = this.getLastName();
        return (firstName.isEmpty() ? firstName : Character.toUpperCase(firstName.charAt(0)) + firstName.substring(1).toLowerCase()) +
                " " +
                (lastName.isEmpty() ? lastName : Character.toUpperCase(lastName.charAt(0)) + lastName.substring(1).toLowerCase());
    }
}