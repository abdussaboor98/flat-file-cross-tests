package com.example.csv.crossjoinflatfile.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.csv.crossjoinflatfile.entity.ItemEntity;

@Repository
public interface ItemRepo extends JpaRepository<ItemEntity, Long>{

}
