package com.example.csv.crossjoinflatfile;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StopWatch;

import com.example.csv.crossjoinflatfile.entity.CombinedEntity;
import com.example.csv.crossjoinflatfile.entity.ItemEntity;
import com.example.csv.crossjoinflatfile.entity.StoreEntity;
import com.example.csv.crossjoinflatfile.repo.ItemRepo;
import com.example.csv.crossjoinflatfile.repo.StoreRepo;
import com.example.csv.crossjoinflatfile.service.impl.MyService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class CrossJoinFlatFileApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context =  SpringApplication.run(CrossJoinFlatFileApplication.class, args);
		
		MyService service = context.getBean(MyService.class);
		StopWatch stopWatch = context.getBean(StopWatch.class);
		
		stopWatch.start();
		service.deleteAllFromDb();
		service.method1();
		service.writeToFile();
//		service.method2();
//		service.method3();
		stopWatch.stop();
		log.info("Total time taken: {}s", stopWatch.getTotalTimeSeconds());
		
	}

}
