package com.techelevator.tenmo.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;


public class UserService {
	
	  public String AUTH_TOKEN = "";
	  private final String BASE_URL;
	  private final RestTemplate restTemplate = new RestTemplate();
	
	public UserService(String BASE_URL) {
		this.BASE_URL = BASE_URL;

	}
	
	
	public BigDecimal getCurrentBalance(User currentUser) throws UserServiceException  {
		
		BigDecimal currentBalance = new BigDecimal(0); 
		int userID = currentUser.getId();
	
		try {
			currentBalance = restTemplate.exchange(BASE_URL + "users/" + userID + "/balance", HttpMethod.GET, makeAuthEntity(), BigDecimal.class).getBody();
		} catch (RestClientResponseException rcEx) {
			throw new UserServiceException(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		return currentBalance;
	}
	
	public  List<User> getAllUsers() {
		
		User[] allUsersArray = null; 
		List<User> newUsers = new ArrayList<>();
		
		try {
			allUsersArray = restTemplate.exchange(BASE_URL + "users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();

			for (User user : allUsersArray) { // Add array members into the newUsers List
				newUsers.add(user); 
			}

		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		return newUsers;
	}
	
	public void updateBalance(int userID, BigDecimal newBalance) {
		
		try {
			restTemplate.exchange(BASE_URL + "users/" + userID + "/balance", HttpMethod.PUT, makeBigDecimalEntity(newBalance), BigDecimal.class); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
	}
	
	public User findUserById(int userID) {
		
		User user = null; 
		try {
			user = restTemplate.exchange(BASE_URL + "users/" + userID, HttpMethod.GET, makeAuthEntity(), User.class).getBody(); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		return user; 
		
	}
	
	public int getAccountId(int userID) {
		
		int accountId = -1;
		
		try {
			accountId = restTemplate.exchange(BASE_URL + "users/" + userID + "/accountId", HttpMethod.GET, makeAuthEntity(), Integer.class).getBody(); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		
		return accountId; 
		
	}

	public int getUserId(int accountId) {
		
		int userId = -1;
															
		try {
			userId = restTemplate.exchange(BASE_URL + "users/" + accountId + "/userId", HttpMethod.GET, makeAuthEntity(), Integer.class).getBody(); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		
		return userId; 

	}
	
	public void createTransfer(int userID, Transfer transfer) {
		try {
			restTemplate.exchange(BASE_URL + "users/" + userID + "/transfers", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		
		
	}
	
	public List<Transfer> getTransfersByUserId(int userID) {
		
		List<Transfer> transferList = new ArrayList<>(); 
		try {
			Transfer[] transfers = restTemplate.exchange(BASE_URL + "users/" + userID + "/transfers", 
					HttpMethod.GET, makeAuthEntity(),Transfer[].class).getBody();
			
			for (Transfer transfer : transfers) {
				transferList.add(transfer); // Add array members into a list
			}
			
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}

		return transferList; 
	}
	
	public void updateTransfer(int transferId, int transferStatusId) {
		
		try {
			restTemplate.exchange(BASE_URL + "transfers/" + transferId, HttpMethod.PUT, makeTransferStatusIdEntity(transferStatusId), Integer.class);
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		
	}
	
	public void cancelSentRequest(int transferId) {
		
		try {
			restTemplate.exchange(BASE_URL + "transfers/" + transferId, HttpMethod.DELETE, makeAuthEntity(), Transfer.class); 
		} catch (RestClientResponseException rcEx) {
			System.out.println(rcEx.getRawStatusCode() + " : " + rcEx.getResponseBodyAsString());
		}
		
	}
	
	private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setBearerAuth(AUTH_TOKEN);
	    HttpEntity<Transfer> entity = new HttpEntity<>(transfer, headers);
	    return entity;
	  }
	
	private HttpEntity<Integer> makeTransferStatusIdEntity(Integer transferId) {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setBearerAuth(AUTH_TOKEN);
	    HttpEntity<Integer> entity = new HttpEntity<>(transferId, headers);
	    return entity;
	  }
	
	
	private HttpEntity<BigDecimal> makeBigDecimalEntity(BigDecimal balance) {
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    headers.setBearerAuth(AUTH_TOKEN);
		    HttpEntity<BigDecimal> entity = new HttpEntity<>(balance, headers);
		    return entity;
		  }
	
	private HttpEntity makeAuthEntity() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setBearerAuth(AUTH_TOKEN);
	    HttpEntity entity = new HttpEntity<>(headers);
	    return entity;
	  }
	

	

}
