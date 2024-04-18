package com.betterreads.betterreadsdataloader;

import com.betterreads.betterreadsdataloader.author.Author;
import com.betterreads.betterreadsdataloader.author.AuthorRepository;
import com.betterreads.betterreadsdataloader.book.Book;
import com.betterreads.betterreadsdataloader.book.BookRepository;
import com.betterreads.betterreadsdataloader.connection.DataStaxAstraProperties;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BetterreadsDataLoaderApplication {

	@Autowired
	AuthorRepository authorRepository;

	@Autowired
	BookRepository bookRepository;

	@Value("${datadump.location.author}")
	private String authorDumpLocation;

	@Value("${datadump.location.works}")
	private String worksDumpLocation;


	public static void main(String[] args) {
		SpringApplication.run(BetterreadsDataLoaderApplication.class, args);
	}

	private void initAuthors() {
		Path path = Paths.get(authorDumpLocation);

		try(Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				// Read & parse the line
				String jsonString = line.substring(line.indexOf("{"));

				try {
					JSONObject jsonObject = new JSONObject(jsonString);

					// Construct an author object
					Author author = new Author();
					author.setName(jsonObject.optString("name"));
					author.setPersonalName(jsonObject.optString("personal_name"));
					author.setId(jsonObject.optString("key").replace("/authors/", ""));

					// Persist using Repository
					System.out.println("Saving author: " + author.getName() + "...");
					authorRepository.save(author);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			});
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void initWorks() {
		Path path = Paths.get(worksDumpLocation);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

		try(Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				// Read & parse the line
				String jsonString = line.substring(line.indexOf("{"));

				try {
					JSONObject jsonObject = new JSONObject(jsonString);

					// Construct a Book object
					Book book = new Book();
					book.setId(jsonObject.getString("key").replace("/works/", ""));
					book.setName(jsonObject.optString("title"));

					JSONObject descriptionObj = jsonObject.optJSONObject("description");
					if (descriptionObj != null) {
						book.setDescription(descriptionObj.optString("value"));
					}

					JSONObject publishedObj = jsonObject.optJSONObject("created");
					if (publishedObj != null) {
						String dateStr = publishedObj.getString("value");
						book.setPublishedDate(LocalDate.parse(dateStr, dateTimeFormatter));
					}

					JSONArray coversJSONArr = jsonObject.optJSONArray("covers");
					if (coversJSONArr != null) {
						List<String> coverIds = new ArrayList<>();
						for (int i = 0; i < coversJSONArr.length(); i++) {
							coverIds.add(coversJSONArr.getString(i));
						}
						book.setCoverIds(coverIds);
					}

					JSONArray authorJSONArr = jsonObject.optJSONArray("authors");
					if (authorJSONArr != null) {
						List<String> authorIds = new ArrayList<>();
						for (int i = 0; i < authorJSONArr.length(); i++) {
							String authorId = authorJSONArr.
									getJSONObject(i).
									getJSONObject("author").
									getString("key")
									.replace("/authors/", "");

							authorIds.add(authorId);
						}
						book.setAuthorIds(authorIds);

						Stream<String> authorNames = authorIds.stream().map(id -> {
							Optional<Author> optionalAuthor = authorRepository.findById(id);
							if (optionalAuthor.isPresent()) {
								return optionalAuthor.get().getName();
							} else {
								return "Unknown Author";
							}
						});
						book.setAuthorNames(authorNames.collect(Collectors.toList()));
					}



					// Persist using Repository
					System.out.println("Saving book: " + book.getName() + "...");
					bookRepository.save(book);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		} catch(IOException e) {
			e.printStackTrace();
		}
	}


	@PostConstruct
	public void start() {
		initAuthors();
		initWorks();
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

}
