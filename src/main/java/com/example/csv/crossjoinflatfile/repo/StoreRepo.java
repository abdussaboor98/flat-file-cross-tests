package com.example.csv.crossjoinflatfile.repo;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.QueryHint;
import com.example.csv.crossjoinflatfile.entity.CombinedEntity;
import com.example.csv.crossjoinflatfile.entity.StoreEntity;

@Repository
public interface StoreRepo extends JpaRepository<StoreEntity, Long> {
	
	@QueryHints(value = {
            @QueryHint(name = "org.hibernate.fetchSize", value = "" + Integer.MIN_VALUE)
    })
	@Query("SELECT new com.example.csv.crossjoinflatfile.entity.CombinedEntity(s.storeId,s.name,s.street,s.city,s.country,s.sundays,s.owner,i.itemId,i.name,i.quantity,i.price) FROM StoreEntity s, ItemEntity i")
	Stream<CombinedEntity> fetchStoresCrossItems();
}
