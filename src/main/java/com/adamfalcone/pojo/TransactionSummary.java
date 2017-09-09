package com.adamfalcone.pojo;

/**
 * Created by adamfalcone on 8/19/17.
 */
public class TransactionSummary {
    public String id;
    public String sales_person;
    public String first_name;
    public String last_name;
    public String total_sale;
    public String transaction_date;

    public TransactionSummary(String id, String sales_person, String first_name, String last_name, String total_sale, String transaction_date) {
        this.id = id;
        this.sales_person = sales_person;
        this.first_name = first_name;
        this.last_name = last_name;
        this.total_sale = total_sale;
        this.transaction_date = transaction_date;
    }

    public TransactionSummary() {
    }
}
