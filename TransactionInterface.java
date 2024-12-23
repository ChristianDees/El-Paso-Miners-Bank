/*
// Authors: Christian Dees & Aitiana Mondragon & Cristina Rivera
// Date: November 12, 2024
// Course: CS 3331 - Advanced Object-Oriented Programming - Fall 2024
// Instructor: Dr. Bhanukiran Gurijala
// Assignment: Programming Assignment 2 (Project Part 2)
// Lab Description: This lab is meant to demonstrate our knowledge in object-oriented concepts such as inheritance, polymorphism, UML diagrams, interfaces, design patterns, and more through coding our own implementation of a bank system of which deposits, withdraws, transfer, pays, and generates various files. This lab also included concepts of logging, testing with JUnit, debugging, file reading, error handling and JavaDoc.
// Honesty Statement: We affirm that we have completed this assignment entirely on our own, without any assistance from outside sources, including peers, experts, online resources, or other means. All code and ideas were that of our own work, and we have followed proper academic integrity.
*/
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * The type Transaction input handler.
 */
public class TransactionInterface extends UserInterface {

    /**
     * Ask user for how much money to deposit.
     *
     * @param scan      scanner object for input
     * @param customer  customer in charge of account to deposit in
     * @param account   account to deposit money into
     * @param fh        file handler to handle logging
     */
    private void depositAmount(Scanner scan, Customer customer, Account account, FileHandler fh) {
        // three attempts allowed
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Enter deposit amount: \n$");
            String depositAmountStr = scan.nextLine();
            try {
                double depositAmount = this.validateMoney(depositAmountStr);
                boolean rc = customer.deposit(account, depositAmount);
                if (rc) fh.appendLog("EPMB_Transactions", customer.getFullName() + " [ID:" + customer.getId() + "] made a deposit of $" + String.format("%.2f", depositAmount) +
                        " to " + account.getType() + " account [Account Number: " + account.getAccountNumber() + "]. Current balance: $" + String.format("%.2f", account.getBalance()));
                else fh.appendLog("EPMB_Error_Log", "Reason for failure: Customer does not own this account or insufficient funds.");
                return;

            } catch (InvalidCurrencyFormat e) {
                // error logging
                System.out.println("Error" + e.getMessage());
            }
        }
        // error logging
        fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] reached maximum attempts.");
        System.out.println("Maximum attempts reached. Returning to main menu.");
    }

    /**
     * Ask user for how much money to withdraw.
     *
     * @param scan      scanner object for input
     * @param customer  customer in charge of account to withdraw in
     * @param account   account to withdraw money into
     * @param fh        file handler to handle logging
     */
    private void withdrawAmount(Scanner scan, Customer customer, Account account, FileHandler fh) {
        // three attempts allowed
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Enter withdrawal amount: \n$");
            String withdrawAmountStr = scan.nextLine();
            try {
                double withdrawAmount = this.validateMoney(withdrawAmountStr);
                // withdraw amount from an account
                boolean success = customer.withdraw(account, withdrawAmount);
                String logMessage = customer.getFullName() + " [ID:" + customer.getId() + "] attempted a withdrawal of $" + String.format("%.2f", withdrawAmount) +
                        " from " + account.getType() + " account [Account Number:" + account.getAccountNumber() + "]. Current balance: $" + String.format("%.2f", account.getBalance());
                if (success) {
                    fh.appendLog("EPMB_Transactions", logMessage);
                } else {
                    // error logging
                    fh.appendLog("EPMB_Error_Log", logMessage + " Reason for failure: Insufficient funds.");
                }
                return;
            } catch (InvalidCurrencyFormat e) {
                // error logging
                fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] attempted a withdrawal from " +
                        account.getType() + " account [Account Number:" + account.getAccountNumber() + "]. Reason for failure: Inappropriate formatting. Current balance: $" + String.format("%.2f", account.getBalance()));
                System.out.println("Error: " + e.getMessage());
            }
        }
        fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] reached maximum attempts.");
        System.out.println("Maximum attempts reached. Returning to main menu.");
    }

    /**
     * Handle a transaction with one account.
     *
     * @param scan     The scanner object to continue taking input.
     * @param customer the customer
     * @param fh       the fh
     */
    public void oneAccountTransaction(Scanner scan, Customer customer, FileHandler fh) {
        customer.viewAccounts(false);
        Account account = getAccountForTransaction(scan, customer, fh);
        if (account == null) return;
        // three attempts allowed
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Choose an action:\nA. Inquire Account Details\nB. Deposit\nC. Withdraw\nD. Transfer\n> ");
            String input = scan.nextLine().trim().toLowerCase();
            if (logout(input)) return;
            switch (input) {
                case "a":
                    // inquire detailed description of account
                    fh.appendLog("EPMB_Transactions", customer.getFullName() + " [ID:" + customer.getId() + "] viewed the details of " + account.getType() + " account [Account Number:" + account.getAccountNumber() +
                            "] Account's Current Balance: $" + String.format("%.2f", account.getBalance()));
                    account.printAccount(true, true);
                    return;
                case "b":
                    // perform depositing of funds
                    depositAmount(scan, customer, account, fh);
                    return;
                case "c":
                    // perform withdrawing of funds
                    withdrawAmount(scan, customer, account, fh);
                    return;
                case "d":
                    // handle transaction between two entities
                    twoAccountTransaction(scan, customer, account, true, fh);
                    return;
                default:
                    // error logging
                    fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] Reason for failure: Failed to choose appropriate transaction option.");
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handle a transaction between two accounts.
     *
     * @param scan        The scanner object to continue taking input.
     * @param customerOne The first customer involved in the transaction.
     * @param accountOne  The first account associated in the transaction.
     * @param transfer    the transfer
     * @param fh          the fh
     */
    public void twoAccountTransaction(Scanner scan, Customer customerOne, Account accountOne, boolean transfer, FileHandler fh) {
        boolean send = !transfer;
        MainInterface ih = new MainInterface();
        if (send) customerOne.viewAccounts(false);
        // get first account if not provided
        if (accountOne == null) {
            accountOne = getAccountForTransaction(scan, customerOne, fh);
            if (accountOne == null) return;
        }
        Account accountTwo;
        Customer customerTwo = null;
        if (send) {
            // get second account
            System.out.println("Please enter the following for the receiving account:\n" + "-".repeat(51));
            customerTwo = ih.getUserName(scan, false, true, false, fh);
            if (customerTwo == null) return;
            accountTwo = getAccountForTransaction(scan, customerTwo, fh);
        } else {
            accountTwo = getAccountForTransaction(scan, customerOne, fh);
        }
        if (accountTwo == null) return;
        // perform transfer/send
        if (transfer) this.transferAmount(scan, customerOne, accountOne, accountTwo, fh);
        else this.sendAmount(scan, customerOne, accountOne, customerTwo, accountTwo, fh);
        }

    /**
     * Send money from one account to another account of the same owner.
     *
     * @param scan          Scanner object for input.
     * @param customer      Customer object for owner of the account.
     * @param accountOne    Source account for the transaction.
     * @param accountTwo    Destination account for the transaction.
     * @param fh            File handler for logging.
     */
    private void transferAmount(Scanner scan, Customer customer, Account accountOne, Account accountTwo, FileHandler fh) {
        // three attempts allowed
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Enter transfer amount: \n$");
            String transferAmountStr = scan.nextLine();
            try {
                double transferAmount = this.validateMoney(transferAmountStr);
                boolean rc = customer.transfer(accountOne, accountTwo, transferAmount);
                String logMessage = customer.getFullName() + " [ID:" + customer.getId() + "] attempted a transfer of $" + String.format("%.2f", transferAmount) +
                        " from " + accountOne.getType() + " account [Account Number:" + accountOne.getAccountNumber() +
                        "] to " + accountTwo.getType() + " account [Account Number:" + accountTwo.getAccountNumber() + "]. " +
                        "Account One Current Balance: $" + String.format("%.2f", accountOne.getBalance()) +
                        " Account Two Current Balance: $" + String.format("%.2f", accountTwo.getBalance());
                if (rc) fh.appendLog("EPMB_Transactions", logMessage);
                else // error logging
                    fh.appendLog("EPMB_Error_Log", logMessage + " Reason for failure: Insufficient funds or incorrect account.");
                return;
            } catch (InvalidCurrencyFormat e){
                // error logging
                fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] attempted a transfer of $" + transferAmountStr + " from " +
                        accountOne.getType() + " to " + accountTwo.getType() + " Reason for failure: Inappropriate format.");
                System.out.println("Error: " + e.getMessage());
            }
        }
        // error logging
        fh.appendLog("EPMB_Error_Log", customer.getFullName() + " [ID:" + customer.getId() + "] reached maximum attempts.");
        System.out.println("Maximum attempts reached. Returning to main menu.");
    }

    /**
     * Send money from one user's account to another user's account
     *
     * @param scan          Scanner object for input.
     * @param customerOne   Customer object for owner of the source account.
     * @param accountOne    Source account for the transaction.
     * @param accountTwo    Destination account for the transaction.
     * @param fh            File handler for logging.
     */
    private void sendAmount(Scanner scan, Customer customerOne, Account accountOne, Customer customerTwo, Account accountTwo, FileHandler fh) {
        // three attempts allowed
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Enter amount to be sent: \n$");
            String sendAmountStr = scan.nextLine();

            try{
                double sendAmount = this.validateMoney(sendAmountStr);
                boolean rc = customerOne.send(accountOne, accountTwo, sendAmount, customerTwo);
                String logMessage = customerOne.getFullName() + " [ID:" + customerOne.getId() + "] attempted to send $" + String.format("%.2f", sendAmount) +
                        " from " + accountOne.getType() + " account [Account Number:" + accountOne.getAccountNumber() +
                        "] to " + customerOne.getFullName() + " [ID:" + customerOne.getId() + "] " + accountTwo.getType() + " account [Account Number:" + accountTwo.getAccountNumber() + "]. " +
                        "Account One Current Balance: $" + String.format("%.2f", accountOne.getBalance());
                if (rc) {
                    fh.appendLog("EPMB_Transactions", logMessage);
                } else {
                    // error logging
                    fh.appendLog("EPMB_Error_Log", logMessage + " Reason for failure: Insufficient funds or incorrect accounts.");
                }
                return;
            } catch (InvalidCurrencyFormat e) {
                // error logging
                fh.appendLog("EPMB_Error_Log", customerOne.getFullName() + " [ID:" + customerOne.getId() + "] attempted to send funds" +
                        " from " + accountOne.getType() + " account [Account Number:" + accountOne.getAccountNumber() + "] to " + customerOne.getFullName() + " [ID:" + customerOne.getId() + "] " + accountTwo.getType() + " account [Account Number:" + accountTwo.getAccountNumber() + "]. Reason for Failure: Inappropriate format.");
                System.out.println("Error: " + e.getMessage());
            }
        }
        // error logging
        fh.appendLog("EPMB_Error_Log", customerOne.getFullName() + " [ID:" + customerOne.getId() + "] reached maximum attempts.");
        System.out.println("Maximum attempts reached. Returning to main menu.");
    }

    /**
     * Return account from the respective array lists.
     *
     * @param accType The account type the user provided.
     * @param accNum  The account number the user provided.
     * @return The account if it exists.
     */
    public Account getAccount(String accType, int accNum) {
        return "checking".equalsIgnoreCase(accType) ? BankDatabase.getInstance().getCheckingAccounts().get(accNum) :
                "savings".equalsIgnoreCase(accType) ? BankDatabase.getInstance().getSavingsAccounts().get(accNum) :
                        "credit".equalsIgnoreCase(accType) ? BankDatabase.getInstance().getCreditAccounts().get(accNum) : null;
    }

    /**
     * Check if the format provided fits correct money format.
     *
     * @param input The input of money.
     * @return      If format is valid or not.
     */
    private double validateMoney(String input) throws InvalidCurrencyFormat {
        boolean correctFormat = Pattern.matches("^(\\d{1,3}(,\\d{3})*(\\.\\d{1,2})?|\\d+(\\.\\d{1,2})?)$", input);
        if (correctFormat) return Double.parseDouble(input.replace(",", ""));
        throw new InvalidCurrencyFormat("Currency format is invalid.");
    }
}
