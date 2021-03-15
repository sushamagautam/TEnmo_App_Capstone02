package com.techelevator.tenmo.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;


@PreAuthorize("isAuthenticated()")
@RestController
public class UserController {
	
	private UserDAO userDao;
	private TransferDAO transferDao;
	
	public UserController(UserDAO userDao, TransferDAO transferDao) {
		this.userDao = userDao;
		this.transferDao = transferDao;
	}
	
		
	@RequestMapping(path = "users/{userID}/balance", method = RequestMethod.GET)
	public BigDecimal getCurrentBalance(@PathVariable int userID) {
//		if(getUserByID(userID).getUsername().equals(principal.getName())) { Tried to secure so only principal could access their own balance
		return userDao.getCurrentBalance(userID);							// did not work
//		} else throw new InvalidUserException("Invalid Request.");
	}
	
	@RequestMapping(path = "users", method = RequestMethod.GET)
	public List<User> getAllUsers() {
		return userDao.findAll();
	}
	
	// May want to get all users without password_hash
//	@RequestMapping(path = "users/public", method = RequestMethod.GET)
//	public List<User> getAllUsersPublic() {
//		return userDao.findAllPublic();
//	}
	
	@RequestMapping(path = "users/{userID}/balance", method = RequestMethod.PUT) 
	public void updateBalance(@PathVariable int userID,  @RequestBody BigDecimal newBalance) {
		userDao.updateBalance(userID, newBalance); 	
	}
	
	@RequestMapping(path = "users/{userID}", method = RequestMethod.GET)
	public User getUserByID(@PathVariable int userID) {
		return userDao.findUserByID(userID);
	}
	
	@RequestMapping(path = "users/{userId}/transfers", method = RequestMethod.POST)
	public void createTransfer(@PathVariable int userId, @RequestBody Transfer transfer) {
		transferDao.createTransfer(transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(), 
				transfer.getAccountTo(), transfer.getAmount());
	}
	
	@RequestMapping(path = "users/{userId}/accountId", method = RequestMethod.GET)
	public int getAccountId (@PathVariable int userId) {
		return transferDao.getAccountId(userId); 
	}
	
	@RequestMapping(path = "users/{accountId}/userId", method = RequestMethod.GET)
	public int getUserId (@PathVariable int accountId) {
		return userDao.findUserIdByAccountId(accountId); 
	}
	
	@RequestMapping(path = "users/{userId}/transfers", method = RequestMethod.GET)
	public List<Transfer> getTransfersByUserId(@PathVariable int userId) {
		return transferDao.getTransfersByUser(transferDao.getAccountId(userId)); 
	}
	
	@RequestMapping(path = "transfers/{transferId}", method = RequestMethod.PUT)
	public void updateTransfer(@PathVariable int transferId, @RequestBody int transferStatusId) {
		transferDao.updateTransferStatusAndType(transferStatusId, transferId);
	}
	
	@RequestMapping(path = "transfers/{transferId}", method = RequestMethod.GET)
	public Transfer getTransferByTransferId(@PathVariable int transferId) {
		return transferDao.getTransferByTransferId(transferId);
	}
	
	@RequestMapping(path = "transfers/{transferId}", method = RequestMethod.DELETE)
	public void deleteTransfer(@PathVariable int transferId) {
		transferDao.deleteSentPendingTransfer(transferId);
	}
}
