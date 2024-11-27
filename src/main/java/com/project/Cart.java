package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cart implements Serializable {
    private long cartId;
    private String type;
    private Set<Item> items = new HashSet<>();

    public Cart() {}
    
    public Cart(String type) {
        this.type = type;
    }

    public long getCartId() {
        return cartId;
    }
    
    public void setCartId(long id) {
        this.cartId = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items != null ? items : new HashSet<>();
    }

    public List<Object[]> queryItems() {
        String query = "SELECT DISTINCT i.cartId, i.name " +
                      "FROM Item i, Cart c " +
                      "WHERE c.cartId = i.cartId " +
                      "AND c.cartId = " + this.cartId;
        return Manager.queryTable(query);
    }

    @Override
    public String toString() {
        String str = Manager.tableToString(queryItems()).replaceAll("\n", " | ");
        return this.getCartId() + ": " + this.getType() + ", Items: [" + str + "]";
    }
}