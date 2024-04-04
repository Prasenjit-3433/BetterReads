# Better Reads: A Highly Scalable Book Tracking Application

**Better Reads** is a full-stack application inspired by GoodReads, built with a focus on scalability and high performance.  The primary objective is to create an application that can handle and serve a vast catalog of every book ever published in the world, allowing users to browse, track their reading progress, and rate books efficiently, even with millions of data records.

## Key Features

* **Comprehensive Book Catalog**: Store and serve information on every book ever published globally.
* **Book Tracking**: Users can mark books as read, currently reading, and rate them on a 5-star scale.
* **Reading Progress Tracking**: Users can track their reading progress and view their recently read books.
* **Highly Scalable Architecture**: Designed to handle large amounts of data and scale seamlessly with increasing user load.
* **High Performance**: Optimized for fast page loads and efficient data retrieval, ensuring a smooth user experience.

## Tech Stack

* **Backend**: Spring Boot
* **Database**: Apache Cassandra (using DataStax Astra DB, a hosted Cassandra service)
* **Security**: Spring Security with GitHub OAuth login
* **View Rendering**: Thymeleaf
* **Data Access**: Spring Data Cassandra (repository pattern)

## Architecture Highlights

* **NoSQL Database (Apache Cassandra)**: Chosen for its ability to handle large amounts of data efficiently and scale horizontally.
* **Hosted Cassandra Service (DataStax Astra DB)**: Provides a managed Cassandra instance, eliminating the need for local installation and scaling based on load.
* **GitHub OAuth Integration**: Secure user authentication and authorization using GitHub OAuth.
## User Experience (UX)

The application offers a user-friendly experience for browsing and tracking books. Here's a quick overview of the key functionalities:

**Pages:**

* **Book:** View book details (cover, title, description). Logged-in users can mark books as "Read," "Currently Reading," or "Not Read." Optionally, a rating system can be implemented.
* **Author:** Lists all books written by a particular author in reverse chronological order (newest first). Accessible by clicking the author's name on the Book page.
* **Search:** Enables searching for books by title.
* **Login:** Uses OAuth (Github, Facebook) for user authentication (not required for basic search).
* **Home (Logged Out):** Provides search functionality.
* **Home (Logged In):** "My Books" section displays the user's recently read books (up to 50) in reverse chronological order, with the currently reading book at the top.

**User Flows:**

* Users can browse the app without logging in (search for books).
* Logged-in users can track their reading progress by marking books.
* Clicking on an author's name leads to the Author Page showcasing all their books.

