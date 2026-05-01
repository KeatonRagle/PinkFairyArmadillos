# Pets for All (PFA) — Developer Environment Setup Guide

This document walks you through setting up a local development environment for the PFA project on your own machine. By the end, you will have the full application running locally — frontend, backend, database, and all — and you will know how to use the developer scripts that come with the project. Read this from top to bottom the first time. Steps build on each other.

Tip - open this file with the markdown preview (Not the text editor)

This document was writen by me, Austin Meredith, a member of the original PFA team. I will still be enrolled at ACU during fall semester of 2026 and am happy to answer any questions via email (or at the lib if your problem is bad enough), so do not hesitate to reach out. I do however, ask that you read my full documentation all the way through, maybe make a couple of google searches, and at least attempt to find the answer to your questions before reaching out to me. Youll find that all of this stuff is well documented online, however I am aware that some of this is very nuanced and specific to PFA. This is by no means an easy process so again, please do not hesitate to reach out when you are truly stuck. adm21c@acu.edu

---

## Table of Contents

1. [What You're Setting Up (Big Picture)](#1-what-youre-setting-up-big-picture)
2. [What You Need Before You Start](#2-what-you-need-before-you-start)
3. [What Is Docker? What Is a Dockerfile?](#3-what-is-docker-what-is-a-dockerfile)
4. [Installing Docker on Your Machine](#4-installing-docker-on-your-machine)
   - [Linux](#41-linux)
   - [macOS](#42-macos)
   - [Windows](#43-windows)
5. [Getting the Codebase](#5-getting-the-codebase)
6. [Understanding the Two compose.override.yml Files](#6-understanding-the-two-composeoverrideyml-files)
7. [Understanding the Dev compose.override.yml (Detailed)](#7-understanding-the-dev-composeoverrideyml-detailed)
8. [Configuring Your .env File](#8-configuring-your-env-file)
9. [Making Scripts Executable](#9-making-scripts-executable)
10. [Starting the Application — run.sh](#10-starting-the-application--runsh)
11. [The Developer Scripts (Detailed)](#11-the-developer-scripts-detailed)
    - [run.sh — Start the stack](#111-runsh--start-the-stack)
    - [down.sh — Stop the stack](#112-downsh--stop-the-stack)
    - [logs.sh — View logs](#113-logssh--view-logs)
    - [sql.sh — Open a database console](#114-sqlsh--open-a-database-console)
    - [backup.sh — Back up the database](#115-backupsh--back-up-the-database)
    - [load_from_backup.sh — Restore the database](#116-load_from_backupsh--restore-the-database)
    - [create_admin_account.sh — Create an admin user](#117-create_admin_accountsh--create-an-admin-user)
    - [run_scrape.sh — Trigger the web scraper](#118-run_scrapesh--trigger-the-web-scraper)
    - [test.sh — Run the test suite](#119-testsh--run-the-test-suite)
12. [Common Errors and How to Fix Them](#12-common-errors-and-how-to-fix-them)
    - [Flyway Migration Failures](#121-flyway-migration-failures)
    - [Spring Application Context Not Loading](#122-spring-application-context-not-loading)
    - [Database Not Ready / Connection Refused](#123-database-not-ready--connection-refused)
    - [Port Already in Use](#124-port-already-in-use)
    - [Docker Build Failures](#125-docker-build-failures)
    - [Frontend Not Reflecting Code Changes](#126-frontend-not-reflecting-code-changes)
    - [Tests Failing Unexpectedly](#127-tests-failing-unexpectedly)
13. [Day-to-Day Development Workflow](#13-day-to-day-development-workflow)
14. [Helpful References](#14-helpful-references)
15. [Quick Reference: Ports and URLs](#15-quick-reference-ports-and-urls)

---

## 1. What You're Setting Up (Big Picture)

In the development environment, the full PFA application runs on your own computer inside Docker containers. There is nothing production going on here — no reverse proxy, no real domain name, and no HTTPS — you access everything over `localhost` (your loopback address) on specific port numbers instead.

```
Your Machine
│
├── http://localhost:3000  ──▶  Frontend (React app, served by Nginx inside Docker)
│
├── http://localhost:8080  ──▶  Backend  (Spring Boot Java API inside Docker)
│                                    │
│                               MySQL DB container (internal only)
│                                    │
│                               Chrome/Selenium container (internal only)
│
└── localhost:3306         ──▶  MySQL (talks to your backend via an internal docker network, more on that later)
```

Everything runs in Docker containers, just like in production. The main differences between the dev and prod setups are:

| | Development | Production |
|--|-------------|------------|
| How you access it | `localhost:3000` and `localhost:8080` | Your domain name over HTTPS |
| Reverse proxy | None — ports are exposed directly | Traefik |
| HTTPS | No | Yes (Let's Encrypt) |
| Database storage | Docker-managed volume | Bind mount at `/data/mysql` |
| Config file | Dev `compose.override.yml` | Prod `compose.override.yml` |

---

## 2. What You Need Before You Start

| Item | Notes |
|------|-------|
| A computer | Linux, macOS, or Windows. See Section 4 for important notes on each. |
| Docker Desktop (Mac/Windows) or Docker Engine (Linux) | Covered in Section 4. |
| Git | To use the remote repository. Download at [https://git-scm.com](https://git-scm.com) or install via your package manager. |
| A code editor | [VS Code](https://code.visualstudio.com/) is recommended. [IntelliJ IDEA](https://www.jetbrains.com/idea/) is excellent for the Java backend specifically — ACU students have free access through JetBrains. |
| A terminal | On Linux/macOS, the built-in Terminal works. On Windows, use Windows Terminal with Git Bash or WSL2 (covered in Section 4). |
| The codebase | The development zip from the original team. |

You do **not** need Maven installed on your machine directly. The backend is compiled and run inside Docker using the Maven wrapper (`./mvnw`) that is already part of the project. The `test.sh` script runs the Maven wrapper on your machine's file system (not inside Docker), so Java is needed if you plan to run tests however mvn is not (please run tests) — this is covered in the test script section.

---

## 3. What Is Docker? What Is a Dockerfile?

Before touching the command line, it helps to understand what Docker is actually doing.

### What Is a Container?

Think of a container as a lightweight, self-contained box that includes an application and everything it needs to run — the right version of Java, the right libraries, the right operating system tools — all bundled together. When you start a container, it is completely isolated from your computer's own software. This is why a developer on macOS and a developer on Windows can run the exact same backend without either of them installing Java on their actual computer.

Containers are similar in concept to virtual machines, but much lighter. A virtual machine emulates an entire computer (hardware and all), which is slow and heavy. A container shares the host operating system's core but isolates everything else, making it fast to start and small to store.

### What Is Docker?

Docker is the software that creates, runs, and manages containers. When you run a Docker command, you are asking Docker to spin up (or stop, or inspect) one of these isolated boxes.

### What Is a Docker Image?

An image is the blueprint for a container. It is a read-only snapshot of an operating system plus application code plus dependencies. When you run a container, Docker takes an image and creates a live, running instance of it. You can run many containers from the same image.

### What Is a Dockerfile?

A Dockerfile is a plain text file that tells Docker how to build an image step by step. Think of it like a recipe. Here is a simplified example of what the PFA backend's Dockerfile might look like:

```dockerfile
# Start from an image that already has Java 21 installed
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the container
COPY . .

# Compile the application using the Maven wrapper
RUN ./mvnw clean package -DskipTests

# When the container starts, run the compiled application
CMD ["java", "-jar", "target/pfa.jar"]
```

Each line is a layer. Docker caches layers, so if you change only application code but not dependencies, it can skip re-downloading dependencies on the next build — which is why subsequent builds are faster than the first one.

In the PFA project, the `backend/` and `frontend/` directories each have their own `Dockerfile`. When you run `docker compose up --build`, Docker reads those Dockerfiles and builds images for each service.

### What Is Docker Compose?

Docker Compose is the tool that manages multiple containers together. Instead of starting the backend, frontend, database, and Chrome containers one by one with separate `docker run` commands, Compose reads the `compose.yml` file and starts them all together with a single command — handling networking, environment variables, and dependencies automatically.

---

## 4. Installing Docker on Your Machine

### 4.1 Linux

On Linux, Docker runs natively and is the smoothest experience of any platform.

```bash
# Install Docker using the official script
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to the docker group so you don't need sudo for every command
sudo usermod -aG docker $USER

# Log out and log back in for the group change to take effect, then verify:
docker --version
docker compose version
```

Both commands should print version numbers. If not, check [https://docs.docker.com/engine/install/ubuntu/](https://docs.docker.com/engine/install/ubuntu/).

### 4.2 macOS

On macOS, install **Docker Desktop** — it includes everything you need (Docker Engine, Docker Compose, and a GUI dashboard).

1. Download from [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/).
2. Choose the correct version for your Mac:
   - **Apple Silicon (M1/M2/M3/M4):** Choose "Mac with Apple chip."
   - **Intel Mac:** Choose "Mac with Intel chip." Check by going to Apple menu → About This Mac.
3. Open the downloaded `.dmg`, drag Docker to Applications, and launch it.
4. Docker will ask for your password to install a system component. Allow it.
5. Wait for the Docker icon in the menu bar to stop animating (it is fully started when the icon is still).
6. Open a terminal and verify:
   ```bash
   docker --version
   docker compose version
   ```

**Recommended macOS settings:** Open Docker Desktop → Settings → Resources. Give it at least 4 CPU cores and 6 GB of RAM if your machine has them. The default allocations are often too low for the full PFA stack.

More information: [https://docs.docker.com/desktop/install/mac-install/](https://docs.docker.com/desktop/install/mac-install/)

### 4.3 Windows

> ⚠️ **Read this section carefully before proceeding.** Docker on Windows works, but it requires more setup and has more potential for issues than Linux or macOS.

Docker on Windows requires **WSL2** (Windows Subsystem for Linux 2) — a compatibility layer that runs a real Linux kernel inside Windows. Docker Desktop uses WSL2 as its backend, which gives you much better performance and compatibility than the older Hyper-V backend.

**Step 1: Enable WSL2**

Open PowerShell as Administrator (right-click the Start menu → "Windows PowerShell (Admin)") and run:

```powershell
wsl --install
```

This installs WSL2 and Ubuntu. Your computer will restart. After restarting, Ubuntu will open and ask you to create a username and password — this is your Linux username inside WSL, it does not need to match your Windows username.

If `wsl --install` does not work on your version of Windows, follow Microsoft's manual guide: [https://learn.microsoft.com/en-us/windows/wsl/install](https://learn.microsoft.com/en-us/windows/wsl/install)

**Step 2: Install Docker Desktop**

1. Download Docker Desktop from [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/).
2. Run the installer. Make sure **"Use WSL2 instead of Hyper-V"** is checked during installation.
3. After installation, open Docker Desktop → Settings → Resources → WSL Integration.
4. Enable integration with your Ubuntu WSL2 distribution.
5. Restart Docker Desktop.

**Step 3: Use WSL2 as Your Terminal**

Open **Windows Terminal** (install it from the Microsoft Store if you do not have it) and open an Ubuntu tab. All commands in this guide — and all the project scripts — should be run from inside this Ubuntu terminal, not from PowerShell or Command Prompt.

Inside the Ubuntu terminal, verify Docker works:
```bash
docker --version
docker compose version
```

**Step 4: Clone the Repository Inside WSL (Important)**

When working with Docker on Windows, keep your project files inside the WSL2 file system (`~/` inside Ubuntu), not in a Windows folder like `C:\Users\YourName\`. Docker's file sharing between Windows folders and containers is significantly slower and can cause issues with file watching and hot-reload. Always work inside the Linux file system. Ig you dont have to but its going to make your life easier.

```bash
# Inside your WSL2 Ubuntu terminal
cd ~
git clone https://github.com/your_remote_repo.git
```

You can still open these files in VS Code from Windows — VS Code has a WSL extension that lets you edit WSL files seamlessly. Open VS Code, install the "WSL" extension, then from inside the WSL terminal run:
```bash
code .
```

More information on Docker + WSL2: [https://docs.docker.com/desktop/wsl/](https://docs.docker.com/desktop/wsl/)

---

## 5. Getting the Codebase

You received a development zip file from the team, or your has already started a GitHub repository. Use whichever applies.

### Option A: Clone from GitHub

```bash
git clone https://github.com/your_remote_repo.git
cd your_cloned_repo
```

### Option B: Unzip the Development Zip

If you only have a zip file:

```bash
# Unzip it into a folder
unzip pfa_dev.zip -d ~/PinkFairyArmadillos
cd ~/PinkFairyArmadillos

# Verify the structure looks right
ls
# You should see: backend/  frontend/  scripts/  compose.yml  compose.override.yml  .env.example  etc.
```

---

## 6. Understanding the Two compose.override.yml Files

The project ships with two separate zip files — one for **development** (this guide) and one for **production** (the server — for more detail on prod, see the Production Deployment Guide). Each has its own `compose.override.yml` file, and they are meaningfully different.

Both files are **gitignored** — they are never committed to the Git repository. This is intentional:

- Your personal development settings should not overwrite another developer's settings.
- The production server's settings should never be exposed in the repository.

Because this file is gitignored, you will not see it when you first clone the repo. You need to either copy it from the dev zip or create it manually. The dev zip includes the correct development version — just make sure it ends up at the root of the project (the same folder as `compose.yml`).

**Never commit `compose.override.yml` to Git.** It is in `.gitignore` for good reason. If you ever see it show up in `git status`, do not add it.

---

## 7. Understanding the Dev compose.override.yml (Detailed)

Here is the full development `compose.override.yml` with every line explained. This file merges with `compose.yml` at runtime — settings here override or extend what is in the base file.

```yaml
services:

  # ─────────────────────────────────────────────
  # BACKEND (Spring Boot Java API)
  # ─────────────────────────────────────────────
  backend:
    ports:
      # Exposes the backend directly on port 8080 of your machine.
      # Format: "host_port:container_port"
      # In production, Traefik handles routing so ports are never exposed like this.
      # In dev, this lets you hit the API directly at http://localhost:8080.
      - "8080:8080"

    env_file:
      # Tells Docker to load all variables from your .env file into the container.
      # This is how the backend gets the database credentials without you
      # hardcoding them into the compose file.
      - .env

    environment:
      # Activates the "dev" Spring profile, which loads application-dev.properties
      # inside the backend codebase. This can change things like log verbosity,
      # disable features that only make sense in production, etc.
      SPRING_PROFILES_ACTIVE: dev

      # Sets the database connection URL.
      # "db" is the name of the MySQL container — Docker automatically resolves
      # container names as hostnames on the same internal network.
      # ${MYSQL_DATABASE} is read from your .env file.
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}

      # These two tell the backend where the database is.
      # They are used by parts of the application that build the connection
      # string themselves rather than using the SPRING_DATASOURCE_URL above.
      DB_HOST: db
      DB_PORT: 3306

    depends_on:
      # Ensures the db container is started before the backend tries to start.
      # Note: this does NOT wait for MySQL to be fully ready — just for the
      # container to exist. The healthcheck in compose.yml handles readiness.
      - db

    volumes:
      # Bind mount: maps the ./backups folder in your project root to /backups
      # inside the backend container. Backup files written by the backend
      # land in your project's backups/ folder on your machine,
      # where you can easily access them.
      - ./backups:/backups

  # ─────────────────────────────────────────────
  # FRONTEND (React / Nginx)
  # ─────────────────────────────────────────────
  frontend:
    ports:
      # Exposes the frontend at http://localhost:3000 on your machine.
      # The frontend Nginx container serves on port 80 internally.
      - "3000:80"

  # ─────────────────────────────────────────────
  # DATABASE (MySQL)
  # ─────────────────────────────────────────────
  db:
    volumes:
      # Docker-managed volume: Docker creates a named storage area called "mysql_data"
      # and manages it internally. Unlike production (which uses a bind mount to
      # a specific server path), development uses a managed volume because:
      # - You do not need to know where the files physically live
      # - Docker handles cleanup for you
      # - It works identically on Linux, macOS, and Windows without path issues
      # Your data persists across container restarts as long as you do not
      # delete the volume (avoid "docker compose down -v").
      - mysql_data:/var/lib/mysql

      # Bind mount: maps ./backups in your project to /backups in the container.
      # The database restore script (load_from_backup.sh) uses this to feed
      # backup files into MySQL.
      - ./backups:/backups

# ─────────────────────────────────────────────
# VOLUMES
# ─────────────────────────────────────────────
volumes:
  # Declares the "mysql_data" volume. Docker creates this automatically
  # the first time you run the stack. It persists until you explicitly delete it.
  mysql_data:
```

### Dev vs. Prod: The Volume Difference

You may notice the development database uses `mysql_data:` (a Docker-managed volume) while production uses `/data/mysql:/var/lib/mysql` (a bind mount to a specific server path). Both approaches keep data across restarts. The bind mount in production exists so the server admin always knows exactly where the data lives and can back it up or migrate it easily. In development, the Docker-managed volume is simpler and avoids cross-platform file path issues on Windows and macOS.

---

## 8. Configuring Your .env File

The `.env` file holds the credentials the application uses. It is gitignored and specific to your machine — every developer has their own. You need to create it before starting the application for the first time.

A template is included in the project root as `.env.example`. Copy it and fill it in:

> *Note - Try not to delete the .env.example, keeping it is useful incase you delete it or it randomly dissapears*

```bash
# Copy the template to create your own .env
cp .env.example .env

# Open it in your editor
nano .env
# or: code .env  (if you use VS Code)
```

Fill in each value:

```env
# The name of the MySQL database Docker will create.
# Can be anything — letters and underscores only.
# Example: PFA_DB (This is what we named ours and you might as well do the same)
MYSQL_DATABASE=PFA_DB

# The MySQL username the application connects with.
# Can be anything. This is a made-up credential for a database
# that only exists inside Docker — it never touches the internet.
# Example: pfa_user
MYSQL_USER=pfa_user

# Password for the above user.
# Use something reasonably strong — does not need to be memorable.
# Example: devpassword123
MYSQL_PASSWORD=devpassword123

# The MySQL root password.
# Used by some scripts for administrative operations.
# Use a different value from MYSQL_PASSWORD.
MYSQL_ROOT_PASSWORD=devrootpassword123

# The base URL of the backend API, as the frontend (running in your browser) sees it.
# In development this is always http://localhost:8080 — your browser talks to the
# backend directly, not through a proxy.
VITE_API_BASE_URL=http://localhost:8080
```

> **A note on development passwords:** These credentials only exist inside Docker on your laptop. The database is not reachable from the internet. Your dev passwords do not need to be strong — use something easy to type. Just do not reuse your real passwords, and do not commit this file.

---

## 9. Making Scripts Executable

The scripts in the `scripts/` folder need execute permission before you can run them. This is a one-time step.

```bash
# From the project root
chmod +x scripts/*.sh
```

On Windows (WSL2), run this from inside the WSL2 Ubuntu terminal. If you cloned inside WSL2 (which you should have — see Section 4.3), this will work correctly.

---

## 10. Starting the Application — run.sh

Once Docker is running, your `.env` is configured, and scripts are executable, you are ready to start the application:

```bash
# From the project root
./scripts/run.sh
```

The first run takes several minutes. Docker needs to:
1. Download base images (Java, MySQL, Chrome, Nginx) — this only happens once.
2. Build the backend image (compile all Java code and download Maven dependencies).
3. Build the frontend image (install Node packages and build the React app).

Subsequent starts are much faster because Docker caches layers that have not changed.

When it finishes, you will see:

```
✔ PFA is up!
  Frontend:  http://localhost:3000
  Backend:   http://localhost:8080
```

Open `http://localhost:3000` in your browser — you should see the PFA website.

> **If the backend seems to start but the site does not load data:** Give it 30–60 seconds. The Spring Boot backend takes time to fully initialize, run Flyway database migrations, and become ready to handle requests. The frontend may load before the backend is ready.

---

## 11. The Developer Scripts (Detailed)

All scripts live in the `scripts/` folder. Each one is self-contained — it figures out where the project root is automatically, so you do not need to `cd` into `scripts/` first.

```bash
# Correct — run from the project root
./scripts/run.sh

# Also correct
./path/to/scripts/backup.sh

# Also correct — run from the project root
cd scripts
./sql.sh
```

### 11.1 run.sh — Start the Stack

```bash
./scripts/run.sh
```

**What it does:** Runs `docker compose up -d --build`, which builds the backend and frontend Docker images from the current source code and starts all containers in the background. Shows the URLs when done.

**When to use it:** Every time you start working. Also use it after pulling new code that includes backend or frontend changes — the `--build` flag ensures the images are rebuilt with the latest code.

**Note:** Running this when the stack is already up is safe — Docker only rebuilds and restarts containers whose source has changed.

---

### 11.2 down.sh — Stop the Stack

```bash
./scripts/down.sh
```

**What it does:** Asks for confirmation, then runs `docker compose down`, which stops and removes all running PFA containers. Your database data is preserved (the `mysql_data` volume is not deleted). The same as stopping all the containers to free up memory and CPU when you are done working.

**When to use it:** When you are done for the day, or when you want to do a clean restart.

> ⚠️ This script runs `docker compose down` (no flags), which is safe. Never manually run `docker compose down -v` unless you are fully aware and accept the consicences — that would delete the `mysql_data` volume and wipe your local database. Can be useful at times, MAJOR emphasis on "can".

---

### 11.3 logs.sh — View Logs

```bash
./scripts/logs.sh
```

**What it does:** Runs `docker compose logs`, which prints recent log output from all containers.

**Limitations:** This script prints a snapshot and exits — it does not stream live. For live streaming (which is more useful for debugging), run directly:

```bash
# Live stream all containers — Ctrl+C to stop watching (does NOT stop containers)
docker compose logs -f

# Live stream just the backend
docker compose logs -f backend

# Live stream just the database
docker compose logs -f db

# Last 50 lines of the backend, not live
docker compose logs --tail=50 backend
```

**When to use it:** When something is not working and you want to see what the containers are printing. The backend logs are especially useful — Spring Boot prints detailed startup information, SQL queries, and error stack traces there.

---

### 11.4 sql.sh — Open a Database Console

```bash
./scripts/sql.sh
```

**What it does:** Opens an interactive MySQL shell connected to the PFA database running inside Docker. You can run any SQL query directly — inspect tables, check data, run manual updates, etc.

**What happens in detail:**
1. Loads credentials from your `.env` file.
2. If the `db` container is not running, it starts it and waits for MySQL to become healthy.
3. If it is already running, it connects immediately.
4. Drops you into a MySQL prompt connected to your `MYSQL_DATABASE`.

**Using the MySQL console:**

```sql
-- See all tables in the database
SHOW TABLES;

-- Inspect the structure of a table
DESCRIBE user;

-- Query data
SELECT * FROM user LIMIT 10;

-- Exit the console
exit
```

**When to use it:** Inspecting what Flyway actually created, verifying data is being stored correctly, debugging data issues, running one-off queries you do not want to write a full endpoint for.

> **Tip:** If you prefer a graphical database tool, the MySQL port is exposed at `127.0.0.1:3306` (set in `compose.yml`). You can connect with tools like [DBeaver](https://dbeaver.io/) or [TablePlus](https://tableplus.com/) using the credentials from your `.env` file and `localhost` as the host.

---

### 11.5 backup.sh — Back Up the Database

```bash
./scripts/backup.sh
```

**What it does:** Creates a compressed SQL dump of the entire PFA database and saves it to the `backups/` folder in your project root (which is bind-mounted into both the backend and db containers). The file is named with a timestamp, e.g. `pfa_2025-04-30_143022.sql.gz`.

**What happens in detail:**
1. Checks that Docker Compose is available.
2. Reads `MYSQL_ROOT_PASSWORD` and `MYSQL_DATABASE` from your `.env`.
3. Verifies the `db` container is running.
4. Runs `mysqldump` inside the container with `--single-transaction` (safe for live databases) and pipes the output through `gzip` for compression.
5. Prints the filename and how long it took.

**When to use it:**
- Before making risky database schema changes.
- Before restoring from another backup.
- Periodically to preserve your local test data.

The `backups/` folder is gitignored — your backup files will not be committed.

---

### 11.6 load_from_backup.sh — Restore the Database

```bash
./scripts/load_from_backup.sh
```

**What it does:** Lists available backup files and lets you restore one of them into the running database. Works for both dev (using `./backups/`) and prod (using `/data/pfa_db/backups/`).

**What happens in detail:**
1. Asks you to choose dev or prod environment.
2. Points at the appropriate backups directory based on your choice.
3. Reads credentials from `.env` (or prompts you to enter them manually if the file is not found).
4. Lists the 10 most recent `.sql.gz` backup files with their size and date.
5. Asks you to pick one.
6. Shows a warning — **this will overwrite the existing database** — and requires you to type `yes` to confirm.
7. Waits for MySQL to be ready, then decompresses and restores the backup.

**When to use it:**
- Restoring your local database after something went wrong.
- Loading a backup from production to test against real data locally (be mindful of any sensitive user data).
- Resetting to a known-good state after a bad migration.

> ⚠️ This is a destructive operation. The restore overwrites the current database. Always back up first if you have data you want to keep.

---

### 11.7 create_admin_account.sh — Create an Admin User

```bash
./scripts/create_admin_account.sh
```

**What it does:** Creates an admin user account in the running PFA application. Because the registration endpoint creates regular users, this script does the extra step of directly updating the database to give the account `ROLE_ADMIN`.

**What happens in detail:**
1. Checks that the backend container is running.
2. Prompts you for the admin password (the email is hardcoded to `admin@pfa.com` — check the script if you need to change it).
3. Calls the `/api/users/register` endpoint via `curl` to create the account.
4. If the account already exists (HTTP 409), it skips registration and proceeds.
5. Runs a SQL `UPDATE` directly against the database to set `role = 'ROLE_ADMIN'`.
6. Verifies the role was set correctly.

**When to use it:** After a fresh start with a clean database, before you can access any admin-protected features in the application. You will probably run this once per fresh environment.

**Note:** The admin email (`admin@pfa.com`) is set inside the script. If you need a different email, edit the `ADMIN_EMAIL` variable near the top of `create_admin_account.sh`.

---

### 11.8 run_scrape.sh — Trigger the Web Scraper

```bash
./scripts/run_scrape.sh
```

**What it does:** Triggers the PFA web scraper by authenticating and calling the scrape API endpoint. The scraper is what populates the database with pet listings.

**What happens in detail:**
1. Asks whether you want to scrape against dev (`http://localhost:8080`) or prod (`https://api.adoptpetsforall.com`).
2. For dev, verifies the backend and database containers are running.
3. Prompts for an admin email and password.
4. Logs in via the API and retrieves a JWT token.
5. Calls the `/api/webScraper/scrape` endpoint with that token.
6. The scrape runs server-side inside the Chrome container — it may take several minutes.

**When to use it:** When you need pet data in your local database. The application starts with an empty database, so running the scraper (or restoring from a backup) is how you get data to work with.

> **Note:** The scrape uses the Chrome/Selenium container to run a headless browser. If Chrome is not running, start the full stack first with `./scripts/run.sh`.

---

### 11.9 test.sh — Run the Test Suite

```bash
./scripts/test.sh
```

**What it does:** Runs the backend's Maven test suite. Unlike the other scripts, this one does NOT run tests inside Docker — it runs Maven directly on your machine, in the `backend/` directory, using the `./mvnw` Maven wrapper that is already included in the project.

> **Prerequisite:** Java must be installed on your machine to run the tests.

**Interactive menu:**

```
What do you want to run?

  1) All tests
  2) Specific class
  3) Specific class + method
  4) Clean build only (skip tests)
```

**Or pass arguments directly:**

```bash
# Run a specific test class
./scripts/test.sh --class UserControllerTest

# Run a specific method within a class
./scripts/test.sh --class UserControllerTest --method testLogin
```

**What happens in detail:**
- Option 1 runs `./mvnw clean test`.
- Option 2 runs `./mvnw clean test -Dtest=ClassName`.
- Option 3 runs `./mvnw clean test -Dtest=ClassName#methodName`.
- Option 4 runs `./mvnw clean install -DskipTests` (compiles without running tests).

**When to use it:** Before opening a pull request to make sure your changes do not break existing tests. When debugging a specific failing test. When you want to verify a new feature is covered.

**Installing Java for tests:** The Maven wrapper handles everything except Java itself. Install Java 21 (to match the project):

- Linux: `sudo apt install openjdk-21-jdk`
- macOS: `brew install openjdk@21`
- Windows: Download from [https://adoptium.net](https://adoptium.net) and select Java 21.

---

## 12. Common Errors and How to Fix Them

This section covers the errors you are most likely to hit, what causes them, and how to fix them.

### 12.1 Flyway Migration Failures

**What is Flyway?** Flyway is a database migration tool. Instead of manually running SQL to update the database schema, you write numbered migration files (e.g. `V1__create_users.sql`, `V2__add_pets_table.sql`) that Flyway runs in order. Every time the backend starts, Flyway checks which migrations have already been applied and runs any new ones.

**What the error looks like:**

```
FlywayException: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 3
```

or:

```
FlywayException: Found non-empty schema(s) "PFA_DB" but no schema history table
```

**Why it happens:** Flyway checksums every migration file. If you:
- Edit a migration file that has already been run
- Delete a migration file
- Change the filename of a migration
- Start with a database that has tables but no Flyway history

...Flyway will refuse to start because the history does not match reality.
- **DO NOT EVER change a migration file unless you know how to fix the problems it comes with**
- **Migration files REQUIRE a specific naming convention.**
    -  "V[version number]__[name].sql"
    - You MUST include two (count em, two) underscores between the version number and the name.

**How to fix it:**

**Option A (simplest for dev) — Wipe the database and start fresh:**

```bash
# Stop the stack
./scripts/down.sh

# Delete the mysql_data volume (this wipes all data — only do this in dev)
docker volume rm pfa_mysql_data
# The volume name is usually the compose project name + "_mysql_data"
# To find the exact name:
docker volume ls | grep mysql

# Start fresh — Flyway will run all migrations from scratch
./scripts/run.sh
```

**Option B — Fix the migration file:**

If you accidentally edited a migration that already ran, revert it to its original content. Flyway validates by checksum, so the file must match exactly what was applied.

**Option C — Repair Flyway:**

If the failure left a partial migration marked as failed in the `flyway_schema_history` table, you can repair it:

```bash
# Open the SQL console
./scripts/sql.sh

-- Remove the failed migration entry so Flyway can try again
DELETE FROM flyway_schema_history WHERE success = 0;
exit
```

Then restart the backend:
```bash
docker compose restart backend
```

**Golden Rule:** In development, once a migration file has been committed, never edit it. If the schema needs to change, write a new migration file with the next version number.

---

### 12.2 Spring Application Context Not Loading

**What the error looks like:**

```
APPLICATION FAILED TO START

Description:
Failed to configure a DataSource: 'url' attribute is not specified

or:

Error creating bean with name 'someBean': Unsatisfied dependency expressed through field 'someRepository'
```

The backend fails to start and the logs show `APPLICATION FAILED TO START` followed by a description.

**Common causes and fixes:**

**Missing or wrong `.env` values:**
The most common cause. Open your `.env` and verify every variable is set — no angle brackets (`<>`), no placeholder text, and especially check `MYSQL_DATABASE`, `MYSQL_USER`, and `MYSQL_PASSWORD`. Then rebuild:
```bash
./scripts/down.sh
./scripts/run.sh
```

**Spring profile mismatch:**
The `compose.override.yml` sets `SPRING_PROFILES_ACTIVE: dev`. If `application-dev.properties` does not exist in the backend or has a typo, Spring cannot find its configuration. Check the backend's `src/main/resources/` folder.

**Bean wiring errors:**
These appear as `Error creating bean with name...`. They usually mean a Java class has a dependency that cannot be satisfied — a missing `@Component`, a wrong import, or a compile error. Check the full stack trace in the logs for the root cause:
```bash
docker compose logs backend | grep -A 20 "APPLICATION FAILED TO START"
```

**Port conflict (8080 already in use):**
If something on your machine is already using port 8080, Docker cannot bind to it and the backend will fail to start. See Section 12.4.

---

### 12.3 Database Not Ready / Connection Refused

**What the error looks like:**

```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
or:
Connection refused: db/172.x.x.x:3306
```

**Why it happens:** The backend started before MySQL finished initializing. The `compose.yml` includes a healthcheck on the `db` service and a `depends_on: condition: service_healthy` on the backend — but sometimes MySQL takes longer than expected, especially on the very first run when it is initializing the data directory.

**How to fix it:**

Usually just waiting and checking again fixes it:
```bash
# Watch the database logs to see when it is ready
docker compose logs -f db
# Look for: "ready for connections" — that means MySQL is up
```

If the backend gave up waiting, restart just the backend (not the whole stack) once the database is healthy:
```bash
docker compose restart backend
```

If this happens repeatedly, increase the `start_period` in the db healthcheck in `compose.yml` to give MySQL more time.

---

### 12.4 Port Already in Use

**What the error looks like:**

```
Error response from daemon: driver failed programming external connectivity:
Bind for 0.0.0.0:8080 failed: port is already allocated
```

**Why it happens:** Another application on your machine is already using port 8080 (backend), 3000 (frontend), 4444 (Selenium), or 3306 (MySQL). This can also happen if a previous PFA container did not shut down cleanly.

**How to find what is using the port:**

```bash
# Linux/macOS — find what is on port 8080
sudo lsof -i :8080

# Windows (in PowerShell)
netstat -ano | findstr :8080
```

**How to fix it:**

Option A — Stop the other application using that port.

Option B — If it is a stale PFA container from a previous session:
```bash
./scripts/down.sh
# Then try starting again
./scripts/run.sh
```

Option C — Change the port in `compose.override.yml`. For example, to move the backend to port 9090:
```yaml
backend:
  ports:
    - "9090:8080"  # your machine's port 9090 → container's port 8080
```
Then update `VITE_API_BASE_URL` in `.env` to `http://localhost:9090` and restart.

---

### 12.5 Docker Build Failures

**What the error looks like:**

```
ERROR [backend build 5/8] RUN ./mvnw clean package -DskipTests
...
BUILD FAILURE
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
```

or a dependency download failure:

```
Could not resolve dependencies for project...
```

**Why it happens:**

- A Java compile error in the backend code. Check the build output for the specific file and line number.
- A Maven dependency that cannot be downloaded (network issue, bad repository URL).
- An out-of-date Maven cache with a corrupt artifact.

**How to fix it:**

For compile errors, read the `[ERROR]` lines in the build output — they will point to the exact file and line number.

For dependency issues, try clearing Docker's build cache:
```bash
# Force a complete rebuild with no cache
docker compose build --no-cache backend
```

This is slower (redownloads all dependencies) but eliminates cache corruption as a cause.

---

### 12.6 Frontend Not Reflecting Code Changes

**Why it happens:** The frontend is built into a static bundle during the Docker image build. If you change React code and want to see the changes, the image needs to be rebuilt.

**How to fix it:**

```bash
# Rebuild just the frontend image and restart it
docker compose up -d --build frontend
```

> **For active frontend development:** Rebuilding the Docker image every time you change a file is tedious. You might be able to find a better way. My idea is try to write the frontend dockerfile so that the front end isnt built into a static bundle. Idk if it'll work, just a though I had while writing this.


---

### 12.7 Tests Failing Unexpectedly

**Why it happens:**

- **A missing or wrong environment:** The test suite uses Testcontainers to spin up a real MySQL database for integration tests. Your test class may not extend the PfaBase class.
- **Port conflicts:** Testcontainers picks random ports, but if something is misconfigured it may try a port that is in use.
- **Stale application context:** A previous test run's Spring context cached in the JVM conflicts with new test configuration.
- **A real bug you introduced:** The test is doing its job.

**How to fix it:**

First, look at the actual failure message — not just `BUILD FAILURE`, but the specific test name and the exception:

```bash
# Run the failing test class alone with full output
./scripts/test.sh --class NameOfFailingTestClass
```

If the error is `Cannot connect to Docker daemon` or similar, make sure Docker is running before executing the tests.

If a specific test was passing before your changes and now is not, compare what you changed against what the test expects. Read the test's assertion failure — it usually says exactly what was expected and what was returned.

If tests pass individually but fail together, there may be test isolation issues (one test is leaving dirty state that breaks another). This is a code problem to fix in the tests themselves.

---

## 13. Day-to-Day Development Workflow

Here is what a typical development session looks like from start to finish.

### Starting Your Session

```bash
# 1. Navigate to the project
cd ~/PinkFairyArmadillos   # or wherever you cloned it

# 2. Pull the latest code from the main branch (on your feature branch, merge or rebase from main)
git fetch origin
git merge origin/main   # or git rebase origin/main

# 3. Start the stack (--build ensures any new backend/frontend code is compiled)
./scripts/run.sh

# 4. Verify everything is up
docker compose ps
# All four containers should show "Up" or "Up (healthy)"

# 5. Open the app
# http://localhost:3000  — frontend
# http://localhost:8080  — backend API
```

### Making Changes

**Backend (Java):**
Edit Java files in `backend/src/`. To see your changes:
```bash
docker compose up -d --build backend
```
This rebuilds the backend image and restarts just that container. The database and frontend keep running.

**Frontend (React):**
For quick iteration, run the Vite dev server outside Docker (see Section 12.6) so you get instant hot-reload. For a full build test, rebuild the container:
```bash
docker compose up -d --build frontend
```

**Database Schema (Flyway migrations):**
Add a new file to `backend/src/main/resources/db/migration/` following the naming convention `V{next_number}__{description}.sql`. Restart the backend to apply it:
```bash
docker compose restart backend
# Watch the logs to confirm Flyway picked it up
docker compose logs -f backend
```

### Before Opening a Pull Request

```bash
# Run the full test suite
./scripts/test.sh
# Choose option 1 (all tests)

# Verify the app still works end-to-end in your browser
# http://localhost:3000
```

Make sure:
- All tests pass.
- You did not commit `.env` or `compose.override.yml`.
- Your branch is up to date with `main`.

### Ending Your Session

```bash
./scripts/down.sh
```

Your database data is preserved in the `mysql_data` Docker volume and will be there when you start again.

### Creating a Fresh Environment

If your local database is in a bad state and you want to start completely from scratch:

```bash
./scripts/down.sh

# Find and remove the mysql_data volume
docker volume ls | grep mysql
docker volume rm pfa_mysql_data   # use the exact name from the above command

# Start fresh
./scripts/run.sh

# Create your admin account again
./scripts/create_admin_account.sh

# Optionally re-run the scraper to populate data
./scripts/run_scrape.sh
```

---

## 14. Helpful References

| Topic | URL |
|-------|-----|
| Docker getting started | https://docs.docker.com/get-started/ |
| Docker Compose reference | https://docs.docker.com/compose/ |
| Docker Desktop for macOS | https://docs.docker.com/desktop/install/mac-install/ |
| Docker Desktop for Windows | https://docs.docker.com/desktop/install/windows-install/ |
| Docker + WSL2 guide | https://docs.docker.com/desktop/wsl/ |
| WSL2 installation (Microsoft) | https://learn.microsoft.com/en-us/windows/wsl/install |
| What is a Dockerfile | https://docs.docker.com/engine/reference/builder/ |
| Docker volumes vs bind mounts | https://docs.docker.com/storage/ |
| Spring Boot application properties | https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html |
| Spring profiles | https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles |
| Flyway documentation | https://documentation.red-gate.com/fd |
| Flyway migration naming conventions | https://documentation.red-gate.com/fd/migrations-184127470.html |
| Testcontainers (for understanding the test setup) | https://testcontainers.com/ |
| Maven wrapper (./mvnw) explained | https://maven.apache.org/wrapper/ |
| VS Code WSL extension | https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-wsl |
| DBeaver (free database GUI) | https://dbeaver.io/ |
| TablePlus (macOS/Windows database GUI) | https://tableplus.com/ |
| Adoptium (Java 21 downloads) | https://adoptium.net |

---

## 15. Quick Reference: Ports and URLs

| Service | URL / Address | Notes |
|---------|--------------|-------|
| Frontend | http://localhost:3000 | The React web application |
| Backend API | http://localhost:8080 | The Spring Boot REST API |
| MySQL | localhost:3306 | Connect with a GUI tool using `.env` credentials |
| Selenium Chrome | http://localhost:4444 | Selenium Grid UI (for debugging scraper) |
| Selenium VNC | http://localhost:7900 | Watch the Chrome browser in real time (password: `secret`) |

### Script Quick Reference

| Script | What it does |
|--------|-------------|
| `./scripts/run.sh` | Build and start the full stack |
| `./scripts/down.sh` | Stop and remove containers (data preserved) |
| `./scripts/logs.sh` | Print recent logs from all containers |
| `./scripts/sql.sh` | Open an interactive MySQL console |
| `./scripts/backup.sh` | Create a compressed database backup |
| `./scripts/load_from_backup.sh` | Restore the database from a backup file |
| `./scripts/create_admin_account.sh` | Create an admin user account |
| `./scripts/run_scrape.sh` | Trigger the web scraper to populate data |
| `./scripts/test.sh` | Run the backend test suite |

---

*Documentation prepared by the original PFA development team. For questions about the application code, reach out to the original team. For infrastructure or Docker questions, refer to the linked documentation sources above, or reach out to Austin Meredith adm21c@acu.edu.*