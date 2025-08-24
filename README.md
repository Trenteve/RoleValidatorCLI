# Role Validator CLI

## Project Overview

This project is a small-scale simulation of a SailPoint IdentityIQ system. It's a command-line application that demonstrates key identity and access management (IAM) principles using **Java**, **SQLite**, and **BeanShell**.

## Features

* **User Management**: Centralized repository for user identities.

* **Role-Based Access Control (RBAC):** Users are assigned to roles, and access to resources is determined based on these roles. This models the principle of **least privilege**.

* **Automated Provisioning:** Roles are automatically to a user based on business logic defined in the *assignRoles* BeanShell script.

* **Access Certification:** A governance process is included that allows for the review and approval or revocation of a user's access rights.

* **Policy Enforcement:** Business rules, such as **Segregation of Duties (SoD)** policies, are enforced using the *checkPolicy* BeanShell script. This separates policy logic from the core Java code, a key feature of IGA solutions.

## Technical Stack

* **Core Application Logic:** Java
* **Database:** SQLite
* **Policy Engine:** BeanShell
* **User Interface:** Command-Line Interface (CLI)

## How It Works

The application uses a simple, integrated workflow:

1. A user is created in the SQLite database.

2. The assignRoles BeanShell script is executed, which applies provisioning rules to assign roles to the in-memory user object.

3. The application saves these roles to the database.

4. The checkPolicy BeanShell script can check for policy violations.

5. The CLI menu allows for access certification, where a user's roles can be reviewed and de-provisioned.

## Installation and Usage

To run the project, simply compile and run from the Main.java class. All of the dependencies are handled in the Maven project.

The CLI will show you the available options to create users, add roles to users, check their access to resources (via the checkAccess script), and running the certification process.