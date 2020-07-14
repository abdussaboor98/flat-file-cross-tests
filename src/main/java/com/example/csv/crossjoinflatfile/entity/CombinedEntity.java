package com.example.csv.crossjoinflatfile.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
//@Entity
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "stores_items")
public class CombinedEntity {
	@Id
	long storeId;
	String storeName;
	String street;
	String city;
	String country;
	boolean sundays;
	String owner;
	long itemId;
	String itemName;
	int quantity;
	String price;

	public CombinedEntity(StoreEntity store, ItemEntity item) {
		this.storeId = store.getStoreId();
		this.storeName = store.getName();
		this.street = store.getStreet();
		this.city = store.getCity();
		this.country = store.getCountry();
		this.sundays = store.isSundays();
		this.owner = store.getOwner();
		this.itemId = item.getItemId();
		this.itemName = item.getName();
		this.quantity = item.getQuantity();
		this.price = item.getPrice();
	}

	@Override
	public String toString() {
		String entity = String.format("%-7s%-20s%-16s%-9s%-2s%-5s%-19s%-5s%-9s%-4s%-9s", storeId, storeName, street, city,country,sundays,owner,itemId,itemName,quantity,price);
		return entity;
	}
	
	
}
