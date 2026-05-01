# Pets for All (PFA) — GitHub Repository Setup Guide

This document walks through setting up the GitHub repository the right way — branch protection, CI/CD pipeline configuration, secrets, and team permissions. It assumes you have basic Git knowledge (clone, commit, push, pull) but goes into detail on the parts that are easy to get wrong or that require deliberate configuration.

Please read this whole document before starting. There are things that have to happen in a very specific order and this process is crutial for the development environment and production environment to work together as intended. If you have any questions please reach out to Austin Meredith adm21c@acu.edu.

---

## Table of Contents

1. [Overview: What This Guide Sets Up](#1-overview-what-this-guide-sets-up)
2. [Windows Users: WSL2 and the Dev Zip](#2-windows-users-wsl2-and-the-dev-zip)
3. [Repository Creation and Initial Push](#3-repository-creation-and-initial-push)
4. [Understanding the Two Workflows](#4-understanding-the-two-workflows)
   - [ci.yml — Integration (runs on every PR)](#31-ciyml--integration-runs-on-every-pr)
   - [ci.yml — Integration (runs on every PR)](#41-ciyml--integration-runs-on-every-pr)
   - [cd.yml — Deployment (runs on merge to main)](#42-cdyml--deployment-runs-on-merge-to-main)
5. [Placing the Workflow Files](#5-placing-the-workflow-files)
6. [Configuring GitHub Actions Secrets](#6-configuring-github-actions-secrets)
7. [The Self-Hosted Runner](#7-the-self-hosted-runner)
8. [Configuring Branch Protection Rules](#8-configuring-branch-protection-rules)
9. [Managing Team Members and Permissions](#9-managing-team-members-and-permissions)
10. [Understanding the Full PR Lifecycle](#10-understanding-the-full-pr-lifecycle)
11. [Repository Settings Worth Knowing](#11-repository-settings-worth-knowing)
12. [Common Problems and Fixes](#12-common-problems-and-fixes)
13. [Helpful References](#13-helpful-references)

---

## 1. Overview: What This Guide Sets Up

By the end of this guide, your repository will:

- Have workflow files in the correct location so GitHub Actions can find them.
- Run automated tests against every pull request before anyone can merge.
- Automatically deploy to the production server when code is merged into `main`.
- Have branch protection rules that prevent anyone from pushing directly to `main`, require the CI tests to pass before a merge is allowed, and require at least one reviewer to approve a PR.
- Have team members added with the correct permission levels.

Here is the big picture of how code flows from a developer's laptop to production:

```
Developer's laptop
      │
      │  git push origin feature/my-feature
      ▼
GitHub (feature branch)
      │
      │  Developer opens Pull Request → main
      ▼
GitHub Actions CI (ci.yml)
  └── Runs on GitHub's servers (ubuntu-latest)
  └── Sets up Java 21
  └── Runs ./mvnw test
  └── ✔ Pass → PR can be reviewed and merged
      │
      │  Team lead approves and merges PR
      ▼
main branch updated
      │
      │  Push to main triggers cd.yml
      ▼
GitHub Actions CD (cd.yml)
  └── Runs on YOUR server (self-hosted runner)
  └── Syncs new code via rsync
  └── Rebuilds and restarts Docker containers
  └── ✔ Production is updated
```

---

## 2. Windows Users: WSL2 and the Dev Zip

> **Skip this section if you are on Linux or macOS.** This only applies to Windows developers.

Before doing anything else in this guide, Windows users need to make sure they are working inside **WSL2** (Windows Subsystem for Linux 2) and that the development codebase is placed inside the WSL2 file system — not in a Windows folder like `C:\Users\YourName\`. This is not optional. Git, the project scripts, and Docker all behave differently (and often incorrectly) when run against Windows-formatted paths, and the shell scripts in the `scripts/` folder will not work at all outside of a Linux environment.

**If you have not set up WSL2 and Docker Desktop yet, stop here and complete that setup first.** The Developer Environment Guide, Section 4.3 covers the full process — WSL2 installation, Docker Desktop configuration, and why you must clone inside the WSL2 file system. Complete that section, then come back here.

### Moving and Extracting the Dev Zip Inside WSL2

You received a development zip file from the original team. The steps below walk through getting it from your Windows Downloads folder into WSL2 and extracting it correctly.

**Step 1: Open a WSL2 terminal**

Open Windows Terminal and select an Ubuntu tab (or launch Ubuntu from the Start menu). All commands below are run here — not in PowerShell, not in Command Prompt.

**Step 2: Copy the zip from Windows into WSL2**

Your Windows file system is accessible inside WSL2 at `/mnt/c/`. So your Downloads folder is at `/mnt/c/Users/YourWindowsUsername/Downloads/`. Copy the zip into your WSL2 home folder:

```bash
# Replace YourWindowsUsername with your actual Windows username
# Replace pfa_dev.zip with the actual filename you received
cp /mnt/c/Users/YourWindowsUsername/Downloads/pfa_dev.zip ~/pfa_dev.zip
```

Verify it arrived:
```bash
ls ~/pfa_dev.zip
```

**Step 3: Create a destination folder and extract**

```bash
# Create the folder where the project will live
mkdir -p ~/PinkFairyArmadillos

# Extract the zip into that folder
# -d specifies the destination directory
unzip ~/pfa_dev.zip -d ~/PinkFairyArmadillos

# Remove the zip now that it is extracted
rm ~/pfa_dev.zip
```

**Step 4: Check for a nested folder**

Depending on how the zip was created, the contents may land inside an extra nested folder. Check:

```bash
ls ~/PinkFairyArmadillos
```

If you see a single folder instead of the expected project files (`compose.yml`, `backend/`, `frontend/`, `scripts/`, etc.), the zip had an outer wrapper. Move everything up one level:

```bash
# Replace "SomeNestedFolderName" with whatever name you see
mv ~/PinkFairyArmadillos/SomeNestedFolderName/* ~/PinkFairyArmadillos/
mv ~/PinkFairyArmadillos/SomeNestedFolderName/.* ~/PinkFairyArmadillos/ 2>/dev/null || true
rmdir ~/PinkFairyArmadillos/SomeNestedFolderName
```

Then verify the structure looks right:
```bash
ls ~/PinkFairyArmadillos
# Expected output: backend/  frontend/  scripts/  compose.yml  .env.example  etc.
```

**Step 5: Navigate into the project**

```bash
cd ~/PinkFairyArmadillos
```

From here, continue with the rest of this guide. When Section 3 refers to "the project folder," this is it.

> **Opening the project in VS Code:** From inside the WSL2 terminal, run `code .` to open the project in VS Code with the WSL extension. Do not open the folder through the Windows file explorer — VS Code will treat it as a Windows path and things will not behave correctly.

---

## 3. Repository Creation and Initial Push

### Creating the Repository on GitHub

1. Log into GitHub and click the **+** icon in the top right → **"New repository"**.
2. Give it a name (e.g. `PinkFairyArmadillos`).
3. Set visibility to **Public** as github actions is free for public repos.
4. **Do not** check "Add a README", "Add .gitignore", or "Choose a license." You already have the codebase — initializing with these files will create a conflict on your first push.
5. Click **"Create repository"**.

GitHub will show you a page with setup instructions. Keep this tab open.

### Pushing the Existing Codebase

**First move and unzip the Zip file containing the development environment. (Very inportant you use the development environment)**

From inside the project folder on your machine:

```bash
# Initialize a Git repository if one does not already exist
git init

# Add all files (Git will respect .gitignore automatically)
git add .

# Make the first commit
git commit -m "Initial commit"

# Rename the default branch to "main" if it was created as "master"
git branch -M main

# Connect your local repo to GitHub (copy the URL from the GitHub page)
git remote add origin https://github.com/YourOrg/PinkFairyArmadillos.git

# Push to GitHub
git push -u origin main
```

The `-u origin main` flag sets "origin/main" as the default upstream for your local `main` branch, so future `git push` and `git pull` commands do not need the full arguments.

### Verifying .gitignore is Working

Before anything else, verify that sensitive files are not being tracked. Run:

```bash
git status
```

You should NOT see `.env`, `compose.override.yml`, or any file in `target/` or `backups/` listed. If you do, they are missing from `.gitignore`. Add them before your first push:

```
# Add these lines to .gitignore if they are not already there
.env
compose.override.yml
compose.override.backup.yml
target/
backups/
```

If a file was already committed and you need to remove it from tracking without deleting it:
```bash
git rm --cached .env
git commit -m "Remove .env from tracking"
```

> **Critical:** Never commit `.env` or `compose.override.yml`. If credentials are ever committed, assume them compromised — rotate the passwords immediately and purge the file from Git history using `git filter-branch` or the BFG Repo Cleaner. This is not a quick fix.

---

## 4. Understanding the Two Workflows

GitHub Actions workflows are YAML files that define automated jobs. They live in `.github/workflows/` in your repository. GitHub detects them automatically — you do not register them anywhere, you just push the files and GitHub picks them up.

### 4.1 ci.yml — Integration (runs on every PR)

```yaml
name: Integration

on:
  pull_request:
    branches: [ "main" ]
```

**When it triggers:** Any time a pull request is opened, updated, or synchronized (new commits pushed) that targets `main`. It does NOT run on pushes to `main` directly — only PRs.

**What it runs on:** `ubuntu-latest` — a fresh GitHub-hosted virtual machine spun up just for this job. It is completely clean on every run, which is why we need to install Java and restore the Maven cache each time.

**What it does step by step:**

```yaml
- name: Checkout
  uses: actions/checkout@v4
```
Clones the repository at the PR's HEAD commit into the runner's working directory. `actions/checkout` is a pre-built action maintained by GitHub.

```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "21"
```
Installs Java 21 (Temurin distribution, which is the open-source Eclipse Adoptium build — the same one recommended in the developer guide). This is why you do not need Java pre-installed on the GitHub runner.

```yaml
- name: Cache Maven
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-maven-
```
This step caches the Maven dependency download folder (`~/.m2/repository`) between workflow runs. Without this, Maven would download all dependencies from scratch on every PR — which takes 2–5 minutes. With the cache, subsequent runs that use the same `pom.xml` restore the dependencies in seconds.

**How the cache key works:** The key is built from the OS name plus a hash of all `pom.xml` files. If `pom.xml` has not changed, the cache key matches and the dependencies are restored. If you add or change a dependency in `pom.xml`, the hash changes, the key does not match, Maven downloads fresh, and a new cache entry is saved. `restore-keys` is a fallback — if an exact match is not found, it uses any cache that starts with `ubuntu-latest-maven-`.

```yaml
- name: Build + test
  working-directory: backend
  run: ./mvnw -B test
```
Runs the Maven test suite from inside the `backend/` directory. `-B` means "batch mode" — suppresses Maven's interactive progress animations so the log output is clean and readable in GitHub's UI. `test` compiles the code and runs all tests. The tests use Testcontainers, which spins up a real MySQL container inside the runner automatically.

**The CI job is the gatekeeper.** If any test fails, the job fails, and (once branch protection is configured in Section 8) the PR cannot be merged until the tests pass.

---

### 4.2 cd.yml — Deployment (runs on merge to main)

```yaml
name: Deployment

on:
  push:
    branches: ["main"]
```

**When it triggers:** Any push to `main`. In practice this means every time a PR is merged, because merging a PR is a push to `main`. It does NOT trigger on PR creation or updates — only actual pushes to the branch.

**What it runs on:** `self-hosted` — the runner you installed on your production server. Unlike the CI job which runs on GitHub's infrastructure, the CD job runs directly on your server. This is what allows it to call `docker compose` and restart your containers.

> For setting up the self-hosted runner on your server, see the **Production Deployment Guide**, Section 17.

```yaml
concurrency:
  group: demo-2-deploy
  cancel-in-progress: true
```
**This is important.** If two PRs are merged in quick succession, two deployment jobs could trigger at almost the same time. Without concurrency control, they would both try to run `docker compose up` simultaneously, which can leave the application in a broken state. The `concurrency` block puts both runs in the same named group (`demo-2-deploy`) and cancels the older one if a newer one starts. The newest deployment always wins.

**Step by step:**

```yaml
- name: Checkout repo
  uses: actions/checkout@v4
  with:
    fetch-depth: 1
```
Clones the repository at the latest commit on `main`. `fetch-depth: 1` is a "shallow clone" — it only downloads the most recent commit, not the full Git history. This is faster and sufficient for deployment since you just need the current files.

```yaml
- name: Sync code to docker apps folder
  run: |
    APP_DIR="$HOME/docker/apps/pink_fairy_armadillos"
    mkdir -p "$APP_DIR"

    rsync -av --delete \
      --exclude ".git/" \
      --exclude ".github/" \
      --exclude "target/" \
      --exclude ".env" \
      --exclude "compose.override.yml" \
      --exclude "compose.override.backup.yml" \
      ./ "$APP_DIR/"
```
Uses `rsync` to copy the freshly checked-out code into the stable deploy directory (`~/docker/apps/pink_fairy_armadillos`). Key flags:
- `-a` — archive mode: preserves permissions, timestamps, and copies recursively.
- `-v` — verbose: prints each file as it is synced (useful in logs).
- `--delete` — removes files from the destination that no longer exist in the source. Without this, deleted files would accumulate in the deploy directory forever.
- `--exclude` — these paths are intentionally skipped. Your `.env` and `compose.override.yml` on the server are never touched, even if the checked-out repo had those files.

The reason for syncing to a separate stable directory (rather than just working in the runner's checkout directory, which is temporary) is stability — the deploy directory persists between runs and is where Docker Compose looks for the `compose.override.yml` and `.env` that live there permanently.

```yaml
- name: Build and restart containers
  run: |
    cd "$HOME/docker/apps/pink_fairy_armadillos"
    docker compose up -d --build
```
Rebuilds any Docker images that changed (backend and/or frontend) and restarts the containers that are affected. Containers whose images did not change are left running untouched — only changed services cycle.

```yaml
- name: Show running containers
  run: |
    cd "$HOME/docker/apps/pink_fairy_armadillos"
    docker compose ps
    docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' | sed -n '1p;/pfa-db/p;/pinkfairyarmadillos/p'
```
Prints the status of the PFA containers to the workflow log so you can see at a glance that everything came up healthy after the deploy. This is purely informational — it does not affect whether the job passes or fails.

---

## 5. Placing the Workflow Files

GitHub Actions requires workflow files to be at a specific path:

```
<repository root>
└── .github/
    └── workflows/
        ├── ci.yml
        └── cd.yml
```

If these files are in any other location, GitHub will not detect them. Create the directory if it does not exist:

```bash
mkdir -p .github/workflows
```

Copy `ci.yml` and `cd.yml` into that folder, then commit and push:

```bash
git add .github/workflows/ci.yml .github/workflows/cd.yml
git commit -m "Add CI and CD GitHub Actions workflows"
git push origin main
```

Go to your repository on GitHub → **Actions** tab. You should see "Integration" and "Deployment" listed under "All workflows." The CD workflow will have already triggered once from this push — that is expected.

> **If the Actions tab shows nothing:** Wait 30 seconds and refresh. If it still shows nothing, double-check the file paths. The most common mistake is `.github/workflow/` (no `s`) or placing the files in a subdirectory.

---

## 6. Configuring GitHub Actions Secrets (Not required but you may need this in the future)

The CD workflow runs on your self-hosted runner, which already has the `.env` file on disk — so it does not need secrets passed through GitHub for database credentials. However, if you ever need to pass sensitive values into a workflow (for example, a deployment notification token, an API key for an external service, or a future workflow that runs on GitHub's infrastructure), GitHub Secrets is the correct way to do it.

**Never hardcode secrets in workflow YAML files.** Workflow files are committed to the repository. Even in a private repository, credentials in YAML files are a bad practice.

### Adding a Secret

1. Repository → **Settings** → **Secrets and variables** → **Actions**.
2. Click **"New repository secret"**.
3. Give it a name (all caps with underscores, e.g. `SOME_API_KEY`).
4. Paste the value.
5. Click **"Add secret"**.

### Using a Secret in a Workflow

```yaml
- name: Some step that needs a secret
  run: echo "Doing something with the key"
  env:
    MY_KEY: ${{ secrets.SOME_API_KEY }}
```

Secrets are masked in logs — GitHub replaces any accidental printing of the value with `***`.

### Environment Secrets vs. Repository Secrets

GitHub also supports **Environments** (Settings → Environments), which let you attach secrets to a named environment (e.g. `production`) and add protection rules — such as requiring a manual approval before a deployment to that environment proceeds. This is worth considering as the project grows. For now, repository-level secrets are sufficient.

More information: [https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions)

---

## 7. The Self-Hosted Runner

The CD workflow runs on `self-hosted` — a runner installed on your production server. This document does not cover the setup steps in detail; see the **Production Deployment Guide, Section 17** for the full walkthrough covering installation, registering the runner with your repository, running it as a system service, and giving it Docker access.

A few things worth knowing here that complement that guide:

### Runner Labels

When you registered the runner, GitHub assigned it the default label `self-hosted`. The `cd.yml` uses `runs-on: self-hosted`, which matches that label. If you ever register a second runner (e.g. for a staging environment), you can give runners custom labels and target them specifically with `runs-on: [self-hosted, production]`.

### Checking Runner Status

Repository → **Settings** → **Actions** → **Runners**. Your runner should show a green dot and "Idle" when it is ready. "Offline" means the runner service on the server is not running — SSH into the server and restart it:

```bash
cd ~/actions-runner
sudo ./svc.sh start
```

---

## 8. Configuring Branch Protection Rules

Branch protection rules are the enforcement mechanism for your team's workflow. Without them, anyone with write access can push directly to `main`, bypass code review, and trigger a production deployment without any tests running. Branch protection prevents all of that.

### Setting Up Protection for Main

1. Repository → **Settings** → **Branches**.
2. Under "Branch protection rules," click **"Add branch ruleset"** (GitHub's newer interface) or **"Add rule"** (classic interface). The steps below use the classic interface — if GitHub shows you "Rulesets," the options are the same but organized differently.
3. In the "Branch name pattern" field, type `main`.

Now configure the following options:

---

#### ✅ Require a pull request before merging

Enable this. This is the foundational rule — it means no one can push directly to `main`. All changes must come through a pull request.

**Sub-option — Required number of approvals:** Set this to `1`. Before a PR can be merged, at least one other team member must review it and click "Approve." This ensures a second set of eyes sees every change before it reaches production.

**Sub-option — Dismiss stale pull request approvals when new commits are pushed:** Enable this. If a PR is approved and then the author pushes more commits, the approval is dismissed and another review is required. This prevents the pattern of getting approval, then sneaking in changes afterward.

**Sub-option — Require review from Code Owners:** Leave this disabled for now unless you set up a `CODEOWNERS` file (covered at the end of this section).

---

#### ✅ Require status checks to pass before merging

Enable this. This is how you enforce that CI tests must pass before a PR can be merged.

After enabling, you need to specify which status checks are required. Click "Add checks" and search for the name of the CI job. The name to search for is the job ID from `ci.yml`:

```yaml
jobs:
  build-and-test:   ← this is the job ID
```

So search for `build-and-test` and add it. GitHub will only show checks that have previously run on this repository, so you may need to open a test PR first (even a trivial one) to get the CI workflow to run at least once before this option becomes searchable.

**Sub-option — Require branches to be up to date before merging:** Enable this. It means a PR must be based on the latest `main` before it can be merged. Without this, two developers could both open PRs against an older `main`, both get approved and pass tests, and then the second one to merge might introduce conflicts or break tests that the first merge revealed. Requiring up-to-date branches prevents this — the second developer has to pull `main` and rebase or merge before they can land their changes.

---

#### ✅ Require conversation resolution before merging

Enable this. If a reviewer leaves a comment on a PR (not just an approval — an actual inline comment or review comment), the author must mark it as "Resolved" before the PR can merge. This prevents PRs from being merged with unaddressed feedback sitting on them.

---

#### ❌ Require signed commits

Leave this disabled unless your team is specifically required to use GPG-signed commits. It adds complexity for limited benefit at this stage.

---

#### ✅ Do not allow bypassing the above settings

Enable this. Without it, repository administrators (people with Admin role) can bypass branch protection. Enabling this applies the rules to everyone — including the team lead or whoever set up the repository.

---

#### ✅ Restrict who can push to matching branches (Optional but recommended)

This limits which users or teams can merge PRs into `main`, even if they have write access. For a small team, this can be set to only allow team leads or senior members to do the final merge. For a fully collaborative team, leave it open and rely on the required review approval instead.

---

Click **"Create"** to save the rule. To verify it is working, try pushing directly to `main`:

```bash
git checkout main
echo "test" >> README.md
git add README.md
git commit -m "test direct push"
git push origin main
```

GitHub should reject it:
```
remote: error: GH006: Protected branch update failed for refs/heads/main.
remote: error: Required status check "build-and-test" is expected.
```

That rejection is the correct behavior — revert the test commit locally with `git reset HEAD~1`.

### The CODEOWNERS File (Optional)

A `CODEOWNERS` file lets you specify that certain files or directories require review from specific people. For example, you could require that any change to the backend's security configuration must be reviewed by the team lead:

```
# .github/CODEOWNERS

# Any change to the security config requires review from the team lead
backend/src/main/java/com/pfa/security/   @team-lead-github-username
```

Place this file at `.github/CODEOWNERS` and commit it. More information: [https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners)

---

## 9. Managing Team Members and Permissions

### Adding Collaborators (Personal Repositories)

If the repository is under a personal GitHub account:

Repository → **Settings** → **Collaborators** → **"Add people"**.

Search by GitHub username or email. Collaborators on a personal repository can be given one of these roles:

| Role | What they can do |
|------|-----------------|
| Read | View code and open issues. Cannot push. |
| Triage | Read + manage issues and PRs. Cannot push. |
| Write | Read + push to non-protected branches, open PRs. Cannot change settings. |
| Maintain | Write + manage some repository settings. Cannot change security settings. |
| Admin | Full access including settings, branch protection, and secrets. |

**For most developers:** Give **Write** access. They can push feature branches and open PRs, but branch protection prevents them from pushing to `main`.

**For team leads:** Give **Admin** access so they can manage settings and merge PRs when policy allows.

### Adding Teams (Organization Repositories)

If the repository is under a GitHub Organization (which is recommended for team projects):

1. Create the organization if you have not already: GitHub profile → **"Your organizations"** → **"New organization"**.
2. Invite team members to the organization.
3. Create a Team inside the organization (Organization → Teams → New Team) and add members to it.
4. Grant the team access to the repository with the appropriate role.

Organizations give you better access control, team-level permissions, and audit logs. More information: [https://docs.github.com/en/organizations](https://docs.github.com/en/organizations)

---

## 10. Understanding the Full PR Lifecycle

This is the workflow every team member should follow for every change, no matter how small. The branch protection rules you just configured enforce most of this automatically.

### Step 1: Start from an Updated Main

```bash
git checkout main
git pull origin main
```

Always start a new feature from the latest `main`. Never branch off an old commit.

### Step 2: Create a Feature Branch

```bash
# Use a descriptive name — be specific
git checkout -b feature/add-pet-filter-by-species

# Or for a bug fix:
git checkout -b fix/login-token-expiration

# Or for a chore:
git checkout -b chore/update-flyway-migration-v7
```

**Branch naming convention:** Keep it consistent across the team. A common pattern is `type/short-description`:
- `feature/` — new functionality
- `fix/` — bug fixes
- `chore/` — maintenance, dependency updates, refactoring
- `docs/` — documentation only

Consistent names make the branch list readable and help reviewers understand the purpose of a PR before they open it.

### Step 3: Make Changes and Commit Incrementally

Make your changes. Commit frequently — small, focused commits are easier to review and easier to revert if something goes wrong.

```bash
git add .
git commit -m "Add species filter to pet search endpoint"
```

**Commit message style:** Be specific. `"Fix bug"` is useless six months later. `"Fix null pointer in PetController when species param is missing"` tells the story. A common convention is to write commit messages in the imperative mood: "Add X", "Fix Y", "Remove Z" — as if completing the sentence "This commit will..."

### Step 4: Push to GitHub

```bash
git push origin feature/add-pet-filter-by-species
```

On subsequent pushes to the same branch, you can just use `git push`.

### Step 5: Open a Pull Request

Go to the repository on GitHub. If you just pushed a branch, GitHub will show a yellow banner suggesting you open a PR — click it. Otherwise, go to **Pull requests** → **"New pull request"**, set the base branch to `main` and the compare branch to your feature branch.

**Writing a good PR description:**

A PR description is a professional communication to your teammates. Include:
- **What** this PR does (one or two sentences).
- **Why** — what problem it solves or what requirement it fulfills.
- **How** — a brief note on your approach, especially if you made a non-obvious design decision.
- **Testing** — what you tested manually and whether new tests were added.
- **Screenshots** (for frontend changes) — reviewers can see the result without running the code.

A reviewer should be able to read the description and understand the change before looking at a single line of code.

### Step 6: CI Runs Automatically

As soon as the PR is opened (and every time you push new commits to the branch), the `ci.yml` workflow runs. You will see a status check at the bottom of the PR:

- 🟡 **Pending** — tests are running.
- ✅ **Passing** — all tests passed, the PR is eligible for review.
- ❌ **Failing** — one or more tests failed. The PR cannot be merged. Fix the failing tests and push again.

You can click the status check to see the full workflow log and find out exactly which test failed and why.

### Step 7: Code Review

A team member reviews the code. GitHub's review interface lets reviewers:
- Leave inline comments on specific lines.
- Start a review (collects all comments) and submit it as **"Comment"**, **"Approve"**, or **"Request changes"**.
  - **Comment** — feedback without a verdict.
  - **Approve** — code looks good, can be merged (after other requirements are met).
  - **Request changes** — the reviewer found something that needs to be fixed before merging. The PR is blocked until the reviewer approves or dismisses their own review.

**As the author:** Respond to every comment. If you make a change based on feedback, push a new commit. If you disagree with feedback, explain your reasoning — do not just silently reject it or silently comply.

**As the reviewer:** Be specific and constructive. "This seems wrong" is not useful. "This will throw a NullPointerException if `pet` is null — add a null check before calling `pet.getSpecies()`" is actionable.

### Step 8: Merge

Once:
- ✅ CI tests pass
- ✅ At least one reviewer has approved
- ✅ All review comments are resolved
- ✅ The branch is up to date with `main`

...the **"Merge pull request"** button becomes available.

**Merge strategy — use "Squash and merge" or "Rebase and merge," not "Create a merge commit":**

- **Squash and merge:** Combines all commits from the PR into a single commit on `main`. This keeps `main`'s commit history clean and readable — each entry in the log corresponds to one PR/feature. The individual development commits ("WIP", "fix typo", "try again") are collapsed. **This is the recommended strategy for most teams.**
- **Rebase and merge:** Replays the PR's commits onto `main` without a merge commit. Preserves individual commit messages but keeps history linear.
- **Create a merge commit:** Creates a merge commit that ties the branch and `main` together. This can make `main`'s history hard to read over time ("Merge pull request #47 from feature/add-thing") and is generally avoided.

To enforce a specific strategy, go to **Settings** → **General** → scroll to "Pull Requests" and uncheck the strategies you do not want to allow.

After merging:
1. The CD workflow (`cd.yml`) triggers automatically and deploys to production.
2. GitHub offers to delete the merged branch — do it. Stale branches accumulate quickly.

---

## 11. Repository Settings Worth Knowing

These are in **Settings** → **General** unless noted.

### Pull Requests Section

- **Allow squash merging:** Check this. Set the default commit message to "Pull request title and description."
- **Allow merge commits:** Uncheck this (to enforce squash merging).
- **Allow rebase merging:** Your call — some teams like this option, others prefer pure squash.
- **Automatically delete head branches:** Enable this. After a PR is merged, GitHub automatically deletes the feature branch. Developers can always recreate a branch if needed, and deleted branches are recoverable. This keeps the branch list clean.

### Default Branch

Make sure this is set to `main`. Settings → General → scroll to "Default branch." If it says `master`, click the pencil icon and change it.

### Issues

If you want to track work items and bugs in GitHub, leave Issues enabled. You can link issues to PRs by including `Closes #42` in a PR description — GitHub will automatically close the linked issue when the PR merges.

### Wikis

Disabled by default. You can use GitHub Wikis for internal documentation, but the team may prefer to keep documentation as Markdown files in the repository itself (which is version-controlled and goes through PR review like everything else).

---

## 12. Common Problems and Fixes

### CI workflow does not appear on GitHub

**Cause:** The workflow file is in the wrong location or has a YAML syntax error.

**Fix:** Verify the file is at `.github/workflows/ci.yml` (note the `s` in `workflows`). Validate the YAML at [https://www.yamllint.com](https://www.yamllint.com) — even one wrong indentation level causes the file to be ignored.

---

### CI runs but the required status check does not appear in branch protection

**Cause:** Branch protection's "required status checks" only shows checks that have already run. If the CI workflow has never run on a PR targeting `main`, the check name will not appear in the search.

**Fix:** Open a dummy PR (targeting `main`) from any branch to trigger CI at least once. After CI runs, the `build-and-test` check will be searchable in branch protection settings.

---

### CD workflow fails with "docker: command not found"

**Cause:** The self-hosted runner's user is not in the `docker` group.

**Fix:** See the Production Deployment Guide, Section 17, Step 3. Run `sudo usermod -aG docker RUNNER_USER` and restart the runner service.

---

### CD workflow runs but nothing on the server changes

**Cause:** Most commonly, the rsync step ran fine but `docker compose up --build` was run from the wrong directory, or the `compose.override.yml` is missing from the server.

**Fix:** Click the failed step in GitHub Actions to see the full log. Check that `~/docker/apps/pink_fairy_armadillos/compose.override.yml` exists on the server and that the runner's `$HOME` resolves to the expected user's home directory.

---

### "Required status check is expected" when trying to merge but CI passed

**Cause:** The branch protection rule requires the check named `build-and-test`, but the workflow ran under a slightly different name, or the check is from a different workflow run (re-runs can sometimes create this confusion).

**Fix:** Go to the PR → scroll to the bottom status checks section → click "Details" on the check to see its exact name. Make sure it matches what is entered in branch protection exactly.

---

### A developer accidentally pushed directly to main before branch protection was set up

**Fix for the Git history:** If it was a single commit, revert it:
```bash
git revert HEAD
git push origin main
```
This creates a new commit that undoes the change, preserving history. Do not use `git reset --hard` on a shared branch — it rewrites history and causes serious problems for anyone who has already pulled.

**Fix going forward:** Set up branch protection immediately. Review what was committed to make sure no secrets or sensitive data were included.

---

### PR cannot be merged because "Branch is out of date"

**Cause:** Branch protection has "Require branches to be up to date" enabled, and someone else merged to `main` after this PR was opened.

**Fix (as the PR author):** Update your branch by merging or rebasing from `main`:

```bash
git checkout main
git pull origin main
git checkout feature/your-branch
git merge main   # or: git rebase main
# Resolve any conflicts, then:
git push origin feature/your-branch
```

The PR will update automatically and the "out of date" warning will clear.

---

## 13. Helpful References

| Topic | URL |
|-------|-----|
| GitHub Actions documentation | https://docs.github.com/en/actions |
| Workflow syntax reference | https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions |
| actions/checkout | https://github.com/actions/checkout |
| actions/setup-java | https://github.com/actions/setup-java |
| actions/cache | https://github.com/actions/cache |
| Using secrets in GitHub Actions | https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions |
| GitHub Actions concurrency | https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/control-the-concurrency-of-workflows-and-jobs |
| Branch protection rules | https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/managing-a-branch-protection-rule |
| About self-hosted runners | https://docs.github.com/en/actions/hosting-your-own-runners/managing-self-hosted-runners/about-self-hosted-runners |
| CODEOWNERS file | https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners |
| GitHub Organizations | https://docs.github.com/en/organizations |
| Pull request best practices | https://docs.github.com/en/pull-requests/collaborating-with-pull-requests |
| git revert vs git reset | https://www.atlassian.com/git/tutorials/undoing-changes/git-revert |
| Conventional commit messages | https://www.conventionalcommits.org |
| YAML syntax validator | https://www.yamllint.com |

---

*Documentation prepared by the original PFA development team.*