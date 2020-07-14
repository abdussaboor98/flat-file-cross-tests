package com.example.csv.crossjoinflatfile.config;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.validation.BindException;

import com.example.csv.crossjoinflatfile.entity.CombinedEntity;
import com.example.csv.crossjoinflatfile.entity.ItemEntity;
import com.example.csv.crossjoinflatfile.entity.StoreEntity;

@Configuration
public class FlatFileReaderWriterConfig {
	
	@Value("${path.flatfile.item}")
	private String itemFilePath;
	
	@Value("${path.flatfile.store}")
	private String storeFilePath;
	
	@Value("${path.flatfile.output}")
	private String outputFilePath;

	@Bean
	public FlatFileItemReader<ItemEntity> itemFileReader() {
		FlatFileItemReader<ItemEntity> reader = new FlatFileItemReader<ItemEntity>();
		reader.setResource(new FileSystemResource(itemFilePath));
		DefaultLineMapper<ItemEntity> lineMapper = new DefaultLineMapper<ItemEntity>();
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
		tokenizer.setNames("itemId","name","quantity","price");
		tokenizer.setColumns(new Range(1,5),new Range(6,14), new Range(15,18),new Range(19));
		lineMapper.setLineTokenizer(tokenizer);
		FieldSetMapper<ItemEntity> fieldSetMapper = new FieldSetMapper<ItemEntity>() {
			
			@Override
			public ItemEntity mapFieldSet(FieldSet fieldSet) throws BindException {
				ItemEntity entity = new ItemEntity();
				
				entity.setItemId(fieldSet.readLong(0));
				entity.setName(fieldSet.readString(1));
				entity.setQuantity(fieldSet.readInt(2));
				entity.setPrice(fieldSet.readString(3));
				
				return entity;
			}
		};
		lineMapper.setFieldSetMapper(fieldSetMapper );
		reader.setLineMapper(lineMapper);
		return reader;
	}
	
	@Bean
	public FlatFileItemReader<StoreEntity> storeFileReader() {
		FlatFileItemReader<StoreEntity> reader = new FlatFileItemReader<StoreEntity>();
		reader.setResource(new FileSystemResource(storeFilePath));
		DefaultLineMapper<StoreEntity> lineMapper = new DefaultLineMapper<StoreEntity>();
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
		tokenizer.setNames("storeId","name","street","city","country","sundays","owner");
		tokenizer.setColumns(new Range(1,7),new Range(8,27), new Range(28,43),new Range(44,52),new Range(53, 54),new Range(55,59), new Range(60));
		lineMapper.setLineTokenizer(tokenizer);
		FieldSetMapper<StoreEntity> fieldSetMapper = new FieldSetMapper<StoreEntity>() {
			
			@Override
			public StoreEntity mapFieldSet(FieldSet fieldSet) throws BindException {
				StoreEntity entity = new StoreEntity();
				
				entity.setStoreId(fieldSet.readLong(0));
				entity.setName(fieldSet.readString(1));
				entity.setStreet(fieldSet.readString(2));
				entity.setCity(fieldSet.readString(3));
				entity.setCountry(fieldSet.readString(4));
				entity.setSundays(fieldSet.readBoolean(5));
				entity.setOwner(fieldSet.readString(6));
				
				return entity;
			}
		};
		lineMapper.setFieldSetMapper(fieldSetMapper );
		reader.setLineMapper(lineMapper);
		return reader;
	}
	
	@Bean
	public FlatFileItemWriter<CombinedEntity> combinedFileWriter() {
		
		FormatterLineAggregator<CombinedEntity> lineAggregator = new FormatterLineAggregator<CombinedEntity>();
		lineAggregator.setFormat("%-7s%-20s%-16s%-9s%-2s%-5s%-19s%-5s%-9s%-4s%-9s");
		BeanWrapperFieldExtractor<CombinedEntity> fieldExtractor = new BeanWrapperFieldExtractor<CombinedEntity>();
		fieldExtractor.setNames(new String[] {"storeId","storeName","street","city","country","sundays","owner","itemId","itemName","quantity","price"});
		lineAggregator.setFieldExtractor(fieldExtractor  );
		FlatFileItemWriter<CombinedEntity> writer = new FlatFileItemWriter<CombinedEntity>();
		writer.setLineAggregator(lineAggregator);
		writer.setResource(new FileSystemResource(outputFilePath));
		writer.setAppendAllowed(true);
		return writer;
	}
}
