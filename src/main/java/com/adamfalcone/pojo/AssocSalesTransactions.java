package com.adamfalcone.pojo;

import java.util.List;

/**
 * Created by adamfalcone on 8/19/17.
 */
public class AssocSalesTransactions {
    private List<TransactionSummary> transactions;
    private Integer transaction_count;
    private String sales_person;

    public AssocSalesTransactions(TransactionSummary transaction, Integer transaction_count, String sales_person) {
        this.setTransaction(transaction);
        this.setTransactionCount(transaction_count);
        this.setSalesPerson(sales_person);
    }

    public AssocSalesTransactions(List<TransactionSummary> transactions, Integer transaction_count, String sales_person) {
        this.setTransactions(transactions);
        this.setTransactionCount(transaction_count);
        this.setSalesPerson(sales_person);
    }

    public AssocSalesTransactions() {
    }

    public List<TransactionSummary> getTransactions() {
        return transactions;
    }

    public void setTransaction(TransactionSummary transaction) {
        this.transactions.add(transaction);
    }

    public void setTransactions(List<TransactionSummary> transactions) {
        this.transactions.addAll(transactions);
    }

    public Integer getTransactionCount() {
        return transaction_count;
    }

    public void setTransactionCount(Integer transaction_count) {
        this.transaction_count = transaction_count;
    }

    public String getSalesPerson() {
        return sales_person;
    }

    public void setSalesPerson(String sales_person) {
        this.sales_person = sales_person;
    }
}
