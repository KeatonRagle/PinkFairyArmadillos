# Pets for All (PFA) — Production Deployment Guide

This document walks you through everything you need to do to get the PFA application running on your own server, publicly accessible on the internet. Read it from top to bottom the first time — steps build on each other. 

Tip - open this file with the markdown preview (Not the text editor)

This document was writen by me, Austin Meredith, a member of the original PFA team. I will still be enrolled at ACU during fall semester of 2026 and am happy to answer any questions via email (or at the lib if your problem is bad enough), so do not hesitate to reach out. I do however, ask that you read my full documentation all the way through, maybe make a couple of google searches, and at least attempt to find the answer to your questions before reaching out to me. Youll find that all of this stuff is well documented online, however I am aware that some of this is very nuanced and specific to PFA. This is by no means an easy process so again, please do not hesitate to reach out when you are truly stuck. adm21c@acu.edu

---

## Table of Contents

1. [What You're Setting Up (Big Picture)](#1-whats-youre-setting-up-big-picture)
2. [Choosing Your Server OS — Read This First](#2-choosing-your-server-os--read-this-first)
3. [What You Need Before You Start](#3-what-you-need-before-you-start)
4. [A Note on File Paths: ~/ vs /](#4-a-note-on-file-paths--vs-)
5. [Preparing Your Server](#5-preparing-your-server)
6. [Setting Up Your Domain Name](#6-setting-up-your-domain-name)
7. [Opening Ports on Your Network (Port Forwarding)](#7-opening-ports-on-your-network-port-forwarding)
8. [Installing Docker and Docker Compose](#8-installing-docker-and-docker-compose)
9. [Setting Up Tailscale (Remote Access)](#9-setting-up-tailscale-remote-access)
10. [Setting Up the Traefik Reverse Proxy](#10-setting-up-the-traefik-reverse-proxy)
11. [Getting the Codebase onto Your Server](#11-getting-the-codebase-onto-your-server)
12. [Understanding the Two compose.override.yml Files](#12-understanding-the-two-composeoverrideyml-files)
13. [Setting Up the PFA Application](#13-setting-up-the-pfa-application)
14. [Understanding the compose.override.yml File (Detailed)](#14-understanding-the-composeoverrideyml-file-detailed)
15. [Configuring Your .env File](#15-configuring-your-env-file)
16. [First Launch — Starting the Application](#16-first-launch--starting-the-application)
17. [Setting Up the GitHub Actions CD Pipeline](#17-setting-up-the-github-actions-cd-pipeline)
18. [Branch Protection — A Brief Note](#18-branch-protection--a-brief-note)
19. [Verifying Everything Works](#19-verifying-everything-works)
20. [Maintenance and Troubleshooting](#20-maintenance-and-troubleshooting)
21. [Helpful References](#21-helpful-references)
22. [Quick Reference: File Locations](#22-quick-reference-file-locations)

---

## 1. What You're Setting Up (Big Picture)

Before diving into commands, here is a plain-English description of what the finished system looks like and why each piece exists.

```
Internet
    │
    ▼
Your Router (ports 80 & 443 open)
    │
    ▼
Your Server (Linux machine, running headless)
    │
    ▼
Traefik (reverse proxy — traffic cop)
    ├──▶ adoptpetsforall.com  ──▶  Frontend (React/Nginx container)
    └──▶ api.adoptpetsforall.com ──▶  Backend (Spring Boot container)
                                           │
                                           ▼
                                       MySQL Database container
                                           │
                                       Chrome/Selenium container

    ┌─────────────────────────────────────┐
    │  Tailscale (secure remote access)   │
    │  Your laptop ◄──────► Your Server   │
    └─────────────────────────────────────┘
```

**Traefik** sits at the front door. Every request from the internet arrives at Traefik first, and Traefik decides which container to send it to based on the domain name. It also handles HTTPS certificates automatically so all traffic is encrypted.

**The Frontend** is the React website users see in their browsers.

**The Backend** is the Spring Boot Java API that the frontend talks to.

**The Database** is MySQL, which stores all the application data (users, pets, etc.).

**Chrome/Selenium** is a headless browser the backend uses internally for web scraping. Users never interact with it directly.

**Tailscale** creates a private, encrypted tunnel between your devices so you can securely SSH into and manage the server from anywhere without exposing it further to the internet.

**GitHub Actions** is the automated deployment pipeline. Whenever someone merges code into the `main` branch, GitHub automatically connects to your server and updates the running application — you do not have to do it manually.

---

## 2. Choosing Your Server OS — Read This First

> **TL;DR: Use Linux. Ubuntu 22.04 LTS or 24.04 LTS is strongly recommended. Do not use Windows as your server OS.**

This is the most important decision you will make before touching anything else, so it deserves a full explanation.

### Why Not Windows?

Docker — the software that runs the entire PFA application — was designed from the ground up for Linux. On Linux, Docker runs natively using built-in kernel features. On Windows, Docker Desktop has to run a hidden Linux virtual machine inside Windows just to function at all. That extra layer causes real problems:

- **Reliability:** Docker on Windows has well-documented stability issues, especially with networking and file mounts. Things that work perfectly on Linux will behave strangely or fail silently on Windows in ways that are very hard to debug.
- **Networking:** Docker's internal networking — how containers find each other, and how Traefik discovers them automatically — behaves differently on Windows in ways this documentation cannot help you fix.
- **File permissions:** Linux file permissions (like `chmod 600`, which you will use for the HTTPS certificate file) do not exist the same way on Windows. This causes Traefik's Let's Encrypt integration to break.
- **Documentation:** Nearly all Docker, Traefik, and Linux server documentation — including everything in this guide — assumes a Linux environment. Every instruction you find online, and every instruction in this document, is written for Linux. On Windows you would constantly be searching for Windows-specific workarounds that may not exist.
- **Switching away from Docker:** If you decided to run the application without Docker on Windows, it would require rewriting significant parts of the deployment setup. That is not a supported path and would be a large amount of work.

I am being EXTREMELY serious about this! Windows may be familer to you, and it is ok to use in development environments, but it will be the cause of all of your pain and suffering if you use it in the production environment. Trust me, I know Linux can seem daunting but do not be afraid, I have provided explenation on everything you will need to know and using linux is actually pretty easy once you get the hang of things. Using Linux will also teach you incredibly useful skills that you will need in your career if you want to do anything related to tech. Please hear me out, I am avalible and willing to help you in any way if you use linux, but if you use windows I won't be as useful to you as my expertice with the application is rooted in it being run on linux.

### Running Headless

Your production server should run **headless** — meaning no graphical desktop (no taskbar, no file explorer, no mouse-driven interface). A headless Linux server uses dramatically fewer resources because it is not rendering a desktop that nobody is using. You interact with it entirely through a terminal, either physically or remotely via SSH and Tailscale.

The only time you need a monitor and keyboard physically connected to the server is during the initial operating system installation. Once the OS is installed and SSH/Tailscale are configured, you can unplug the monitor and manage everything remotely.

### Can You Use Something Other Than Ubuntu?

Other Linux distributions work too — Debian, Fedora, Arch, etc. However, Ubuntu Server is recommended because it is the most widely documented, and all the commands in this guide use Ubuntu's package manager (`apt`). If you use a different distribution, you will need to translate the package installation commands yourself.

### Recommended OS

**Ubuntu Server 22.04 LTS or 24.04 LTS.** "LTS" stands for Long-Term Support — it receives security updates for 5 years, which matters for a server you want to run reliably without reinstalling the OS frequently. The "Server" edition installs without a desktop environment by default, which is exactly what you want.

Download: [https://ubuntu.com/download/server](https://ubuntu.com/download/server)

If you are renting a VPS (Virtual Private Server) from a provider like DigitalOcean, Hetzner, or Linode, just select Ubuntu 22.04 or 24.04 from their OS picker — no manual install needed.

---

## 3. What You Need Before You Start

Gather these things before touching the server:

| Item | Notes |
|------|-------|
| A Linux server | Ubuntu 22.04 LTS or 24.04 LTS, running headless after OS install. |
| A public IP address | The IP address the internet sees for your server. Your ISP or VPS provider can tell you this. |
| A domain name | Provided by the original team. This is the address users will type (e.g. `adoptpetsforall.com`). |
| SSH access to your server | On Windows use [PuTTY](https://www.putty.org/) or Windows Terminal (PLEASE don't use windows). On Mac/Linux, open Terminal and type `ssh youruser@your_server_ip`. |
| An email address | Used by Let's Encrypt to notify you if an HTTPS certificate is about to expire. |
| The codebase | Provided by the original team as a zip file. Includes a production zip and a development zip — more on this in Section 12. |

---

## 4. A Note on File Paths: ~/ vs /

Before running any commands, you need to understand the difference between paths that start with `~/` and paths that start with `/`. Getting this wrong will cause files to end up in the wrong place.

### `/` — The Root of the Entire System

`/` is the very top of the Linux file system. Everything on the computer lives somewhere under `/`. When you see a path like `/data/mysql` or `/var/log`, those are **absolute paths** — the same location no matter which user you are or where you are in the terminal.

```
/
├── data/          ← /data — a system-level directory
├── var/
│   └── log/       ← /var/log — system logs
├── etc/           ← system configuration
└── home/
    └── youruser/  ← your home folder lives here
```

### `~/` — Your Home Folder (a Shortcut)

`~/` is a shortcut that always means "my home folder." On Ubuntu, your home folder is at `/home/youruser` (where `youruser` is your actual username). So `~/docker` and `/home/youruser/docker` are exactly the same path — just written two different ways.

**Why this distinction matters:**

Files inside `~/` belong to you and can be created and edited without `sudo`. Files under system paths like `/data/` belong to the system and usually require `sudo` to create. This guide deliberately keeps Docker config files in `~/` (your space) and database data in `/data/` (the system's space, for cleaner separation and easier migration).

```
~/docker/               →  /home/youruser/docker/        (your Docker configs)
~/docker/traefik/       →  /home/youruser/docker/traefik/
~/docker/apps/pfa/      →  /home/youruser/docker/apps/pfa/

/data/mysql/            →  system-level, stores database files
/data/pfa_db/backups/   →  system-level, stores backup files
```

> **Rule of thumb:** Docker compose files and configs go in `~/docker/`. Database files and application data go in `/data/`. Keep them separate.

---

## 5. Preparing Your Server

Log into your server and run these commands. Lines starting with `#` are comments — you do not type those.

```bash
# Update the package list so your server knows about the latest software
sudo apt update

# Install available upgrades
sudo apt upgrade -y

# Install tools you will need throughout this setup
sudo apt install -y curl git rsync ufw unzip
```

### Setting Up a Firewall (ufw)

A firewall controls which network connections are allowed into your server. We will allow only what the application needs.

```bash
# Allow SSH first — this is critical, do this BEFORE enabling the firewall
sudo ufw allow ssh

# Allow HTTP (port 80) — Traefik needs this to verify your domain for HTTPS certificates
sudo ufw allow 80/tcp

# Allow HTTPS (port 443) — all real user traffic uses this
sudo ufw allow 443/tcp

# Turn the firewall on
sudo ufw enable

# Verify it looks right
sudo ufw status
```

> ⚠️ **Critical:** Run `sudo ufw allow ssh` BEFORE running `sudo ufw enable`. If you enable the firewall without allowing SSH first, you will be locked out of the server immediately.

---

## 6. Setting Up Your Domain Name

Your domain name needs to be pointed at your server's public IP address. This is done through **DNS A records** — think of it like updating the internet's phone book to say "this domain lives at this IP address."

**Steps:**

1. Find your server's **public IP address**. On a VPS it is shown in your provider's dashboard. On a self-hosted machine, run this from the server:
   ```bash
   curl ifconfig.me
   ```

2. Log into the domain registrar (the original team will tell you which one — common options are Namecheap, Cloudflare, GoDaddy, or Google Domains).

3. Find the **DNS settings** or **DNS management** section.

4. Create or update these records (replace `YOUR_SERVER_IP` with your actual IP):

   | Type | Name | Value | TTL |
   |------|------|-------|-----|
   | A | `@` | `YOUR_SERVER_IP` | 300 |
   | A | `www` | `YOUR_SERVER_IP` | 300 |
   | A | `api` | `YOUR_SERVER_IP` | 300 |

   - `@` — the root domain (`adoptpetsforall.com`)
   - `www` — `www.adoptpetsforall.com`
   - `api` — `api.adoptpetsforall.com` (the backend API)

5. DNS changes take anywhere from a few minutes to 48 hours to propagate. Verify it with:
   ```bash
   nslookup adoptpetsforall.com
   # Should return your server's IP address
   ```

---

## 7. Opening Ports on Your Network (Port Forwarding)

> **Skip this section if you are on a VPS.** VPS machines are directly on the internet — port forwarding only applies when your server is behind a home or office router.

If your server is a physical machine on a local network, your router sits between the internet and your server and blocks incoming connections by default. You need to create rules that forward traffic on ports 80 and 443 to your server.

**Steps:**

1. Open your router's admin page (usually `192.168.1.1` or `192.168.0.1` in a browser).

2. Log in (check the sticker on the router for the default password).

3. Find a section called **Port Forwarding**, **NAT**, or **Virtual Servers**.

4. Create two rules:

   | Rule Name | External Port | Internal IP | Internal Port | Protocol |
   |-----------|--------------|-------------|---------------|----------|
   | PFA HTTP | 80 | `<your server's local IP>` | 80 | TCP |
   | PFA HTTPS | 443 | `<your server's local IP>` | 443 | TCP |

   Find your server's local IP (e.g. `192.168.1.42`) by running on the server:
   ```bash
   ip addr show | grep "inet " | grep -v 127.0.0.1
   ```

5. Save the rules.

> **Tip:** To keep the local IP from changing on you, set up a **DHCP Reservation** (sometimes called "Static IP Assignment") in your router — it lets you permanently assign a specific local IP to a specific device by its MAC address. Most home routers support this.

---

## 8. Installing Docker and Docker Compose

```bash
# Install Docker using the official script from docker.com
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to the docker group so you can run docker commands without sudo
sudo usermod -aG docker $USER

# IMPORTANT: Log out and log back in for the group change to take effect.
# Then verify:
docker --version
docker compose version
```

Both commands should print version numbers. If you see errors, check [https://docs.docker.com/engine/install/ubuntu/](https://docs.docker.com/engine/install/ubuntu/).

---

## 9. Setting Up Tailscale (Remote Access)

Tailscale creates a private, encrypted network between your devices. Once configured, you can SSH into your server from anywhere — home, campus, coffee shop — without exposing SSH to the public internet. Your laptop and server both join the same private network (called a "tailnet") and can talk to each other securely regardless of where they physically are.

### Why Tailscale?

- You do not need to expose SSH to the entire internet, which significantly reduces your attack surface.
- Access works from anywhere without complicated VPN setup.
- Tailscale handles all networking and encryption automatically.
- It is free for personal and small team use.

### Step 1: Create a Tailscale Account

Sign up at [https://tailscale.com](https://tailscale.com). You can use a Google, GitHub, or Microsoft account.

### Step 2: Install Tailscale on the Server

```bash
# Download and run the official install script
curl -fsSL https://tailscale.com/install.sh | sh

# Connect the server to your Tailscale account
# This will print a URL — open it in your browser and log in
sudo tailscale up

# Verify it connected
tailscale status
```

Your server should now appear in your Tailscale dashboard with a `100.x.x.x` IP address.

### Step 3: Install Tailscale on Your Laptop

Download the client from [https://tailscale.com/download](https://tailscale.com/download) and sign in with the same account. Your laptop will now appear on the same tailnet as your server.

### Step 4: SSH via Tailscale

```bash
# Find your server's Tailscale hostname
tailscale status
# Look for a name like: yourserver.tail1234.ts.net

# SSH using the Tailscale hostname or address from your laptop
ssh youruser@yourserver.tail1234.ts.net
```

> You can use what Tailscale calls MagicDNS, this allows you to use the hostname of the machine you are connecting to in place of the tailscale address. e.g. `ssh youruser@serverhostname`

### Optional: Lock SSH Down to Tailscale Only

Once you have confirmed Tailscale SSH works reliably, you can remove the public SSH firewall rule so SSH is only accessible through the Tailscale network:

```bash
# Only do this AFTER you have confirmed Tailscale SSH works
sudo ufw delete allow ssh
```

> ⚠️ Test Tailscale SSH from an outside network before doing this. If you remove the rule and Tailscale is not working, you will lock yourself out.

More information: [https://tailscale.com/kb/1193/tailscale-ssh](https://tailscale.com/kb/1193/tailscale-ssh)

---

## 10. Setting Up the Traefik Reverse Proxy

Traefik is the reverse proxy — the single entry point that receives all incoming web traffic and routes each request to the correct container based on the domain name. It also automatically issues and renews free HTTPS certificates via Let's Encrypt.

### Why Traefik?

Without Traefik you could only serve one application on port 443. Traefik lets you run many services on the same server, each on their own domain or subdomain, all sharing ports 80 and 443. When you start a new container with the right configuration labels, Traefik picks it up automatically — no manual reconfiguration needed.

### Step 1: Create the Shared Docker Network

Traefik and the PFA containers need to share a network to communicate. Create it once:

```bash
docker network create proxy
```

If you run this again later it will say the network already exists — that is fine.

### Step 2: Create the Traefik Folder and Files

```bash
mkdir -p ~/docker/traefik

# Create the certificate storage file
touch ~/docker/traefik/acme.json

# Restrict permissions — Let's Encrypt REQUIRES this or it will refuse to start
chmod 600 ~/docker/traefik/acme.json
```

> **Why chmod 600?** This makes the file readable and writable only by its owner. Let's Encrypt enforces this as a security requirement — if the file has open permissions, Traefik will refuse to use it and your HTTPS certificates will not be issued.

### Step 3: Create the Traefik docker-compose.yml

```bash
nano ~/docker/traefik/compose.yml
```

Paste this content, replacing `your-email@example.com` with a real address:

```yaml
services:
  traefik:
    image: traefik:v2.11
    container_name: traefik
    restart: unless-stopped
    command:
      - --providers.docker=true
      - --providers.docker.exposedbydefault=false

      - --entrypoints.web.address=:80
      - --entrypoints.websecure.address=:443

      # Redirect http -> https
      - --entrypoints.web.http.redirections.entrypoint.to=websecure
      - --entrypoints.web.http.redirections.entrypoint.scheme=https

      # LetsEncrypt (HTTP challenge)
      - --certificatesresolvers.le.acme.email=your-email@example.com
      - --certificatesresolvers.le.acme.storage=/letsencrypt/acme.json
      - --certificatesresolvers.le.acme.httpchallenge=true
      - --certificatesresolvers.le.acme.httpchallenge.entrypoint=web

      # Dashboard (useful for debugging)
      - --api.dashboard=true
      - --log.level=DEBUG
      - --accesslog=true

    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ~/docker/traefik:/letsencrypt
    networks:
      - proxy

networks:
  proxy:
    external: true
```

Save and exit (`Ctrl+X`, then `Y`, then `Enter`).

**What these settings mean:**

- `--providers.docker=true` — Traefik watches Docker and configures itself automatically when containers start or stop.
- `--providers.docker.exposedbydefault=false` — Traefik ONLY routes traffic to containers that explicitly have a `traefik.enable=true` label. Everything else is invisible to Traefik by default.
- `--entrypoints.web.address=:80` — Listens on port 80 (HTTP).
- `--entrypoints.websecure.address=:443` — Listens on port 443 (HTTPS).
- `--entrypoints.web.http.redirections...` — Automatically redirects all HTTP traffic to HTTPS. Users on port 80 are instantly moved to port 443.
- `--certificatesresolvers.le.acme.*` — Configures automatic certificate generation via Let's Encrypt. The "HTTP challenge" works by having Let's Encrypt make a temporary request to your domain on port 80 to verify ownership — this is why port 80 must stay open even though all user traffic uses port 443.
- `/var/run/docker.sock:/var/run/docker.sock:ro` — Gives Traefik read-only access to Docker so it can watch for new containers. `:ro` means read-only.
- `~/docker/traefik:/letsencrypt` — Maps the folder containing `acme.json` into the container.

### Step 4: Start Traefik

```bash
cd ~/docker/traefik
docker compose up -d

# Verify it started
docker compose ps
docker compose logs
```

Traefik should show `Up` and the logs should not show any errors.

---

## 11. Getting the Codebase onto Your Server

You will receive two Zip files. One contains the development version of the application which each team member will use on their work machines to make updates. The other contains the production version of the application which will live on the server and receive updates as they get made. The differences between the two is minimal with the only 2 files containing any difference are .env and compose.override.yml. It is crutial that you use the production version on the server as it contains the configurations for Treafik and file structure that is specific to the production environment. The difference will be discussed in section 12.

### Transfer a Zip File with SCP

Use **SCP** (Secure Copy Protocol) to transfer the Zip file containing the production environment. SCP copies files over an SSH connection. Run this command on **your laptop**, not the server:

```bash
# Format:
# scp /path/to/local/file youruser@server_address:/path/on/the/server

# Example — copying pfa_prod.zip from your Downloads folder to your home folder on the server:
scp ~/Downloads/pfa_prod.zip youruser@your_server_ip:~/pfa_prod.zip

# If you are using Tailscale, you can use the Tailscale hostname instead of the IP:
scp ~/Downloads/pfa_prod.zip youruser@yourserver.tail1234.ts.net:~/pfa_prod.zip
```

Once the transfer completes, SSH into the server and extract it:

```bash
# Create the destination directory
mkdir -p ~/docker/apps/pink_fairy_armadillos

# Unzip into the destination folder
# -d tells unzip where to put the extracted files
unzip ~/pfa_prod.zip -d ~/docker/apps/pink_fairy_armadillos

# Remove the zip file now that it is extracted
rm ~/pfa_prod.zip

# Check the files are there
ls ~/docker/apps/pink_fairy_armadillos
```

> **Note:** Depending on how the zip was created, you might get an extra nested folder. If `ls` shows a single folder instead of the expected files, move the contents up:
> ```bash
> mv ~/docker/apps/pink_fairy_armadillos/SomeNestedFolder/* ~/docker/apps/pink_fairy_armadillos/
> rmdir ~/docker/apps/pink_fairy_armadillos/SomeNestedFolder
> ```

---

## 12. Understanding the Two compose.override.yml Files

This section is important — read it carefully before proceeding.

You received **two zip files** from the original team: one labeled **production** and one labeled **development**. Each one contains its own `compose.override.yml` file. These two files are different and serve completely different purposes.

| File | Who uses it | What it does |
|------|-------------|--------------|
| Production `compose.override.yml` | Your server | Adds Traefik routing labels, your real domain names, real server file paths, and production environment settings |
| Development `compose.override.yml` | Developer laptops | Local development settings — no Traefik, no real domain, configured to run entirely on a local machine |

### Why Are These Files Gitignored?

Both `compose.override.yml` files are listed in `.gitignore` and are intentionally never committed to the Git repository. There are two important reasons for this:

1. **Security and privacy:** The production file contains your domain name, server-specific directory paths, and configuration details. You do not want these committed to a shared repository where they could be seen by others or accidentally overwritten.

2. **Environment flexibility:** Every environment needs its own version of this file. A developer's laptop and your production server need completely different settings. If one version were committed, it would break every other environment.

### The CD Pipeline Will Never Overwrite This File

Look at the `cd.yml` workflow — inside the `rsync` deployment step you will see:

```
--exclude "compose.override.yml"
```

This means every time a deployment runs and syncs new code to your server, your `compose.override.yml` is completely skipped and left exactly as it is. You create it once manually in Section 14, and it stays there permanently regardless of how many deployments happen.

> **Never commit either `compose.override.yml` to Git. Never remove it from `.gitignore`.** The entire deployment system is designed around this file being excluded.

### The cd.yml Workflow File

The `cd.yml` file that defines the GitHub Actions deployment pipeline **is** included in the codebase — it lives at `.github/workflows/cd.yml` inside the production zip. You do not need to create it manually. Once you set up the GitHub Actions runner in Section 17, it will find and use this file automatically.

---

## 13. Setting Up the PFA Application

### Step 1: Create the Data Directories

The database needs a permanent home on your server's disk. Docker containers are temporary by design — if data were stored inside a container, it would be wiped every time the container was rebuilt or restarted. Instead, we store data in specific folders on the server and mount them into the container. This is called a **bind mount** — more on why this matters in Section 14.

```bash
# MySQL database files
sudo mkdir -p /data/mysql

# Database backup files (shared between the backend and database containers)
sudo mkdir -p /data/pfa_db/backups

# Give your user ownership so you do not need sudo to work with these
sudo chown -R $USER:$USER /data
```

### Step 2: Create the compose.override.yml

```bash
nano ~/docker/apps/pink_fairy_armadillos/compose.override.yml
```

See **Section 14** below for a detailed line-by-line explanation of everything that goes in this file.

---

## 14. Understanding the compose.override.yml File (Detailed)

### Bind Mounts vs. Docker-Managed Volumes

Before going through the file, you need to understand two ways Docker can store data outside a container — because the choice matters.

**Docker-managed volumes** are storage areas that Docker creates and manages internally, stored under `/var/lib/docker/volumes/` on the server. You just give them a name and Docker handles the rest. The downside is you do not control where they live (or rather it requires a lot of work to control where they live), they are harder to back up directly, and they live in a root-owned system directory that requires `sudo` to access. (This is ok for development but not worth the risk in production).

**Bind mounts** map a specific folder you choose on your server into the container. In a compose file they look like `/data/mysql:/var/lib/mysql` — the left side is the path on your server, the right side is where it appears inside the container. You have full control over where the data lives.

**For the production database, this config uses bind mounts, and you should keep it that way.** Here is why:

- Your database files are always at `/data/mysql` — you always know where to find them.
- Backing up the database manually is straightforward: just copy that folder (although you probably wont do it this way, the application has better methods).
- If you move to a new server, you know exactly what to transfer.
- Bind-mounted data is **never deleted by Docker** — not even by `docker compose down -v`. Docker-managed volumes can be accidentally deleted with that command. Since your database is the most important data on the server, using a bind mount means there is one fewer way to accidentally wipe it.

Now, here is the full file. Lines marked `# ← CHANGE THIS` must be updated for your server:

```yaml
services:

  # ─────────────────────────────────────────────
  # BACKEND SERVICE (Spring Boot Java API)
  # ─────────────────────────────────────────────
  backend:
    environment:
      # Tells Spring Boot to load the "prod" configuration profile,
      # which activates production-specific settings inside the codebase.
      # Leave this as-is.
      SPRING_PROFILES_ACTIVE: prod

      # Overrides the database connection URL for production.
      # "db" is Docker's internal name for the MySQL container — Docker
      # automatically resolves container names as hostnames on the same network.
      # ${MYSQL_DATABASE} is read from your .env file at runtime.
      # Leave this as-is.
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}?useSSL=false&serverTimezone=UTC

    networks:
      # "default" is the internal Docker network shared only by containers
      # in this compose project. The backend needs this to reach
      # the database (db) and Chrome (chrome) containers.
      - default
      # "proxy" is the external network Traefik is on.
      # The backend needs this so Traefik can forward traffic to it.
      - proxy

    labels:
      # Tell Traefik to pay attention to this container.
      - traefik.enable=true

      # Tell Traefik which Docker network to use when connecting to this container.
      # Must match the external network name. Leave as-is.
      - traefik.docker.network=proxy

      # ← CHANGE THIS: Replace with your actual API subdomain.
      # Traefik will route any request for this hostname to the backend container.
      - traefik.http.routers.pfa-api.rule=Host(`api.adoptpetsforall.com`)

      # Use the "websecure" entry point (port 443, HTTPS). Leave as-is.
      - traefik.http.routers.pfa-api.entrypoints=websecure

      # Enable TLS (HTTPS) for this router. Leave as-is.
      - traefik.http.routers.pfa-api.tls=true

      # Use the Let's Encrypt certificate resolver named "le" (defined in your
      # Traefik config). The name must match exactly. Leave as-is.
      - traefik.http.routers.pfa-api.tls.certresolver=le

      # The port the backend listens on inside the container.
      # Spring Boot defaults to 8080. Leave as-is unless the backend changed it.
      - traefik.http.services.pfa-api.loadbalancer.server.port=8080

    volumes:
      # Bind mount: /data/pfa_db/backups on your server → /backups inside the container.
      # The backend writes database backups to /backups, which physically land
      # at /data/pfa_db/backups on your server.
      #
      # ← CHANGE THIS if you used different paths in Step 13.
      - /data/pfa_db/backups:/backups

  # ─────────────────────────────────────────────
  # FRONTEND SERVICE (React/Nginx web app)
  # ─────────────────────────────────────────────
  frontend:
    networks:
      # The frontend only needs the proxy network.
      # It does NOT need to be on the internal "default" network because
      # the frontend never talks directly to the database —
      # all data goes through the backend API instead.
      - proxy

    labels:
      - traefik.enable=true

      # ← CHANGE THIS: Replace with your actual domain names.
      # Routes both the root domain and the www subdomain to the frontend.
      - traefik.http.routers.pfa-web.rule=Host(`adoptpetsforall.com`,`www.adoptpetsforall.com`)

      - traefik.http.routers.pfa-web.entrypoints=websecure
      - traefik.http.routers.pfa-web.tls=true
      - traefik.http.routers.pfa-web.tls.certresolver=le

      # The frontend Nginx container serves traffic on port 80 internally.
      # Leave as-is.
      - traefik.http.services.pfa-web.loadbalancer.server.port=80

  # ─────────────────────────────────────────────
  # DATABASE SERVICE (MySQL)
  # ─────────────────────────────────────────────
  db:
    networks:
      # The database is ONLY on the internal "default" network.
      # It is completely unreachable from the internet —
      # only the backend container can connect to it.
      # This is an important security boundary.
      - default

    volumes:
      # Bind mount: MySQL stores its data files at /data/mysql on your server.
      # This is a bind mount (not a Docker-managed volume) so:
      # - You always know where your data is
      # - It is easy to back up
      # - Docker can never accidentally delete it
      #
      # Without this volume, your entire database is wiped every time
      # the container is rebuilt or restarted.
      #
      # ← CHANGE THIS if you used different paths in Step 13.
      - /data/mysql:/var/lib/mysql

      # The database also gets access to the backups folder.
      # ← CHANGE THIS if you used a different path in Step 13.
      - /data/pfa_db/backups:/backups

# ─────────────────────────────────────────────
# NETWORKS
# ─────────────────────────────────────────────
networks:
  # Declares the "proxy" network as external — Docker Compose will not
  # try to create it, but will connect to the one you created manually
  # with "docker network create proxy".
  proxy:
    external: true
```

Save and exit.

---

## 15. Configuring Your .env File

The `.env` file holds all the secrets the application needs — database credentials, API URLs, etc. It is gitignored and never copied by the CD pipeline, so you create it once manually on the server and it stays there permanently.

```bash
nano ~/docker/apps/pink_fairy_armadillos/.env
```

```env
# ── Database Configuration ──────────────────────────────────────────────────

# The name of the MySQL database. Probably can be anything (letters and underscores only).
# We went with PFA_DB and you might as well do the same
MYSQL_DATABASE=PFA_DB

# The MySQL username the application connects with.
# Because this database runs inside Docker and is never exposed to the internet,
# this is a made-up credential for an internal system — it can be anything you want.
MYSQL_USER=pfa_user

# Password for the above user.
# Make this long and random. You do NOT need to be able to type it from memory —
# the application reads it directly from this file. Nobody ever types it manually.
# Generate one: openssl rand -base64 32
MYSQL_PASSWORD=paste_a_strong_random_password_here

# The MySQL root password.
# Same rules — long, random, and you do not need to remember it.
# Use a DIFFERENT value from MYSQL_PASSWORD above.
MYSQL_ROOT_PASSWORD=paste_a_different_strong_random_password_here

# ── Frontend Configuration ───────────────────────────────────────────────────

# The full public URL of the backend API, as the user's browser sees it.
# ← CHANGE THIS to your actual API subdomain if you do not transfer ours
VITE_API_BASE_URL=https://api.adoptpetsforall.com
```

Save and exit. Lock down the file:

```bash
chmod 600 ~/docker/apps/pink_fairy_armadillos/.env
```

**A note on the database credentials:** Because PFA runs entirely inside Docker, the database is completely isolated from the internet — only containers within the same internal Docker network can reach it. This means these credentials are for a fully private internal system. You are essentially making up a username and password for something that only your own application will ever connect to. You should still use strong random passwords (a compromised server is still a concern), but you absolutely do not need credentials you can type from memory. Use `openssl rand -base64 32` to generate them, paste the output into this file, and let the application handle the rest.

Generate strong passwords with:
```bash
openssl rand -base64 32
```

---

## 16. First Launch — Starting the Application

Everything is in place. Start everything up:

```bash
cd ~/docker/apps/pink_fairy_armadillos

# Docker Compose automatically finds and merges compose.yml and compose.override.yml.
# You do not need to specify both files — it happens automatically.
# --build tells Docker to build the backend and frontend images from source code.
docker compose up -d --build
```

The first build takes several minutes — Docker downloads base images and compiles the Java backend. Be patient.

**Monitor what is happening:**

```bash
# Stream live logs from all containers.
# The -f flag means "follow" — new log lines appear in real time.
# Press Ctrl+C to stop watching. This stops the log stream only —
# it does NOT stop or affect the running containers.
docker compose logs -f

# Watch just the backend
docker compose logs -f backend

# See the current state of all containers
docker compose ps
```

**What healthy output looks like:**

- `db` shows `healthy` in the status (it runs an internal health check).
- `backend` logs `Started PfaApplication in X.XXX seconds`.
- `frontend` shows `Up`.
- `chrome` shows `Up`.

If the backend keeps restarting, check its logs for the error — the most common cause is an incorrect value in `.env`. There are plenty of other reasons it can fail to start — failed flyway migrations, incorrect .env values, the database didnt start, Spring context can't load, etc... Just check the logs and they should tell you whats wrong.

---

## 17. Setting Up the GitHub Actions CD Pipeline

The CD pipeline automatically deploys new code to your server every time a pull request is merged into `main`. The `cd.yml` workflow file that defines this is already in the codebase at `.github/workflows/cd.yml` — you do not create it, it is already there.

The pipeline runs on a **self-hosted runner** — a small background service you install on your server. When GitHub triggers a deployment, it sends the job to your runner, which executes the steps locally on your server.

### Step 1: Register a Runner on GitHub

1. Go to your GitHub repository → **Settings** → **Actions** → **Runners**.
2. Click **"New self-hosted runner"**.
3. Select **Linux** and **x64** (use arm64 if your server has an ARM processor).
4. GitHub will display a series of commands to run on your server. **Use exactly the commands GitHub shows you** — they include a unique token for your repository. The commands below are examples only.

```bash
# Example structure only — use the exact commands from GitHub
mkdir ~/actions-runner && cd ~/actions-runner
curl -o actions-runner-linux-x64.tar.gz -L https://github.com/actions/runner/releases/...
tar xzf ./actions-runner-linux-x64.tar.gz
./config.sh --url https://github.com/YourOrg/YourRepo --token YOUR_UNIQUE_TOKEN
```

When prompted:
- **Runner name:** Press Enter to use the default.
- **Labels:** Press Enter to use the default.
- **Work folder:** Press Enter to use the default `_work`.

### Step 2: Install the Runner as a System Service

```bash
sudo ./svc.sh install
sudo ./svc.sh start
sudo ./svc.sh status
```

On GitHub → Settings → Actions → Runners, your runner should now appear with a green dot showing **"Idle"**.

### Step 3: Give the Runner Docker Access

```bash
# Find the user the runner service runs as (look for "User=" in the output)
sudo ./svc.sh status

# Add that user to the docker group
sudo usermod -aG docker RUNNER_USER

# Restart the service to apply the group change
sudo ./svc.sh stop
sudo ./svc.sh start
```

### Step 4: Test the Pipeline

Merge a small change into `main` (or trigger manually: GitHub → Actions → "Deployment" → "Run workflow"). Watch each step in the Actions tab. If a step fails, click it to see the full error output.

### What the CD Workflow Does (cd.yml explained)

```yaml
on:
  push:
    branches: ["main"]
# Fires only when code is pushed to main — not on feature branches or open PRs.

concurrency:
  group: demo-2-deploy
  cancel-in-progress: true
# If two deployments are triggered simultaneously, cancel the older one.
# Prevents two deploys from conflicting with each other.
```

The `rsync` step copies new code to `~/docker/apps/pink_fairy_armadillos` but deliberately skips:

```
--exclude ".env"                    ← Your secrets are never touched
--exclude "compose.override.yml"    ← Your server config is never touched
--exclude "target/"                 ← Compiled Java output (Docker recompiles from source)
--exclude ".git/"                   ← Git metadata not needed at runtime
--exclude ".github/"                ← Workflow files not needed at runtime
```

Then it runs `docker compose up -d --build`, which rebuilds only changed containers and restarts them automatically.

---

## 18. Branch Protection — A Brief Note

Developers should never push code directly to the `main` branch. All changes must go through a **Pull Request (PR)**: a developer creates a new branch, makes their changes there, and opens a PR to merge those changes into `main`. This keeps `main` stable and ensures all code is reviewed before it reaches production (and before the CD pipeline fires).

**Branch protection rules** in GitHub enforce this automatically — you can configure them so that direct pushes to `main` are blocked entirely. This is covered in detail in a separate document. For now: if anyone asks "can I just push straight to main?", the answer is no.

---

## 19. Verifying Everything Works

Work through this checklist after your first deployment:

- [ ] **DNS resolves correctly:**
  ```bash
  nslookup adoptpetsforall.com
  # Should return your server's IP
  ```

- [ ] **HTTP redirects to HTTPS:**
  Visit `http://adoptpetsforall.com` in a browser — it should automatically redirect to `https://`.

- [ ] **HTTPS certificate is valid:**
  Look for the padlock icon in the browser address bar. Click it and verify the certificate is issued by "Let's Encrypt".

- [ ] **Frontend loads:**
  Visit `https://adoptpetsforall.com` — the PFA website should appear.

- [ ] **Backend API responds:**
  Visit `https://api.adoptpetsforall.com` — you should see a JSON response, not an error page.

- [ ] **Tailscale works from outside the network:**
  Disconnect from your local network (use your phone as a hotspot if needed) and confirm you can SSH to the server via its Tailscale hostname.

- [ ] **Database persists across restarts:**
  Create a record in the application (creating a new account is a good way to test this), run `docker compose restart`, and confirm the record is still there.

- [ ] **CD pipeline works:**
  Merge a trivial change into `main` and watch GitHub Actions deploy it automatically.

---

## 20. Maintenance and Troubleshooting

### Viewing Logs

```bash
cd ~/docker/apps/pink_fairy_armadillos

# All containers, live stream
# -f means "follow" — output streams in real time.
# Ctrl+C exits the stream but does NOT stop the containers.
docker compose logs -f

# Just the backend, live
docker compose logs -f backend

# Last 100 lines of the frontend, not live
docker compose logs --tail=100 frontend
```

### Restarting Containers

```bash
docker compose restart          # restart everything
docker compose restart backend  # restart just the backend
```

### Stopping and Starting

```bash
# Stop all containers — your data is completely safe, nothing is deleted
docker compose down

# Start everything again
docker compose up -d
```

> ⚠️ **Do not use `docker compose down -v` unless you know exactly what it does.**
>
> The `-v` flag tells Docker to delete **Docker-managed volumes** — storage that Docker created and manages internally. It does NOT affect bind mounts (the specific server folders like `/data/mysql` that you mapped in manually). Because this stack uses bind mounts for the database, your actual MySQL data would survive `docker compose down -v`. However, any Docker-managed volumes in the stack would be permanently deleted. It is best practice to never use `-v` unless you are intentionally resetting something and understand what will be lost.

### Manual Deployment (if GitHub Actions is unavailable)

```bash
cd ~/docker/apps/pink_fairy_armadillos
git pull origin main
docker compose up -d --build
```

### Checking Disk Space

Docker images and logs accumulate over time:

```bash
df -h
docker system df
```

Clean up unused images and build cache (safe — only removes things not currently in use):

```bash
docker system prune -f
```

### Common Problems and Fixes

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Backend keeps restarting | Honestly this can really be anything. Your best bet is to check the logs and paste them into claude or something until you get more familiar with the application. | `docker compose logs backend` to see the error |
| HTTPS certificate not issued | DNS not propagated or port 80 blocked | Wait for DNS; verify port 80 is open and forwarded |
| "502 Bad Gateway" from Traefik | Backend crashed or still starting | Check backend logs; give it a minute |
| Database empty after restart | Bind mount path wrong | Verify `compose.override.yml` volume paths match what you created in Step 13 |
| GitHub Actions runner offline | Server rebooted, service stopped | `cd ~/actions-runner && sudo ./svc.sh start` |
| Can't reach the site at all | Port forwarding or DNS misconfigured | Revisit Sections 6 and 7 |
| Can't SSH remotely | Tailscale disconnected or firewall issue | Check `tailscale status` on the server; try direct IP first |

---

## 21. Helpful References

| Topic | URL |
|-------|-----|
| Ubuntu Server install guide | https://ubuntu.com/tutorials/install-ubuntu-server |
| Docker getting started | https://docs.docker.com/get-started/ |
| Docker Compose reference | https://docs.docker.com/compose/ |
| Docker bind mounts vs volumes | https://docs.docker.com/storage/ |
| Traefik documentation | https://doc.traefik.io/traefik/ |
| Traefik Docker provider | https://doc.traefik.io/traefik/providers/docker/ |
| Let's Encrypt (how it works) | https://letsencrypt.org/how-it-works/ |
| Tailscale getting started | https://tailscale.com/kb/1017/install |
| Tailscale SSH | https://tailscale.com/kb/1193/tailscale-ssh |
| GitHub Actions self-hosted runners | https://docs.github.com/en/actions/hosting-your-own-runners |
| GitHub branch protection rules | https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches |
| UFW firewall guide | https://help.ubuntu.com/community/UFW |
| How DNS works (beginner-friendly) | https://howdns.works/ |
| SCP file transfer guide | https://linuxize.com/post/how-to-use-scp-command-to-securely-transfer-files/ |

---

## 22. Quick Reference: File Locations

| File / Directory | Purpose |
|-----------------|---------|
| `~/docker/traefik/docker-compose.yml` | Traefik configuration |
| `~/docker/traefik/acme.json` | HTTPS certificate storage (must be chmod 600) |
| `~/docker/apps/pink_fairy_armadillos/` | PFA application code |
| `~/docker/apps/pink_fairy_armadillos/.env` | Secrets and credentials — never share or commit |
| `~/docker/apps/pink_fairy_armadillos/compose.yml` | Base Docker Compose config (from the codebase) |
| `~/docker/apps/pink_fairy_armadillos/compose.override.yml` | Your server-specific config — gitignored, never overwritten by CD |
| `~/docker/apps/pink_fairy_armadillos/.github/workflows/cd.yml` | GitHub Actions CD pipeline (included in codebase) |
| `/data/mysql/` | MySQL database files (bind mount — lives on your server) |
| `/data/pfa_db/backups/` | Database backup files (bind mount) |
| `~/actions-runner/` | GitHub Actions self-hosted runner |

---

*Documentation prepared by the original PFA development team. For questions about the application code, contact the original team. For infrastructure questions, refer to the linked documentation sources above or contact Austin Meredith (adm21c@acu.edu).*
