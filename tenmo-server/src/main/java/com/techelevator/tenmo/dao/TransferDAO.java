package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

public interface TransferDAO {

	Integer createTransfer(int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount);
	
	int getAccountId(int userID); 
	
	List<Transfer> getTransfersByUser(int accountId);
	
	User findUserByAccountID(int accountID);
	
	void updateTransferStatusAndType(int transferStatusId, int transferId);
	
	Transfer getTransferByTransferId(int transferId);

	void deleteSentPendingTransfer(int transferId);
}
