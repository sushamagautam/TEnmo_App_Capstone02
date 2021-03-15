package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDAO {

    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);
    
    BigDecimal getCurrentBalance(int accountID); 
    
    void updateBalance(int userID, BigDecimal newBalance); 
    
    User findUserByID(int userID); 
    
    int findUserIdByAccountId(int accountId);
    
//    List<User> findAllPublic();
    
}
