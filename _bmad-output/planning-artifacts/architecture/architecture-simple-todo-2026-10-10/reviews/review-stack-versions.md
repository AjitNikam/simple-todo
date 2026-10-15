# Independent Review — Stack and Version Claims

**Reviewed artifact:** `ARCHITECTURE-SPINE.md`  
**Review date / as-of date:** 2026-10-10  
**Scope:** Current release status, compatibility, and exact version claims. The spine was not edited. Project-local state such as installed versions is treated as reported/verified by the project owner, not independently observable from public sources.

## Verdict

**Compatible, with one patch-freshness action recommended.** The versioned stack is within the published compatibility ranges: Corretto Java 25 works with Spring Boot 4.1.1 and Gradle 9.8.1; Vite 8.3.4 accepts Node 26.7.0; PostgreSQL 18.6 is the current supported 18.x minor. React 19.3.0, Vite 8.3.4, Spring Boot 4.1.1, Gradle 9.8.1, and PostgreSQL 18.6 match the latest stable/current versions displayed by their official sources on 2026-10-10.

**Remaining finding:** Corretto **25.0.2.10.1 is a real build, but is no longer the latest Corretto 25 update**. AWS's official Corretto project lists 25.0.4.10.1, published 2026-09-26. Local verification establishes what is installed; it does not establish that the JDK is current. Refresh to 25.0.4.10.1 (or document a reason for retaining the older patch) before implementation/security-sensitive use.

Node 26.7.0 is no longer the latest 26.x patch, but the spine now explicitly treats it as the installed local runtime rather than claiming it is latest, and records 26.11.1 for refresh. Vite's official package metadata accepts Node 26.7.0. The spine also accurately distinguishes the observed local PostgreSQL 18.3 from the 18.6 target and requires updating before implementation.

## Findings

| Priority | Claim / assessment | Evidence and disposition |
| --- | --- | --- |
| **Medium — update recommended** | Local Java is Amazon Corretto OpenJDK 25.0.2.10.1; stack target says Java 25 LTS. | Corretto's official release record confirms 25.0.2.10.1 exists and was published 2026-01-20 [S2]. The same official release feed identifies 25.0.4.10.1, published 2026-09-26, as latest [S1]. Java 25 is an LTS release [S3], and both Spring Boot 4.1.1 and Gradle 9.8.1 support running on Java 25 [S4, S5]. No compatibility defect; update the installed patch to the latest security/maintenance build or document an intentional pin. |
| **Low — local patch behind latest, disclosed** | Node.js 26.7.0 is installed locally; 26.11.1 is noted as the latest 26.x patch at verification. | Node.js lists 26.11.1 as Current, last updated 2026-10-07 [S6]; its release record confirms that date and status [S7]. The pinned 26.7.0 is a valid Current-line release from 2026-08-05 [S8]. Vite 8.3.4 declares `^20.19.0 || >=22.12.0`, which includes Node 26.7.0 [S9]. The spine transparently calls for refreshing the installed patch; no compatibility issue. For network deployment, also heed Node's policy that production applications should use Active or Maintenance LTS; the spine already keeps this version local-only and calls for reassessment [S6]. |
| **Verified — target/current** | PostgreSQL 18.6 target; local PostgreSQL 18.3 observed and to be updated before implementation. | PostgreSQL's official versioning policy identifies 18.6 as current minor for supported PostgreSQL 18 and recommends the current minor [S10]. It says minor updates do not require dump/restore, but directs operators to read release notes [S10]. The explicit 18.3-to-18.6 pre-implementation update is consistent. The observed local version is a project fact, not independently verifiable from public documentation. |
| **Verified — compatible** | Corretto 25 / Spring Boot 4.1.1 / Gradle 9.8.1. | Spring Boot 4.1.1 supports Java 17–26 and Gradle 8.x (8.14+) and 9.x [S4]. Gradle's 9.8.1 matrix supports Java 25 for running Gradle from Gradle 9.1 onward [S5]. Java 25 is therefore inside both declared support ranges. |
| **Verified — current** | React 19.3.0; Vite 8.3.4; Spring Boot 4.1.1; Gradle 9.8.1. | The official React, Vite, Spring Boot, and Gradle release endpoints list these exact versions as latest stable releases as of review [S11–S14]. |
| **Verified — managed dependencies, with configuration caveat** | Spring Data JPA and Flyway versions are managed by Spring Boot 4.1.1; PostgreSQL Flyway module is used. | Spring Boot's managed dependency coordinates are the authoritative version source for artifacts declared without explicit overrides [S15]. Its supported starters include JPA and Flyway [S16]. The management claim is sound when using Boot dependency management/plugin conventions and not overriding versions. Keep the PostgreSQL-specific Flyway database module explicitly included as the spine states; Boot-managed versioning does not itself add an omitted module dependency. |

