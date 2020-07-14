package com.example.csv.crossjoinflatfile.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name="stores")
public class StoreEntity {
	
	@Id
	long storeId;
	String name;
	String street;
	String city;
	String country;
	boolean sundays;
	String owner;
}
