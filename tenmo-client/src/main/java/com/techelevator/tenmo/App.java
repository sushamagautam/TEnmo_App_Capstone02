package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.UserService;
import com.techelevator.tenmo.services.UserServiceException;
import com.techelevator.view.ConsoleService;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_VIEW_SENT_PENDING_REQUESTS = "View your sent pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_VIEW_SENT_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private static AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private UserService userService;

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL), new UserService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, UserService userService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.userService = userService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			// Option 1: View Balance
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				BigDecimal balance = viewCurrentBalance();
				System.out.println("Balance: $" + balance);
			
			// Option 2: Send TE Bucks	
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				promptUserForSendBucks(); 	
				
			// Option 3: View Past Transfers	
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory(currentUser.getUser().getId());
				printTransferDetails(); 
	
			// Option 4: Request TE Bucks
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
				
			// Option 5: View Pending Requests
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				List<Transfer> pendingTransfers = viewPendingRequests(currentUser.getUser().getId());
				if (pendingTransfers != null) {
					approveOrRejectPendingTransfers(pendingTransfers); 
				}
				
			// Option 6: View sent pending requests
			} else if(MAIN_MENU_OPTION_VIEW_SENT_PENDING_REQUESTS.equals(choice)) {
				List<Transfer> sentPendingTransfers = viewSentPendingRequests();
				if (sentPendingTransfers != null) {
					promptForCancelSentPendingRequest(sentPendingTransfers); 
				}
				
			// Option 7: Login as different user
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
				
			// Option 8: Exit
			} else {
				exitProgram();
			}
		}
	}
	
	// Option 1: Get current user's balance
	private BigDecimal viewCurrentBalance() {
		try {
			return userService.getCurrentBalance(currentUser.getUser());
		} catch (UserServiceException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getLocalizedMessage());
		}
		return null;
		
	}
	
	// Option 2: Send bucks
	private boolean sendBucks(User sendingUser, int recievingUserID, BigDecimal amountToSend) {
			
		try {
			if (userService.getCurrentBalance(sendingUser).compareTo(amountToSend) == -1) { 
				System.out.println("*** Insufficient Funds ***\n");// If the user does not have enough money
				return false; 												// return false, can't send 				
			
			} else if (amountToSend.compareTo(BigDecimal.ZERO) < 0) {
				System.out.println("Please enter a valid amount greater than $0.00\n");
				return false;
			}
			
			else { 
				// Update senders balance
				BigDecimal sendingUserNewBalance = userService.getCurrentBalance(sendingUser).subtract(amountToSend); 
				userService.updateBalance(sendingUser.getId(), sendingUserNewBalance);
				
				// Update recievers balance
				BigDecimal recievingUserNewBalance = userService.getCurrentBalance(userService.findUserById(recievingUserID)).add(amountToSend); 
				userService.updateBalance(recievingUserID, recievingUserNewBalance);
				
			}
		} catch (UserServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false; 
		}
		return true;
	}
	
	// Option 2: Prompt the user to for amount and user to send to
	private void promptUserForSendBucks() {
		
			List<User> availableUsers = listUsersExceptCurrentUser(userService.getAllUsers());
			
			console.printUsers(availableUsers);
			int recieverUserID = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)"); 
			
			if (recieverUserID == 0) {
				mainMenu(); 
			} 
			
			checkForValidUserId(availableUsers, recieverUserID, "Enter ID of user you are sending to (0 to cancel)"); 
			
			boolean successfulSend = false;
			
			while (!successfulSend) { // If send fails due to insufficient funds or negative amount, prompt them again
			BigDecimal bdAmount = console.getUserInputBigDecimal("Enter amount (0 to cancel)"); 

			if (bdAmount.compareTo(BigDecimal.ZERO) == 0) {
				mainMenu(); 
			} 
			
			successfulSend = sendBucks(currentUser.getUser(), recieverUserID, bdAmount); // Attempt to send money,
																									//determine if it was successful
				if (successfulSend) {
					createTransfer(currentUser.getUser().getId(), recieverUserID, bdAmount, 2, 2); // if it is successful, populate a transfer
					System.out.println();
					System.out.println("Successfully sent $" + bdAmount + " to " + userService.findUserById(recieverUserID).getUsername());
				} 
			}

	}
	
	// Option 3: View past transfers
	private void viewTransferHistory(int userId) {
		// TODO Auto-generated method stub
		// Do we need a try/catch??
		
		List<Transfer> transfers = new ArrayList<>(); 
		transfers = userService.getTransfersByUserId(userId);
		
		List<Transfer> pastTransfers = new ArrayList<>();
		
		for (Transfer transfer : transfers) {
			
			if(transfer.getTransferStatusId() != 1) {
				pastTransfers.add(transfer); 
			}
		}

		if (pastTransfers.isEmpty()) {
			System.out.println("No history of transfers yet.");
		} else {
			
			System.out.println("-------------------------------------------------");
			System.out.println("Transfers");
			System.out.printf("%-10s %-20s %-20s\n", "ID", "From/To", "Amount");
			//System.out.println("ID \t\t From/To \t\t Amount");
			System.out.println("-------------------------------------------------");
			
			for (Transfer transfer : pastTransfers) { // Loop through all transfers for that user, 
				String toOrFrom = ""; 				// determine if they were sender or reciever
				String name = "";
				if (transfer.getAccountTo() == userService.getAccountId(userId)) {
					//name = userService.findUserById(userService.getUserId(transfer.getAccountFrom())).getUsername(); delete some of these unused methods 
					name = transfer.getFromUserName();
					toOrFrom = "From: " + name;
				} else {
					name = transfer.getToUserName();	
					toOrFrom = "To: " + name;
				}
				
				String amount = "$" + transfer.getAmount();
				System.out.printf("%-10s %-20s %-20s\n", transfer.getTransferID(), toOrFrom, amount);
				//System.out.println(transfer.getTransferID() + "\t\t " + toOrFrom + "\t\t $" + transfer.getAmount());
			}
		}
	}
	
	// Option 3: View transfer details after entering a transfer ID
	private void printTransferDetails() {
		
		int transferId = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
		boolean validTransferId = false;
		
		if (transferId == 0) {
			mainMenu(); 
			
		} else {
			List<Transfer> allTransfers = userService.getTransfersByUserId(currentUser.getUser().getId()); 
		
			for (Transfer transfer : allTransfers) { // Loop through all transfers to identify if transferId entered by user exists
				
				if (transfer.getTransferID() == transferId) { // If it exists, idenfiy transfer type and status
					validTransferId = true;
					String type = "";
					String status = ""; 
					
					if (transfer.getTransferTypeId() == 1) {
						type = "Request";
					} else if (transfer.getTransferTypeId() == 2) {
						type = "Send";
					}
					
					if (transfer.getTransferStatusId() == 1) {
						status = "Pending";
					} else if (transfer.getTransferStatusId() == 2) {
						status = "Approved";
					} else if (transfer.getTransferStatusId() == 3) {
						status = "Rejected";
					}
					printLinesTransferDetails(transfer, type, status); // Print the details
				}
			}
			
			if (validTransferId == false) { // If transferId entered by user does not exist, print this and rerun method
				System.out.println("Sorry transfer Id " + transferId + " does not exist.");
				printTransferDetails(); 
			}
		
		}
	}
	
	// Option 3: Print transfer details
	private void printLinesTransferDetails(Transfer transfer, String type, String status) {
		
		System.out.println("-------------------------------------------------");
		System.out.println("Transfer Details");
		System.out.println("-------------------------------------------------");
		System.out.println("Id: " + transfer.getTransferID());
		System.out.println("From: " + transfer.getFromUserName());
		System.out.println("To: " + transfer.getToUserName());
		System.out.println("Type: " + type);
		System.out.println("Status: " + status);
		System.out.println("Amount: $" + transfer.getAmount());
		
	}
	
	// Option 4: Request money
	private void requestBucks() {
	
		List<User> availableUsers = listUsersExceptCurrentUser(userService.getAllUsers());
		
		console.printUsers(availableUsers);
		int requestedUserID = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)"); 
		
		if (requestedUserID == 0) {
			mainMenu(); 
		} 
		
		checkForValidUserId(availableUsers, requestedUserID, "Enter ID of user you are requesting from (0 to cancel)");
		
		boolean validAmount = false;
		BigDecimal bdAmount = null; 
		while (!validAmount) {
			bdAmount = console.getUserInputBigDecimal("Enter amount (0 to cancel)"); 
		
			if (bdAmount.compareTo(BigDecimal.ZERO) == 0) {
				mainMenu(); 
			} else if (bdAmount.compareTo(BigDecimal.ZERO) == -1) {
				System.out.println("You cannot request a negative amount.");
			} else {
				validAmount = true;
			}
		}
		
		createTransfer(requestedUserID, currentUser.getUser().getId(), bdAmount, 1, 1); 
		System.out.println("\nRequest sent to " + userService.findUserById(requestedUserID).getUsername() + " for $" + bdAmount);
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	
	// Option 5: View pending requests
	private List<Transfer> viewPendingRequests(int userId) {
		
		List<Transfer> transfers = new ArrayList<>(); 
		transfers = userService.getTransfersByUserId(userId);
		
		List<Transfer> pendingTransfers = new ArrayList<>();
		
		for (Transfer transfer : transfers) {
			
			if(transfer.getTransferStatusId() == 1 && transfer.getAccountFrom() == userService.getAccountId(userId)) {
				pendingTransfers.add(transfer); 
			}
		}

		if (pendingTransfers.isEmpty()) {
			System.out.println("No pending transfers at this time.");
		} else {
			
			System.out.println("--------------------------------------------------");
			System.out.println("Pending Transfers");
			System.out.printf("%-20s %-20s %-20s\n", "ID", "To", "Amount");
			System.out.println("--------------------------------------------------");
			
			for (Transfer transfer : pendingTransfers) { 
				System.out.printf("%-20s %-20s %-20s\n", transfer.getTransferID(), transfer.getToUserName(), "$" + transfer.getAmount());
			}
		}
		return pendingTransfers;
	}
	
	// Option 5: Approve or Reject a pending request someone has sent you
	private void approveOrRejectPendingTransfers(List<Transfer> pendingTransfers) {
		
		boolean sendSuccessful = false;
		while (!sendSuccessful) {
		
			Transfer newTransfer= null; 
			System.out.println();
			int pendingTransferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)"); 
			
			if (pendingTransferId == 0) {
				mainMenu(); 
			} 
			
			boolean validTransferId = false;
			for (Transfer transfer : pendingTransfers) { // Check if user entered a valid id number
				if (transfer.getTransferID() == pendingTransferId) {
					validTransferId = true; 
					newTransfer = transfer;
				}
					
			}
			
			while (!validTransferId) { // If they did not enter a valid id number, continue prompting them until they do
				System.out.println("*** Invalid Transfer Id ***");
				pendingTransferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)"); 
				
				if (pendingTransferId == 0) {
					mainMenu(); 
				} 
				
				for (Transfer transfer : pendingTransfers) { // Check if user entered a valid id number
					if (transfer.getTransferID() == pendingTransferId) {
						validTransferId = true; 
						newTransfer = transfer;
					}
				}
			} 
			int transferOption = 0; 
			
			System.out.println();
			System.out.println("1: Approve");
			System.out.println("2: Reject");
			System.out.println("0: Don't approve or reject");
			System.out.println("---------");
			
			transferOption = console.getUserInputInteger("Please choose an option"); 
			
			if (transferOption == 0) {
				mainMenu(); 
			} else if (transferOption == 1) {
				
				int recievingUserId = userService.getUserId(newTransfer.getAccountTo()); 
				
				sendSuccessful = sendBucks(currentUser.getUser(), recievingUserId, newTransfer.getAmount()); 
				
				if (sendSuccessful) {
				userService.updateTransfer(newTransfer.getTransferID(), 2);
				System.out.println();
				System.out.println("Successfully sent $" + newTransfer.getAmount() + " to " + newTransfer.getToUserName());
				} 
			} else if (transferOption == 2) {
				userService.updateTransfer(newTransfer.getTransferID(), 3);
				System.out.println();
				System.out.println("Request transfer: " + newTransfer.getTransferID() + " was rejected.");
			} else {
				System.out.println(" *** Invalid request ***");
				System.out.println();
				approveOrRejectPendingTransfers(pendingTransfers); 
			}
		
		} 
	}
		
	// Option 6: View sent pending requests
	// We added this menu feature because the user was unable to see reqeusts 
	// they have made to others that have not been satisified
	private List<Transfer> viewSentPendingRequests() {
		
		List<Transfer> transfers = new ArrayList<>(); 
		transfers = userService.getTransfersByUserId(currentUser.getUser().getId());
		
		List<Transfer> pendingTransfers = new ArrayList<>();
		
		for (Transfer transfer : transfers) {
			
			if(transfer.getTransferStatusId() == 1 && transfer.getAccountTo() == userService.getAccountId(currentUser.getUser().getId())) {
				pendingTransfers.add(transfer); 
			}
		}
		
		if (pendingTransfers.isEmpty()) {
			System.out.println("No unfulfilled transfer requests at this time.");
		} else {
			
			System.out.println("--------------------------------------------------");
			System.out.println("Pending Transfers");
			System.out.printf("%-20s %-20s %-20s\n", "ID", "From", "Amount");
			System.out.println("--------------------------------------------------");
			
			for (Transfer transfer : pendingTransfers) { 
				System.out.printf("%-20s %-20s %-20s\n", transfer.getTransferID(), transfer.getFromUserName(), "$" + transfer.getAmount());
			}
		}
		return pendingTransfers;
		
	}
	
	// Option 6: Added feature for user to be able to cancel a request they have made
	private void promptForCancelSentPendingRequest(List<Transfer> sentPendingTransfers) {
		
		Transfer deleteTransfer = new Transfer();
		int pendingTransferId = console.getUserInputInteger("\nEnter a transfer ID to cancel the request (0 to cancel)"); 
		
		if (pendingTransferId == 0) {
			mainMenu(); 
		} 
		
		boolean validTransferId = false;
		for (Transfer transfer : sentPendingTransfers) { // Check if user entered a valid id number
			if (transfer.getTransferID() == pendingTransferId) {
				validTransferId = true; 
				deleteTransfer = transfer;
			}
				
		}
		
		while (!validTransferId) { // If they did not enter a valid id number, continue prompting them until they do
			System.out.println("*** Invalid Transfer Id ***");
			pendingTransferId = console.getUserInputInteger("Enter a transfer ID to cancel the request (0 to cancel)"); 
			
			if (pendingTransferId == 0) {
				mainMenu(); 
			} 
			
			for (Transfer transfer : sentPendingTransfers) { // Check if user entered a valid id number
				if (transfer.getTransferID() == pendingTransferId) {
					validTransferId = true; 
					deleteTransfer = transfer;
				}
			}
		} 
		
		Integer confirmation = console.getUserInputInteger("Press 1 to confirm the cancellation of your request to "
		+ deleteTransfer.getFromUserName() + " for $" + deleteTransfer.getAmount() + " (0 to cancel)");
		
		if (confirmation == 0) {
			mainMenu(); 
		} else if (confirmation == 1) {
			userService.cancelSentRequest(deleteTransfer.getTransferID());
			System.out.println("\nRequest number: " + deleteTransfer.getTransferID() + " has been cancelled");
		} else {
			System.out.println("*** Invalid Response ***");
			promptForCancelSentPendingRequest(sentPendingTransfers);
		}
	}

	// This method is used in several menu options to create a transfer whenever a user sends money or requests money
	private Transfer createTransfer(int sendingUserID, int recievingUserID, BigDecimal amountSent, int transferStatusId, int transferTypeId) {
		
		Transfer newTransfer = new Transfer();
		
		newTransfer.setAccountFrom(userService.getAccountId(sendingUserID));
		newTransfer.setAccountTo(userService.getAccountId(recievingUserID));
		newTransfer.setAmount(amountSent); 
		
		newTransfer.setTransferStatusId(transferStatusId);
		newTransfer.setTransferTypeId(transferTypeId);
		
		newTransfer.setFromUserName(userService.findUserById(sendingUserID).getUsername());
		newTransfer.setToUserName(userService.findUserById(recievingUserID).getUsername());
		
		userService.createTransfer(sendingUserID, newTransfer);
		
		return newTransfer;
		
	}
	
	// Helper method to list all users except the current user so they cannot
	// Send or request money to themselves
	private List<User> listUsersExceptCurrentUser(List<User> allUsers) {
		
		List<User> availableUsers = new ArrayList<>();
		
	
		for (User user : allUsers) { // Do not add the current user into the list of available users to send money to
			if(!user.getId().equals(currentUser.getUser().getId())) {
				availableUsers.add(user);
			}
		}
		return availableUsers;
		
	}
	
	// Check that the user ID they are choosing is valid and exists
	private void checkForValidUserId(List<User> availableUsers, int userId, String message) {
		
		boolean validUserId = false; 
		
		for (User user : availableUsers) { // Check if user entered a valid id number
			if (user.getId().equals(userId)) {
				validUserId = true; 
			}
		}
		
		while (!validUserId) { // If they did not enter a valid id number, continue prompting them until they do
			System.out.println("*** Please enter a valid user Id or press 0 to return to main menu");
			userId = console.getUserInputInteger(message); 
			
			if (userId == 0) {
				mainMenu(); 
			} 
			
			for (User user : availableUsers) {
				if (user.getId().equals(userId)) {
					validUserId = true; 	
				}
			}
		} 
	}
	
	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
				
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
				userService.AUTH_TOKEN = currentUser.getToken(); // Populate auth token
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