## Sources

All sources accessed on **2026-10-10**. “Latest” release feeds are point-in-time evidence and may subsequently change.

| Ref. | Claim supported | Publisher / date | URL |
| --- | --- | --- | --- |
| S1 | Latest Corretto 25 release: 25.0.4.10.1, published 2026-09-26 | Amazon Corretto project, 2026-09-26 | [Corretto 25 latest release](https://api.github.com/repos/corretto/corretto-25/releases/latest) |
| S2 | Corretto 25.0.2.10.1 exists, published 2026-01-20 | Amazon Corretto project, 2026-01-20 | [Corretto 25.0.2.10.1 release](https://api.github.com/repos/corretto/corretto-25/releases/tags/25.0.2.10.1) |
| S3 | Java SE 25 is an LTS release | Oracle, Java SE support roadmap; accessed 2026-10-10 | [Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html) |
| S4 | Spring Boot 4.1.1 supports Java 17–26 and Gradle 8.14+/9.x | Spring, system requirements for Boot 4.1.1; accessed 2026-10-10 | [Spring Boot System Requirements](https://docs.spring.io/spring-boot/system-requirements.html) |
| S5 | Gradle 9.8.1 supports Java 25 as its runtime | Gradle, compatibility matrix version 9.8.1; accessed 2026-10-10 | [Gradle Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html) |
| S6 | Node 26.11.1 is Current/latest v26; production should use LTS | Node.js project, release status page; accessed 2026-10-10 | [Node.js Previous Releases](https://nodejs.org/en/about/previous-releases) |
| S7 | Node 26.11.1 published 2026-10-07 and Current | Node.js project, 2026-10-07 | [Node.js v26.11.1 release](https://github.com/nodejs/node/releases/tag/v26.11.1) |
| S8 | Node 26.7.0 published 2026-08-05 and was Current | Node.js project, 2026-08-05 | [Node.js v26.7.0 release](https://github.com/nodejs/node/releases/tag/v26.7.0) |
| S9 | Vite 8.3.4 supported Node engine range | Vite project package metadata; accessed 2026-10-10 | [Vite 8.3.4 npm metadata](https://registry.npmjs.org/vite/8.3.4) |
| S10 | PostgreSQL 18.6 current minor; minor-upgrade guidance | PostgreSQL Global Development Group; accessed 2026-10-10 | [PostgreSQL Versioning Policy](https://www.postgresql.org/support/versioning/) |
| S11 | React 19.3.0 latest stable release | React project, release published 2026-09-09 | [React latest release](https://api.github.com/repos/react/react/releases/latest) |
| S12 | Vite 8.3.4 latest stable release | Vite project, release published 2026-10-08 | [Vite latest release](https://api.github.com/repos/vitejs/vite/releases/latest) |
| S13 | Spring Boot 4.1.1 latest stable release | Spring project, release published 2026-08-20 | [Spring Boot latest release](https://api.github.com/repos/spring-projects/spring-boot/releases/latest) |
| S14 | Gradle 9.8.1 latest stable release | Gradle project, release published 2026-10-07 | [Gradle latest release](https://api.github.com/repos/gradle/gradle/releases/latest) |
| S15 | Spring Boot managed dependency coordinates | Spring Boot reference; accessed 2026-10-10 | [Managed Dependency Coordinates](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html) |
| S16 | Boot JPA/Flyway starter support and managed dependency conventions | Spring Boot reference; accessed 2026-10-10 | [Build systems and starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html) |
