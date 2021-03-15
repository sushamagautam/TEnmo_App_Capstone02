package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@Component
public class JdbcTransferDAO implements TransferDAO {

	
	 private JdbcTemplate jdbcTemplate;

	    public JdbcTransferDAO(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }
	    
	    @Override
	    public Integer createTransfer(int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount) {
	    	
	    	String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) "
	    			+ "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
	    	Integer newTransferId = -1;
	    	try {
	    		newTransferId = jdbcTemplate.queryForObject(sql, Integer.class, transferTypeId, transferStatusId, accountFrom, accountTo, amount);
	    	}
	    	catch(DataAccessException e) {
	    		System.out.println("Error Creating Transfer."); // Potentially change later
	    	}
	    	return newTransferId;
	    }
	    
	    @Override
	    public User findUserByAccountID(int accountID) {
			
			User user = null; 
			String sql = "SELECT u.username, u.user_id FROM users u JOIN accounts a ON u.user_id = a.user_id WHERE a.account_id = ?";
			SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountID);
			if (results.next()) {
	            user = mapRowToUser(results);
	        }
				return user; 
			}
	    
	    @Override
	    public int getAccountId(int userID) {
			
			String sql = "SELECT account_id FROM accounts WHERE user_id = ?";
			Integer accountId = -1; 
			
			try {
				accountId = jdbcTemplate.queryForObject(sql, Integer.class, userID); 
			} catch (DataAccessException e) {
	    		System.out.println("Error Getting Account ID."); // Potentially change later
	    	}
			
			return accountId; 
		}

	    
	    @Override
	    public List<Transfer> getTransfersByUser(int accountId) {
	    	
	    	List<Transfer> transferList = new ArrayList<>();
	    	String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount "
	    			+ "FROM transfers t WHERE account_from = ? OR account_to = ?";
	    	
	    	try {
	    		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId); 
	    		
	    		while (results.next()) {
	    			Transfer newTransfer = mapRowToTransfer(results); 
	    			User user = findUserByAccountID(newTransfer.getAccountFrom());
	    			newTransfer.setFromUserName(user.getUsername());
	    			
	    			user = findUserByAccountID(newTransfer.getAccountTo());
	    			newTransfer.setToUserName(user.getUsername());
	    			
	    			transferList.add(newTransfer);
	    		}
	    		
	    		
	    	} catch (DataAccessException e) {
	    		System.out.println("Error Getting Transfers."); // Potentially change later
	    	}
	    	
	    	return transferList;
	    }
	    
	    @Override
	    public Transfer getTransferByTransferId(int transferId) {
	    	
	    	Transfer newTransfer = new Transfer(); 
	    	String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
	    	
	    	try {
	    		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
	    		
	    		while (results.next()) {
	    		newTransfer = mapRowToTransfer(results); 
    			User user = findUserByAccountID(newTransfer.getAccountFrom());
    			newTransfer.setFromUserName(user.getUsername());
    			
    			user = findUserByAccountID(newTransfer.getAccountTo());
    			newTransfer.setToUserName(user.getUsername());
	    		}
	    	} catch (DataAccessException e) {
	    		System.out.println("Error Getting Transfer."); // Potentially change later
	    	}
	    	
	    	return newTransfer;
	    }
	    
	    @Override
	    public void updateTransferStatusAndType(int transferStatusId, int transferId) {
	    	
	    	String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?"; 
	    	
	    	try {
	    	jdbcTemplate.update(sql, transferStatusId, transferId);
	    	
	    	} catch(DataAccessException e) {
	    		System.out.println("Error Updating Transfer."); // Potentially change later
		    }
	    }
	    
	    @Override 
	    public void deleteSentPendingTransfer(int transferId) {
	    	
	    	String sql = "DELETE FROM transfers WHERE transfer_id = ?";
	    	
	    	try {
	    		jdbcTemplate.update(sql, transferId);
	    	} catch(DataAccessException e) {
	    		System.out.println("Error Deleting Transfer."); // Potentially change later
		    }
	    	
	    }
	    

	    private User mapRowToUser(SqlRowSet rs) {
	        User user = new User();
	        user.setId(rs.getLong("user_id"));
	        user.setUsername(rs.getString("username"));
	        user.setActivated(true);
	        user.setAuthorities("USER");
	        return user;
	    }

	    private Transfer mapRowToTransfer(SqlRowSet rs) {
	    	Transfer transfer = new Transfer();
	    	
	    	transfer.setTransferId(rs.getInt("transfer_id"));
	    	transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
	    	transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
	    	transfer.setAccountFrom(rs.getInt("account_from"));
	    	transfer.setAccountTo(rs.getInt("account_to"));
	    	transfer.setAmount(rs.getBigDecimal("amount"));
	    	
	    	return transfer;
	    }
	    
}
