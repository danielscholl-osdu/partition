# Changelog

## [1.1.1](https://github.com/danielscholl-osdu/partition/compare/v1.1.0...v1.1.1) (2026-03-11)


### 🔧 Miscellaneous

* Add missing Apache 2.0 copyright headers to Java files ([fee2536](https://github.com/danielscholl-osdu/partition/commit/fee25364f309ca7a32c86e0339be982cfd5011b0))
* Add missing Apache 2.0 copyright headers to Java files ([ec20ea6](https://github.com/danielscholl-osdu/partition/commit/ec20ea68078be713a6144579ebc79e37d59c6e5e))
* Sync template updates ([a33e1e7](https://github.com/danielscholl-osdu/partition/commit/a33e1e795231cfcaa1024e5f35abff4f7e87c265))
* Sync template updates ([2537ac5](https://github.com/danielscholl-osdu/partition/commit/2537ac5682b052a4a60a0076a217be090e02a3df))
* Sync template updates ([40ec422](https://github.com/danielscholl-osdu/partition/commit/40ec422ed84c838aae6ce4f9a0234527c6fd0bdb))
* Sync template updates ([747e8a0](https://github.com/danielscholl-osdu/partition/commit/747e8a0f55b6398db16a16d535b48f62c50946fa))
* Update licenses and dependencies with build config changes ([38f6dde](https://github.com/danielscholl-osdu/partition/commit/38f6dded9ae1a94a6bb16482e0dca1c02e5edbc2))

## [1.1.0](https://github.com/danielscholl-osdu/partition/compare/v1.0.0...v1.1.0) (2026-02-20)


### ✨ Features

* **acceptance-test:** Support configurable OIDC scope and access_token ([db03282](https://github.com/danielscholl-osdu/partition/commit/db03282dc50f61d3e5069b6582aaf6c6c8035bdd))
* **acceptance-test:** Support configurable OIDC scope and access_token ([e938f8f](https://github.com/danielscholl-osdu/partition/commit/e938f8f8ab6f1f9a654f4930a1dd59d365fd3670))
* **audit:** Centralize required group definitions via AuditOperation enum ([6920588](https://github.com/danielscholl-osdu/partition/commit/6920588b5422865372302b1ab4faf70c145a8358))
* Implement  dependency management for partition testing and bump ([8a8ff95](https://github.com/danielscholl-osdu/partition/commit/8a8ff95bde4930a4d84b854adfaebb82d6844382))
* Implement  dependency management for partition testing and bump ([4c2ac2d](https://github.com/danielscholl-osdu/partition/commit/4c2ac2d0e4c6958a559911aeb3d876c2b76b2728))
* **partition-aws:** Migrate partition storage from MongoDB to DynamoDB ([fad5f20](https://github.com/danielscholl-osdu/partition/commit/fad5f20cb5154e8b214c34fd4cdf1203d3aa9758))
* **partition:** Enhance audit logging with required groups and request details ([656cdfb](https://github.com/danielscholl-osdu/partition/commit/656cdfbdfc5d5b0fbcc2829e52ffa5c70c7ffa2e))
* Replacing MongoDB database with DynamoDB ([e6294a9](https://github.com/danielscholl-osdu/partition/commit/e6294a9ec058324a00c34ee6cde4349dec8a0651))


### 🐛 Bug Fixes

* Aws startup issue caused by incompatible @Autowired annotation with latest spring boot version ([08f7587](https://github.com/danielscholl-osdu/partition/commit/08f75876b5f36af02ee2ee27d3ee36eea668fe36))
* Aws startup issue caused by incompatible @Autowired annotation with latest spring boot version ([1531ea9](https://github.com/danielscholl-osdu/partition/commit/1531ea908ba4c13a0bd719bdb7d3b70156b2a4e4))
* Remove test value from gc chart ([45b5f00](https://github.com/danielscholl-osdu/partition/commit/45b5f00919e0adf955eb0a6676abde02b4b81d74))
* Tomcat cve ([e6ff869](https://github.com/danielscholl-osdu/partition/commit/e6ff869104e1c5bce6867816087be47327be7e78))
* Tomcat cve ([489577c](https://github.com/danielscholl-osdu/partition/commit/489577c48265cbd0a333def2ebb6402a588333b4))
* Tomcat-core crypto json-smart netty-common CVE ([347383a](https://github.com/danielscholl-osdu/partition/commit/347383a838e76921bf907d113b093c3e436d953a))
* Tomcat-core crypto json-smart netty-common CVE ([68a314b](https://github.com/danielscholl-osdu/partition/commit/68a314b3a46b73d9de94d96779bc6319920e8726))
* Tomcat-core CVE ([2ab9286](https://github.com/danielscholl-osdu/partition/commit/2ab9286baafdfdb9a1cd455551f1c8e812041f63))
* Tomcat-core CVE ([3ba81b7](https://github.com/danielscholl-osdu/partition/commit/3ba81b73e96dbead94cc7c1a2ea5b93b6fb86c53))
* Various CVE ([566c839](https://github.com/danielscholl-osdu/partition/commit/566c839be6b8d2772946b6d7ffdbb0f1b74eb424))
* Various CVE ([3169a8d](https://github.com/danielscholl-osdu/partition/commit/3169a8da4c23982b0a273ffa6888f370868c124b))


### 🔧 Miscellaneous

* Complete repository initialization ([74d9326](https://github.com/danielscholl-osdu/partition/commit/74d9326511f3f98263fee23eca17f2bdb875ef27))
* Copy configuration and workflows from main branch ([fd27e41](https://github.com/danielscholl-osdu/partition/commit/fd27e41ca251ce87d7440eace53ab085047953f0))
* Deleting aws helm chart ([5f71b03](https://github.com/danielscholl-osdu/partition/commit/5f71b03a3a0cca65f629799d62af29618fc41ea5))
* Deleting aws helm chart ([63fd795](https://github.com/danielscholl-osdu/partition/commit/63fd795c04f765614c92b8db1b66f69305225349))
* Dependency bump - patches ([d4f9a91](https://github.com/danielscholl-osdu/partition/commit/d4f9a918d38318edb0b9aa6c322c67aed758e4cf))
* Dependency bump - patches ([7c34772](https://github.com/danielscholl-osdu/partition/commit/7c34772b297cd3aeb2c59e1dc9997bbe48ea8470))
* **deps:** Apply security updates and sync with os-core-common ([631a1db](https://github.com/danielscholl-osdu/partition/commit/631a1dbb4506a99648d64641b94d32019c1373f6))
* **deps:** Apply security updates and sync with os-core-common ([d5a43af](https://github.com/danielscholl-osdu/partition/commit/d5a43af6c4aa5571b318eb892ba1015d144330da))
* **deps:** Dependency bumps ([af6630d](https://github.com/danielscholl-osdu/partition/commit/af6630d123fc91a2e5dffcdfd43ac114da09a02e))
* **deps:** Dependency bumps ([c0aa47e](https://github.com/danielscholl-osdu/partition/commit/c0aa47e9940b66a5ad13aacf9124834d3427c801))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([13968df](https://github.com/danielscholl-osdu/partition/commit/13968df0fb974e8fef7d1a0c0b1246d7972dcb3c))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([88041da](https://github.com/danielscholl-osdu/partition/commit/88041dab1eaf582db89ba1a564f0fda1bcc0b4b1))
* Fixing AWS build. ([4022d7d](https://github.com/danielscholl-osdu/partition/commit/4022d7dcbbd4bc4d14e4856386c4b2acc2264c95))
* Fixing AWS build. ([1de65ca](https://github.com/danielscholl-osdu/partition/commit/1de65ca393d04628a92d105e2e4a2d9ea82af820))
* Fixing sonar issues ([e7ab7c0](https://github.com/danielscholl-osdu/partition/commit/e7ab7c068684dd3f97b50f6444c42b885bb2f655))
* Fixing sonar issues ([a9e7830](https://github.com/danielscholl-osdu/partition/commit/a9e78309b84fe1109babd876b4d98c286efd1f5e))
* Removing helm copy from aws buildspec ([5ee445d](https://github.com/danielscholl-osdu/partition/commit/5ee445d65b51e188c745674ff1f3654f239c460e))
* Sync template updates ([8d89e49](https://github.com/danielscholl-osdu/partition/commit/8d89e49c0e2d9fd791ad08e92ac4a0991fff05c8))
* Updating aws core lib version ([686537f](https://github.com/danielscholl-osdu/partition/commit/686537f6590f09219056e1f74abee1d01f5e5dba))
* Updating aws core lib version ([d10e5ae](https://github.com/danielscholl-osdu/partition/commit/d10e5ae5d0472341a683169f4943f4c39be8995b))


### ♻️ Code Refactoring

* **audit:** Encapsulate audit roles in logging layer ([1397af8](https://github.com/danielscholl-osdu/partition/commit/1397af8b88c627359550088182fe1e8659d9e8b0))
* **audit:** Encapsulate audit roles in logging layer ([234c714](https://github.com/danielscholl-osdu/partition/commit/234c714f5e435e9034750c77bfdabf4b2dfd7d81))


### 🔨 Build System

* **partition-azure:** Bump core-lib-azure to 2.2.8 ([6393eed](https://github.com/danielscholl-osdu/partition/commit/6393eed00ab776fd2b3b346208f685e1d91141b1))
* **partition-azure:** Bump core-lib-azure to 2.2.8 ([c56261e](https://github.com/danielscholl-osdu/partition/commit/c56261e0f43a57a59e2a782398627ee28d67418f))


### ⚙️ Continuous Integration

* Change rules for core jobs ([7c08b48](https://github.com/danielscholl-osdu/partition/commit/7c08b48da6fc94d14c9e6e0e98f3da6ba2aae9f4))
* Change rules for core jobs ([ca00e40](https://github.com/danielscholl-osdu/partition/commit/ca00e40bf546df8f895f8a42deab77cba4a8294c))
* Update gc parameters for env deploys ([37f0c3c](https://github.com/danielscholl-osdu/partition/commit/37f0c3ce82435e05f8613d1e0570fb14c9fb14b4))
* Update gc parameters for env deploys ([e0b2da2](https://github.com/danielscholl-osdu/partition/commit/e0b2da20ee696197b3b6b3cf820f2360c0eb99a3))
