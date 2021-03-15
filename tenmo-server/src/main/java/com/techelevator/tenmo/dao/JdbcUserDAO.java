package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDAO implements UserDAO {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;

    public JdbcUserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
    	String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
    	Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
    	if (id != null) {
    		return id;
    	} else {
    		return -1;
    	}

    }
    
    @Override
    public int findUserIdByAccountId(int accountId) {
    	
    	String sql = "SELECT user_id FROM accounts WHERE account_id = ?";
    	Integer id = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
    	
    	if (id != null) {
    		return id;
    	} else {
    		return -1;
    	}
    
    }
    
    
	@Override
	public User findUserByID(int userID) {
		
		User user = null; 
		String sql = "SELECT * FROM users WHERE user_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userID);
		if (results.next()) {
            user = mapRowToUser(results);
        }
			return user; 
		}

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM users;"; // took out password_hash
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }
    
//    public List<User> findAllPublic() {
//        List<User> users = new ArrayList<>();
//        String sql = "SELECT user_id, username FROM users;";
//        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
//        while(results.next()) {
//            User user = mapRowToUserPublic(results);
//            users.add(user);
//        }
//        return users;
//    }
    

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM users WHERE username ILIKE ?;"; // took out password_hash
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
            }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
                }

        // create account
        sql = "INSERT INTO accounts (user_id, balance) values(?, ?)";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }
    
    @Override
    public BigDecimal getCurrentBalance(int userID) {
    	
    	BigDecimal balance = new BigDecimal(0);
    	String sql = "SELECT balance FROM accounts WHERE user_id = ?";
    	
    	try {
    		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userID);
    		
    		if (results.next()) {
    			balance = results.getBigDecimal("balance"); 
    		}
    		
    	} catch (DataAccessException e) {
    		System.out.println("Error Getting Balance."); // Potentially change later
    	}
    	
    	return balance;
    }
    
    @Override
    public void updateBalance(int userID, BigDecimal newBalance) {
    	
    	String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?"; 
    	
    	try {
    		jdbcTemplate.update(sql, newBalance, userID);
    		
    	} catch (DataAccessException e) {
    		System.out.println("Error Updating Balance."); // Potentially change later
    	}
    	
    }
    

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }
    
//    private User mapRowToUserPublic(SqlRowSet rs) {
//        User user = new User();
//        user.setId(rs.getLong("user_id"));
//        user.setUsername(rs.getString("username"));
//        user.setActivated(true);
//        user.setAuthorities("USER");
//        return user;
//    }
}
