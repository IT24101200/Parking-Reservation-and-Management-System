# Parking System - API Testing Guide

## Overview

This document provides comprehensive testing instructions for the Parking Reservation and Management System authentication endpoints using Postman.

## Prerequisites

1. **Application Running**: Ensure the Spring Boot application is running on `http://localhost:8080`
2. **Database**: MySQL database should be accessible and configured
3. **Postman**: Latest version of Postman installed

## Import Instructions

### 1. Import Collection

1. Open Postman
2. Click **Import** button
3. Select **Upload Files** or drag and drop `Parking_System_API_Tests.postman_collection.json`
4. Collection will be imported with all test cases

### 2. Import Environment

1. Click the gear icon ⚙️ next to environment dropdown
2. Click **Import**
3. Select `Parking_System_Local_Environment.postman_environment.json`
4. Select the imported environment from dropdown

## API Endpoints Overview

### Authentication Endpoints

| Method | Endpoint         | Description             | Public                          |
| ------ | ---------------- | ----------------------- | ------------------------------- |
| `POST` | `/api/register`  | Register new user       | ✅ Yes                          |
| `POST` | `/api/login`     | User login verification | ✅ Yes                          |
| `GET`  | `/api/user/{id}` | Get user profile        | ❌ No (requires authentication) |

## Test Categories

### 1. User Registration Tests

- ✅ **Valid Customer Registration**: Tests successful registration with customer role
- ✅ **Valid Admin Registration**: Tests successful registration with admin role
- ❌ **Duplicate Email**: Tests error handling for existing email
- ❌ **Missing First Name**: Tests validation for required first name
- ❌ **Invalid Email**: Tests email format validation
- ❌ **Short Password**: Tests password length validation

### 2. User Login Tests

- ✅ **Valid Customer Login**: Tests successful customer login
- ✅ **Valid Admin Login**: Tests successful admin login
- ❌ **Invalid Email**: Tests error for non-existent email
- ❌ **Wrong Password**: Tests error for incorrect password
- ❌ **Missing Email**: Tests validation for required email
- ❌ **Missing Password**: Tests validation for required password

### 3. User Profile Tests

- ✅ **Get User Profile**: Tests profile retrieval for valid user
- ❌ **Invalid User ID**: Tests error handling for non-existent user

### 4. Role-Based Registration Tests

Tests registration for all system roles:

- ✅ **Parking Slot Manager**
- ✅ **Finance Executive**
- ✅ **Security Officer**
- ✅ **Customer Support Officer**

### 5. Role-Based Login Tests

Tests login and redirect URLs for all roles:

- ✅ **Parking Slot Manager** → `/slotmanager/dashboard`
- ✅ **Finance Executive** → `/finance/dashboard`
- ✅ **Security Officer** → `/security/dashboard`
- ✅ **Customer Support Officer** → `/support/dashboard`

## Test Data

### Valid Registration Payload

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe.test@example.com",
  "passwordHash": "password123",
  "phoneNumber": "+1234567890"
}
```

### Valid Login Payload

```json
{
  "email": "john.doe.test@example.com",
  "password": "password123"
}
```

## Supported User Roles

| Role                     | Code                       | Dashboard URL            |
| ------------------------ | -------------------------- | ------------------------ |
| Customer                 | `CUSTOMER`                 | `/customer/dashboard`    |
| Admin                    | `ADMIN`                    | `/admin/dashboard`       |
| Parking Slot Manager     | `PARKING_SLOT_MANAGER`     | `/slotmanager/dashboard` |
| Finance Executive        | `FINANCE_EXECUTIVE`        | `/finance/dashboard`     |
| Security Officer         | `SECURITY_OFFICER`         | `/security/dashboard`    |
| Customer Support Officer | `CUSTOMER_SUPPORT_OFFICER` | `/support/dashboard`     |

## Response Formats

### Successful Registration Response

```json
{
  "success": true,
  "message": "Registration successful!",
  "userId": 1,
  "email": "john.doe.test@example.com",
  "role": "CUSTOMER",
  "status": "ACTIVE"
}
```

### Successful Login Response

```json
{
  "success": true,
  "message": "Login successful!",
  "userId": 1,
  "email": "john.doe.test@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "redirectUrl": "/customer/dashboard"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Email is already registered!"
}
```

## Running Tests

### 1. Individual Test Execution

1. Select any test from the collection
2. Click **Send** button
3. Review response and test results in **Test Results** tab

### 2. Collection Runner

1. Click on collection name → **Run**
2. Select environment: **Parking System - Local Environment**
3. Click **Run Parking System - Authentication API Tests**
4. Monitor test execution and results

### 3. Automated Test Sequence

The tests are designed to run in sequence:

1. **Registration Tests** create test users
2. **Login Tests** verify authentication for created users
3. **Profile Tests** verify user data retrieval

## Test Assertions

Each test includes comprehensive assertions:

- **Status Code Validation**: Ensures correct HTTP response codes
- **Response Structure**: Validates JSON response format
- **Business Logic**: Verifies application-specific logic
- **Error Handling**: Confirms proper error messages

### Example Test Script

```javascript
pm.test("Registration successful", function () {
  pm.response.to.have.status(200);
});

pm.test("Response contains success true", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.success).to.eql(true);
});

pm.test("User role is CUSTOMER", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.role).to.eql("CUSTOMER");
});
```

## Troubleshooting

### Common Issues

1. **Connection Refused**

   - Ensure Spring Boot application is running
   - Verify port 8080 is not blocked
   - Check if MySQL database is accessible

2. **404 Not Found**

   - Verify API endpoints are correctly mapped
   - Check if SecurityConfig allows access to `/api/**` endpoints

3. **500 Internal Server Error**

   - Check application logs for detailed error information
   - Verify database connection and schema

4. **Validation Errors**
   - Ensure request payloads match expected format
   - Check required fields are included

### Database Requirements

- MySQL database running on `localhost:3306`
- Database name: `parking_app`
- Username: `root`
- Password: (empty)

## Security Testing

The collection includes security-focused tests:

- **Input Validation**: Tests for SQL injection and XSS prevention
- **Authentication**: Verifies proper password hashing
- **Authorization**: Tests role-based access control
- **Error Handling**: Ensures no sensitive data exposure

## Performance Considerations

- **Response Time**: Monitor API response times
- **Concurrent Users**: Test with multiple simultaneous requests
- **Database Performance**: Monitor query execution times

## Continuous Integration

The Postman collection can be integrated with CI/CD pipelines using:

- **Newman CLI**: Command-line collection runner
- **Postman API**: Automated test execution
- **Jenkins/GitHub Actions**: Integration with build pipelines

### Newman Command Example

```bash
newman run Parking_System_API_Tests.postman_collection.json \
  -e Parking_System_Local_Environment.postman_environment.json \
  --reporters html,cli
```

## Best Practices

1. **Environment Variables**: Use environment variables for dynamic data
2. **Test Data Cleanup**: Clean up test data after test execution
3. **Realistic Data**: Use realistic test data that matches production scenarios
4. **Error Coverage**: Test both success and failure scenarios
5. **Documentation**: Keep API documentation updated with test results

## Support

For issues or questions regarding the API tests:

1. Check application logs in the Spring Boot console
2. Verify database connectivity and schema
3. Review Postman console for detailed request/response information
4. Ensure all dependencies are properly configured

---

**Last Updated**: September 30, 2025  
**Version**: 1.0  
**Author**: Parking System Development Team
