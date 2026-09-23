# Changelog

## [1.2.0](https://github.com/danielscholl-osdu/partition/compare/v1.1.0...v1.2.0) (2026-09-23)


### ✨ Features

* **acceptance-test:** Support configurable OIDC scope and access_token ([db03282](https://github.com/danielscholl-osdu/partition/commit/db03282dc50f61d3e5069b6582aaf6c6c8035bdd))
* **acceptance-test:** Support configurable OIDC scope and access_token ([e938f8f](https://github.com/danielscholl-osdu/partition/commit/e938f8f8ab6f1f9a654f4930a1dd59d365fd3670))
* Implement  dependency management for partition testing and bump ([8a8ff95](https://github.com/danielscholl-osdu/partition/commit/8a8ff95bde4930a4d84b854adfaebb82d6844382))
* Implement  dependency management for partition testing and bump ([4c2ac2d](https://github.com/danielscholl-osdu/partition/commit/4c2ac2d0e4c6958a559911aeb3d876c2b76b2728))
* Removed System Partition API ([be4dd53](https://github.com/danielscholl-osdu/partition/commit/be4dd5343e04fe770d9b8eede5ec9e79547f0c97))
* Removed System Partition API ([6c0babc](https://github.com/danielscholl-osdu/partition/commit/6c0babcb7662544588d371a2c8cd8e0154366318))
* Replacing MongoDB database with DynamoDB ([e6294a9](https://github.com/danielscholl-osdu/partition/commit/e6294a9ec058324a00c34ee6cde4349dec8a0651))
* **spi:** Add the acceptance and integration service descriptor ([1afc462](https://github.com/danielscholl-osdu/partition/commit/1afc46232d1b86b98f8f4baaeb3e28151a221f60))
* **spi:** Add the acceptance and integration service descriptor ([0f6619b](https://github.com/danielscholl-osdu/partition/commit/0f6619b819b31714b1c2e72f8a055d63db6065cb))


### 🐛 Bug Fixes

* Aws startup issue caused by incompatible @Autowired annotation with latest spring boot version ([08f7587](https://github.com/danielscholl-osdu/partition/commit/08f75876b5f36af02ee2ee27d3ee36eea668fe36))
* Aws startup issue caused by incompatible @Autowired annotation with latest spring boot version ([1531ea9](https://github.com/danielscholl-osdu/partition/commit/1531ea908ba4c13a0bd719bdb7d3b70156b2a4e4))
* **azure:** Netty-bom before core-lib-azure (lettuce 7.5.2 NoClassDefFoundError) ([6bb0fb3](https://github.com/danielscholl-osdu/partition/commit/6bb0fb3a2a971b578d1f491193ac0aa048a9a090))
* **azure:** Netty-bom before core-lib-azure (lettuce 7.5.2 NoClassDefFoundError) ([28afaf3](https://github.com/danielscholl-osdu/partition/commit/28afaf3da5d2c9c28970ea8c8ada26b7ed933338))
* **azure:** Upgrade core-lib-azure to 3.0.1 ([561c0f7](https://github.com/danielscholl-osdu/partition/commit/561c0f73c7ba714d9226bd86812f340408b0e621))
* **azure:** Upgrade core-lib-azure to 3.0.1 ([a9a3cf8](https://github.com/danielscholl-osdu/partition/commit/a9a3cf82ac7ce4022b58486b47600c57ff7f67cf))
* Bump netty and tomcat for CVE remediation ([675eba5](https://github.com/danielscholl-osdu/partition/commit/675eba521503ac09e7b955f0d8d05aedde30c4ae))
* Bump netty and tomcat for CVE remediation ([f356318](https://github.com/danielscholl-osdu/partition/commit/f356318bafd0215d061bc3cc97167843aab8b861))
* CVE remediation wave 1 (tomcat, log4j-api, jackson-databind, kotlin-stdlib) ([d194fa7](https://github.com/danielscholl-osdu/partition/commit/d194fa7e2641aebce879a684d2d60ab2f9014bba))
* **cve:** Pin bcprov-jdk18on 1.84 in partition-aws (CVE-2025-14813) ([7ce6746](https://github.com/danielscholl-osdu/partition/commit/7ce674657491b85647d73bb2805d86d05ddd86b7))
* **cve:** Pin bcprov-jdk18on 1.84 in partition-aws (CVE-2025-14813) ([b9a2cac](https://github.com/danielscholl-osdu/partition/commit/b9a2cac42fb0ffebf39fd8e68b6d954ff923b47a))
* Remediate CVE remediation wave 1 (tomcat, log4j-api, jackson-databind, kotlin-stdlib) ([53767d1](https://github.com/danielscholl-osdu/partition/commit/53767d1f7e20fc21bfe3d86ed1f686daeb8e66a2))
* Remove test value from gc chart ([45b5f00](https://github.com/danielscholl-osdu/partition/commit/45b5f00919e0adf955eb0a6676abde02b4b81d74))
* **spi:** Bind CLIENT_TENANT for the hosted integration path ([a430cb5](https://github.com/danielscholl-osdu/partition/commit/a430cb584817170d6aa4414cdf18e9614e68a8a1))
* **spi:** Bind the no-access bearer for the integration 401 cases ([210601d](https://github.com/danielscholl-osdu/partition/commit/210601d04e271522b2d51fb578db017972e525d2))
* Sync upstream changes from a48954c8 ([75d0fea](https://github.com/danielscholl-osdu/partition/commit/75d0feacd9cd254338ec813dc4d667248d557326))
* Tomcat cve ([e6ff869](https://github.com/danielscholl-osdu/partition/commit/e6ff869104e1c5bce6867816087be47327be7e78))
* Tomcat cve ([489577c](https://github.com/danielscholl-osdu/partition/commit/489577c48265cbd0a333def2ebb6402a588333b4))
* Tomcat-core crypto json-smart netty-common CVE ([347383a](https://github.com/danielscholl-osdu/partition/commit/347383a838e76921bf907d113b093c3e436d953a))
* Tomcat-core crypto json-smart netty-common CVE ([68a314b](https://github.com/danielscholl-osdu/partition/commit/68a314b3a46b73d9de94d96779bc6319920e8726))
* Tomcat-core CVE ([2ab9286](https://github.com/danielscholl-osdu/partition/commit/2ab9286baafdfdb9a1cd455551f1c8e812041f63))
* Tomcat-core CVE ([3ba81b7](https://github.com/danielscholl-osdu/partition/commit/3ba81b73e96dbead94cc7c1a2ea5b93b6fb86c53))
* Various CVE ([566c839](https://github.com/danielscholl-osdu/partition/commit/566c839be6b8d2772946b6d7ffdbb0f1b74eb424))
* Various CVE ([3169a8d](https://github.com/danielscholl-osdu/partition/commit/3169a8da4c23982b0a273ffa6888f370868c124b))


### 📚 Documentation

* **acceptance:** Fill in the source column of the environment table ([5aaac09](https://github.com/danielscholl-osdu/partition/commit/5aaac0975fe2006ea0bbd83f7687046ccd43c973))
* **acceptance:** Fill in the source column of the environment table ([f89e19f](https://github.com/danielscholl-osdu/partition/commit/f89e19f91092bc2fdf7675ea263d11d68cfc91bb))


### 🔧 Miscellaneous

* Add missing Apache 2.0 copyright headers to Java files ([fee2536](https://github.com/danielscholl-osdu/partition/commit/fee25364f309ca7a32c86e0339be982cfd5011b0))
* Add missing Apache 2.0 copyright headers to Java files ([ec20ea6](https://github.com/danielscholl-osdu/partition/commit/ec20ea68078be713a6144579ebc79e37d59c6e5e))
* **ci:** Remove IBM jobs from pipeline ([7d5f5e8](https://github.com/danielscholl-osdu/partition/commit/7d5f5e8f05ce1c1090b1dbccfee0277651a307c4))
* **ci:** Remove IBM jobs from pipeline ([a3fb144](https://github.com/danielscholl-osdu/partition/commit/a3fb144b90a6216d2a91dcfb297e0f84f6e84afc))
* Complete repository initialization ([f4b4634](https://github.com/danielscholl-osdu/partition/commit/f4b4634450b1bc47805cedd0248e9535ed2ef25e))
* Copy configuration and workflows from main branch ([72a0dfe](https://github.com/danielscholl-osdu/partition/commit/72a0dfe27fea8425197c2179ef6304353ab6e5b6))
* Deleting aws helm chart ([5f71b03](https://github.com/danielscholl-osdu/partition/commit/5f71b03a3a0cca65f629799d62af29618fc41ea5))
* Deleting aws helm chart ([63fd795](https://github.com/danielscholl-osdu/partition/commit/63fd795c04f765614c92b8db1b66f69305225349))
* Dependency bump - patches ([d4f9a91](https://github.com/danielscholl-osdu/partition/commit/d4f9a918d38318edb0b9aa6c322c67aed758e4cf))
* Dependency bump - patches ([7c34772](https://github.com/danielscholl-osdu/partition/commit/7c34772b297cd3aeb2c59e1dc9997bbe48ea8470))
* **deps:** Apply security updates and sync with os-core-common ([631a1db](https://github.com/danielscholl-osdu/partition/commit/631a1dbb4506a99648d64641b94d32019c1373f6))
* **deps:** Apply security updates and sync with os-core-common ([d5a43af](https://github.com/danielscholl-osdu/partition/commit/d5a43af6c4aa5571b318eb892ba1015d144330da))
* **deps:** Dependency bumps ([af6630d](https://github.com/danielscholl-osdu/partition/commit/af6630d123fc91a2e5dffcdfd43ac114da09a02e))
* **deps:** Dependency bumps ([c0aa47e](https://github.com/danielscholl-osdu/partition/commit/c0aa47e9940b66a5ad13aacf9124834d3427c801))
* **deps:** Remediate partition vulnerabilities ([2efe9da](https://github.com/danielscholl-osdu/partition/commit/2efe9da9523ddd1b4c331371e0cdbb5a8bd73c72))
* **deps:** Remediate partition vulnerabilities ([74946fc](https://github.com/danielscholl-osdu/partition/commit/74946fcfd6e34da921cdc49e953d241e232499f0))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([13968df](https://github.com/danielscholl-osdu/partition/commit/13968df0fb974e8fef7d1a0c0b1246d7972dcb3c))
* **deps:** Security dependency remediation - Spring Boot 3.5.8 and library updates ([88041da](https://github.com/danielscholl-osdu/partition/commit/88041dab1eaf582db89ba1a564f0fda1bcc0b4b1))
* Fixing AWS build. ([4022d7d](https://github.com/danielscholl-osdu/partition/commit/4022d7dcbbd4bc4d14e4856386c4b2acc2264c95))
* Fixing AWS build. ([1de65ca](https://github.com/danielscholl-osdu/partition/commit/1de65ca393d04628a92d105e2e4a2d9ea82af820))
* Fixing sonar issues ([e7ab7c0](https://github.com/danielscholl-osdu/partition/commit/e7ab7c068684dd3f97b50f6444c42b885bb2f655))
* Fixing sonar issues ([a9e7830](https://github.com/danielscholl-osdu/partition/commit/a9e78309b84fe1109babd876b4d98c286efd1f5e))
* Generate filtered upstream tree ([52c796f](https://github.com/danielscholl-osdu/partition/commit/52c796ff7a6949b3a94bf48c9207765527c6ca09))
* Generate filtered upstream tree ([f98aacb](https://github.com/danielscholl-osdu/partition/commit/f98aacbc415602403256c232f221b05917cd2574))
* Remove AWS provider and AWS CI/CD ([c4841a2](https://github.com/danielscholl-osdu/partition/commit/c4841a266e8b9cf22abdddcaceef70d73a8e341b))
* Remove AWS provider and AWS CI/CD ([78d2ab3](https://github.com/danielscholl-osdu/partition/commit/78d2ab36baac41a230dc7a4c8b523e892969ddc5))
* Removing helm copy from aws buildspec ([5ee445d](https://github.com/danielscholl-osdu/partition/commit/5ee445d65b51e188c745674ff1f3654f239c460e))
* Seed fork-owned azure trees ([0e75ce5](https://github.com/danielscholl-osdu/partition/commit/0e75ce56814b90517e92297dc74c1fae4629db6e))
* **sonar:** Remove unused imports in partition-aws AuthorizationService (S1128) ([82b0fbe](https://github.com/danielscholl-osdu/partition/commit/82b0fbee6e72865e1c2614fedf6502fe7c392c74))
* **sonar:** Remove unused imports in partition-aws AuthorizationService (S1128) ([f9d2119](https://github.com/danielscholl-osdu/partition/commit/f9d21191a4cfc858a4d2df5a1b75df273d5198b8))
* Sync template updates ([7226e37](https://github.com/danielscholl-osdu/partition/commit/7226e3705ec50c876f4dd1424ac6ab1254ba27cb))
* Sync template updates ([4d18d74](https://github.com/danielscholl-osdu/partition/commit/4d18d747c0bb02465b1aaba7a31a560877f5ae5a))
* Sync template updates ([bd7e37d](https://github.com/danielscholl-osdu/partition/commit/bd7e37d1b99893d4bb0e7fff132bb961aa705ecb))
* **template-sync:** Sync template updates (updated 2026-09-23) ([6bf2646](https://github.com/danielscholl-osdu/partition/commit/6bf26460e01ea2d52b2e479c66a57b564c26fd0e))
* **template-sync:** Sync template updates 2026-09-16 ([c6e0c0a](https://github.com/danielscholl-osdu/partition/commit/c6e0c0a9fef9728159180f000f04b89e4560b0cf))
* **template-sync:** Sync template updates 2026-09-23 ([124f3e0](https://github.com/danielscholl-osdu/partition/commit/124f3e093414a29d44366686a26a9b908444534d))
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
