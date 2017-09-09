package com.adamfalcone.pojo;

public class AssociateSalesData {

    public String sales_person;
    public String total_sales;
    public String transaction_count;
    public String average_sale;

    public AssociateSalesData() {
    }

    public AssociateSalesData(String sales_person, String total_sales, String transaction_count, String average_sale) {
        this.sales_person = sales_person;
        this.total_sales = total_sales;
        this.transaction_count = transaction_count;
        this.average_sale = average_sale;
    }
}
