# Tech Eazy Backend

## Overview

The **Tech Eazy Backend** project is a backend system designed to manage and process parcel delivery data efficiently. This repository contains APIs for handling various operations such as uploading parcels, retrieving parcel details, filtering by attributes like size and weight, and performing CRUD operations. The system is powered by *Jakarta EE*, *Spring Data JPA*, and *Spring MVC*, ensuring robust and scalable application architecture.

---

## Features

This backend provides a comprehensive API set to manage parcel delivery services:

### Core Functionalities:
1. **Upload Parcel(s):**
    - Allows bulk or individual parcel uploads with information like customer name, address, size, weight, and contact number.

2. **Retrieve All Parcels:**
    - Fetch details of all uploaded parcels.

3. **Filter Parcels:**
    - Filter based on parcel size (e.g., Small, Medium, Large).
    - Filter by weight range (e.g., between 2kg and 6kg).

4. **Search Parcels:**
    - Search by contact number or customer name.
    - Find a parcel by its unique ID.

5. **Update Parcel:**
    - Modify parcel details using its unique ID.

6. **Delete Parcel:**
    - Delete parcel records by ID.
    - Delete parcel records by contact number.

---

## API Details

### Base URL:
- `http://localhost:80/api/parcels`

### Endpoints:
1. **Upload Parcels**
    - **Method:** `POST`
    - **Endpoint:** `/upload`
    - **Payload:** Array of parcels in JSON format.

2. **Get All Parcels**
    - **Method:** `GET`
    - **Endpoint:** `/all`

3. **Filter by Size**
    - **Method:** `GET`
    - **Endpoint:** `/filter/by-size`
    - **Query Param:** `size`

4. **Filter by Weight**
    - **Method:** `GET`
    - **Endpoint:** `/filter/by-weight`
    - **Query Params:** `min`, `max`

5. **Search by Contact Number**
    - **Method:** `GET`
    - **Endpoint:** `/search-by-number`
    - **Query Param:** `contactNumber`

6. **Search by Customer Name**
    - **Method:** `GET`
    - **Endpoint:** `/search-by-name`
    - **Query Param:** `name`

7. **Find Parcel by ID**
    - **Method:** `GET`
    - **Endpoint:** `/findbyid/{id}`

8. **Update Parcel**
    - **Method:** `PUT`
    - **Endpoint:** `/update/{id}`
    - **Payload:** JSON object with updated details.

9. **Delete Parcel by ID**
    - **Method:** `DELETE`
    - **Endpoint:** `/deletebyid/{id}`

10. **Delete Parcel by Contact Number**
    - **Method:** `DELETE`
    - **Endpoint:** `/delete-by-contact`
    - **Query Param:** `number`

---

## Getting Started

### Prerequisites:
1. Java SDK 21 or higher.
2. Spring dependencies.
3. A database instance (H2 In-Memory Database).
4. Postman (optional, for testing API).

### Installation:
1. Clone the repository:
   ```bash
   git clone https://github.com/Prabal864/tech_eazy_backend_Prabal864.git
   cd tech_eazy_backend_Prabal864
   ```

2. Configure application properties for database connectivity (update `application.properties`).

3. Build the project:
   ```bash
   .\mvnw clean install
   ```

4. Run the project:
   ```bash
   .\mvnw spring-boot:run
   ```

### Testing APIs:
- Import the `postman_collection.json` file into Postman to access pre-configured requests for all API endpoints.

---

## File Structure

1. **Backend Source Code:** `/src` - Contains the main application source files.
2. **Configurable Properties:** `/resources` - Spring configuration and properties files.
3. **Postman Collection:** `postman_collection.json` to test API functionality directly.

---

## Additional Configuration

- **In-Memory Database:** The project uses an H2 database for data storage.
    - The H2 console is accessible at: `http://localhost:80/h2-console`
    - Default JDBC URL: `jdbc:h2:mem:testdb`
    - Username: `sa`
    - Password: *(blank)*

- **Server Port:** The application runs on port `80`.

- **APIs Testing:** Postman

- **Build Tool:** Maven

---

## Contact

If you have any questions or encounter any issues, feel free to reach out:
- **Author:** Prabal Pratap Singh

