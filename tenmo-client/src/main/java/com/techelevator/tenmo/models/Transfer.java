package com.techelevator.tenmo.models;

import java.math.BigDecimal;

public class Transfer {

		private int transferID;
		private int transferTypeId;
		private int transferStatusId;
		private int accountFrom;
		private int accountTo;
		private BigDecimal amount;
		private String toUserName;
		private String fromUserName;

		
		public Transfer() {
			
		}
		
		public int getTransferID() {
			return transferID;
		}
		
		public void setTransferId(int transferId) {
			this.transferID = transferId;
		}
		
		public int getTransferTypeId() {
			return transferTypeId;
		
		}
		public void setTransferTypeId(int transferTypeId) {
			this.transferTypeId = transferTypeId;
		}
		
		
		public int getTransferStatusId() {
			return transferStatusId;
		}
		
		public void setTransferStatusId(int transferStatusId) {
			this.transferStatusId = transferStatusId;
		}
		
		public int getAccountFrom() {
			return accountFrom;
		}
		
		public void setAccountFrom(int accountFrom) {
			this.accountFrom = accountFrom;
		}
		
		public int getAccountTo() {
			return accountTo;
		}
		
		public void setAccountTo(int accountTo) {
			this.accountTo = accountTo;
		}
		
		public BigDecimal getAmount() {
			return amount;
		}
		
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
		
		public String getToUserName() {
			return toUserName;
		}
		
		public void setToUserName(String toUserName) {
			this.toUserName = toUserName;
		}
		
		public String getFromUserName() {
			return fromUserName;
		}
		
		public void setFromUserName(String fromUserName) {
			this.fromUserName = fromUserName;
		}
		
		
	  @Override
	   public String toString() {
	      return "Transfer{" +
	              "transferId=" + transferID +
	              ", transferTypeId='" + transferTypeId +
	              ", transferStatusId=" + transferStatusId +
	              ", accountFrom=" + accountFrom +
	              ", fromUserName=" + fromUserName +
	              ", accountTo=" + accountTo +
	              ", toUserName=" + toUserName +
	              ", amount=" + amount +
	              
	              '}';
	   }
	}