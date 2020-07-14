package com.example.csv.crossjoinflatfile.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.example.csv.crossjoinflatfile.entity.CombinedEntity;
import com.example.csv.crossjoinflatfile.entity.ItemEntity;
import com.example.csv.crossjoinflatfile.entity.StoreEntity;
import com.example.csv.crossjoinflatfile.repo.ItemRepo;
import com.example.csv.crossjoinflatfile.repo.StoreRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyService {

	@Autowired
	StopWatch stopWatch;

	@Autowired
	FlatFileItemReader<ItemEntity> itemFileReader;
	@Autowired
	FlatFileItemReader<StoreEntity> storeFileReader;
	@Autowired
	FlatFileItemWriter<CombinedEntity> combinedFileWriter;

	@Autowired
	ItemRepo itemRepo;
	@Autowired
	StoreRepo storeRepo;

	

	@Value("${path.flatfile.item}")
	private String itemFilePath;

	@Value("${path.flatfile.store}")
	private String storeFilePath;

	@Value("${path.flatfile.output}")
	private String outputFilePath;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void deleteAllFromDb() {

		storeRepo.deleteAllInBatch();
		itemRepo.deleteAllInBatch();
	}

	// Method 1: Read using spring batch, store both files in db, cross join using
	// db
	// 325.8290353s 378.3400495s
	public void method1() throws Exception {

		ItemEntity item;
		StoreEntity store;
		CombinedEntity entity;

		try {
			itemFileReader.open(new ExecutionContext());
			storeFileReader.open(new ExecutionContext());
			combinedFileWriter.open(new ExecutionContext());
			while ((item = itemFileReader.read()) != null) {
				itemRepo.save(item);
			}
			while ((store = storeFileReader.read()) != null) {
				storeRepo.save(store);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			itemFileReader.close();
			storeFileReader.close();
			combinedFileWriter.close();
		}
	}

	@Transactional(readOnly = true)
	public void writeToFile() throws Exception {
		File outputFile = new File(outputFilePath);
		if (outputFile.exists()) {
			outputFile.delete();
			outputFile.createNewFile();
		}
		BufferedWriter bwOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
		storeRepo.fetchStoresCrossItems().forEach(record -> {
			try {
				bwOutput.write(record.toString());
//				log.info("Print: {}", record.toString());
				bwOutput.newLine();
//				storeRepo.detach(record);
			} catch (IOException e) {
				stopWatch.stop();
				log.info("Total time taken: {}s", stopWatch.getTotalTimeSeconds());
				e.printStackTrace();
			}

		});
	}

	// Method 2: BufferedReader to read files, jdbcTemplate to store in db
	// Store items in db
	// Total time taken: 38.4847331s(2000 x 10000) 38.4859282s 33.5731082s
	// 20000 x 10000 326.2067789s 20GB
	public void method2() throws Exception {

		stopWatch.start();

		File itemFile = new File(itemFilePath);
		File storeFile = new File(storeFilePath);
		File outputFile = new File(outputFilePath);

		if (outputFile.exists()) {
			outputFile.delete();
			outputFile.createNewFile();
		}

		BufferedReader brItem = new BufferedReader(new InputStreamReader(new FileInputStream(itemFile)));
		BufferedReader brStore = new BufferedReader(new InputStreamReader(new FileInputStream(storeFile)));

		BufferedWriter bwOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

		jdbcTemplate.update("delete from items_temp");
		log.info("Delete all records in items_temp");

		String itemLine;
		String storeLine;
		String printLine;

		while ((itemLine = brItem.readLine()) != null) {
			jdbcTemplate.update("insert into items_temp (entry) values (?)", itemLine);
			log.info("insert into items_temp (entry) values ({})", itemLine);
		}

		jdbcTemplate.setFetchSize(Integer.MIN_VALUE);

		while ((storeLine = brStore.readLine()) != null) {
			SqlRowSet rs = jdbcTemplate.queryForRowSet("select * from items_temp");
			while (rs.next()) {
				printLine = storeLine + rs.getString(1);
				try {
					bwOutput.write(printLine);
					bwOutput.newLine();
				} catch (IOException e) {
					e.printStackTrace();
					log.info("Error writing: {}", printLine);
					stopWatch.stop();
					log.info("Total time taken: {}s", stopWatch.getTotalTimeSeconds());
					bwOutput.close();
					brItem.close();
					brStore.close();
				}
			}
		}
		bwOutput.close();
		brItem.close();
		brStore.close();
		stopWatch.stop();
		log.info("Total time taken: {}s", stopWatch.getTotalTimeSeconds());

	}

	// Method 3: Read into db using Buferred Reader and JdbcTemplate
	// Cross join in db
	// Fetch with Integer.MIN_VALUE
	// Time: 942.3045492s (2000 x 10000)
	// 48.1285421s, 60.742128799s
	public void method3() throws Exception {
		stopWatch.start();

		File itemFile = new File(itemFilePath);
		File storeFile = new File(storeFilePath);
		File outputFile = new File(outputFilePath);

		if (outputFile.exists()) {
			outputFile.delete();
			outputFile.createNewFile();
		}

		BufferedReader brItem = new BufferedReader(new InputStreamReader(new FileInputStream(itemFile)));
		BufferedReader brStore = new BufferedReader(new InputStreamReader(new FileInputStream(storeFile)));

		BufferedWriter bwOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

		jdbcTemplate.update("delete from items_temp");
		jdbcTemplate.update("delete from stores_temp");
		jdbcTemplate.update("delete from combined_temp");
		log.info("Delete all records in items_temp and stores_temp");

		String itemLine;
		String storeLine;
		String printLine;

		while ((itemLine = brItem.readLine()) != null) {
			jdbcTemplate.update("insert into items_temp (entry) values (?)", itemLine);
			log.info("insert into items_temp (entry) values ({})", itemLine);
		}
		while ((storeLine = brStore.readLine()) != null) {
			jdbcTemplate.update("insert into stores_temp (entry) values (?)", storeLine);
			log.info("insert into stores_temp (entry) values ({})", storeLine);
		}
		brItem.close();
		brStore.close();
		jdbcTemplate.update("insert into combined_temp select * from stores_temp cross join items_temp");
		log.info("Cross join statement executed");
		jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
		try {
			jdbcTemplate.query("select * from combined_temp", new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					BufferedWriter bwOutput = null;
					try {
						bwOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					while (rs.next()) {
						String storeLine = rs.getString(1);
						String itemLine = rs.getString(2);
						String printLine = storeLine + itemLine;
						try {
							bwOutput.write(printLine + "\n");
						} catch (IOException e) {
							e.printStackTrace();
							throw new SQLException();
						}
					}
					try {
						bwOutput.close();
					} catch (IOException e) {
						e.printStackTrace();
						throw new SQLException();
					}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		jdbcTemplate.update("delete from items_temp");
//		jdbcTemplate.update("delete from stores_temp");
//		jdbcTemplate.update("delete from combined_temp");

		stopWatch.stop();
		log.info("Total time taken: {}s", stopWatch.getTotalTimeSeconds());
	}
}
