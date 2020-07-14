package com.example.csv.crossjoinflatfile.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table( name = "items")
public class ItemEntity {

	@Id
	long itemId;
	String name;
	int quantity;
	String price;
	
}
