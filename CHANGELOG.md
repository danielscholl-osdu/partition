# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0 (2025-07-12)


### 🐛 Bug Fixes

* Remove test value from gc chart ([45b5f00](https://github.com/danielscholl-osdu/partition/commit/45b5f00919e0adf955eb0a6676abde02b4b81d74))
* Tomcat-core crypto json-smart netty-common CVE ([347383a](https://github.com/danielscholl-osdu/partition/commit/347383a838e76921bf907d113b093c3e436d953a))
* Tomcat-core crypto json-smart netty-common CVE ([68a314b](https://github.com/danielscholl-osdu/partition/commit/68a314b3a46b73d9de94d96779bc6319920e8726))


### 📚 Documentation

* Updating helm chart documentation and versioning ([1c034d7](https://github.com/danielscholl-osdu/partition/commit/1c034d7fc49b391440b61a78b389d64e347e47a0))


### 🔧 Miscellaneous

* Complete repository initialization ([51d6113](https://github.com/danielscholl-osdu/partition/commit/51d61132fc7e23cfd4f64202a9f34930af2bbad3))
* Copy configuration and workflows from main branch ([31b0123](https://github.com/danielscholl-osdu/partition/commit/31b01239cbc4a48277468cd70703cfd37262236e))
* Deleting aws helm chart ([5f71b03](https://github.com/danielscholl-osdu/partition/commit/5f71b03a3a0cca65f629799d62af29618fc41ea5))
* Deleting aws helm chart ([63fd795](https://github.com/danielscholl-osdu/partition/commit/63fd795c04f765614c92b8db1b66f69305225349))
* Fixing AWS build. ([4022d7d](https://github.com/danielscholl-osdu/partition/commit/4022d7dcbbd4bc4d14e4856386c4b2acc2264c95))
* Fixing AWS build. ([1de65ca](https://github.com/danielscholl-osdu/partition/commit/1de65ca393d04628a92d105e2e4a2d9ea82af820))
* Removing helm copy from aws buildspec ([5ee445d](https://github.com/danielscholl-osdu/partition/commit/5ee445d65b51e188c745674ff1f3654f239c460e))
* Updating aws core lib version ([686537f](https://github.com/danielscholl-osdu/partition/commit/686537f6590f09219056e1f74abee1d01f5e5dba))
* Updating aws core lib version ([d10e5ae](https://github.com/danielscholl-osdu/partition/commit/d10e5ae5d0472341a683169f4943f4c39be8995b))


### ⚙️ Continuous Integration

* Change rules for core jobs ([7c08b48](https://github.com/danielscholl-osdu/partition/commit/7c08b48da6fc94d14c9e6e0e98f3da6ba2aae9f4))
* Change rules for core jobs ([ca00e40](https://github.com/danielscholl-osdu/partition/commit/ca00e40bf546df8f895f8a42deab77cba4a8294c))
* Update gc parameters for env deploys ([37f0c3c](https://github.com/danielscholl-osdu/partition/commit/37f0c3ce82435e05f8613d1e0570fb14c9fb14b4))
* Update gc parameters for env deploys ([e0b2da2](https://github.com/danielscholl-osdu/partition/commit/e0b2da20ee696197b3b6b3cf820f2360c0eb99a3))

## [2.0.0] - Major Workflow Enhancement & Documentation Release

### ✨ Features
- **Comprehensive MkDocs Documentation Site**: Complete documentation overhaul with GitHub Pages deployment
- **Automated Cascade Failure Recovery**: System automatically recovers from cascade workflow failures
- **Human-Centric Cascade Pattern**: Issue lifecycle tracking with human notifications for critical decisions
- **Integration Validation**: Comprehensive validation system for cascade workflows
- **Claude Workflow Integration**: Full Claude Code CLI support with Maven MCP server integration
- **GitHub Copilot Enhancement**: Java development environment setup and firewall configuration
- **Fork Resources Staging Pattern**: Template-based staging for fork-specific configurations
- **Conventional Commits Validation**: Complete validation system with all supported commit types
- **Enhanced PR Label Management**: Simplified production PR labels with automated issue closure
- **Meta Commit Strategy**: Advanced release-please integration for better version management
- **Push Protection Handling**: Sophisticated upstream secrets detection and resolution workflows

### 🔨 Build System
- **Workflow Separation Pattern**: Template development vs. fork instance workflow isolation
- **Template Workflow Management**: 9 comprehensive template workflows for fork management
- **Enhanced Action Reliability**: Improved cascade workflow trigger reliability with PR event filtering
- **Base64 Support**: Enhanced create-enhanced-pr action with encoding capabilities

### 📚 Documentation
- **Structured MkDocs Site**: Complete documentation architecture with GitHub Pages
- **AI-First Development Docs**: Comprehensive guides for AI-enhanced development
- **ADR Documentation**: 20+ Architectural Decision Records covering all major decisions
- **Workflow Specifications**: Detailed documentation for all 9 template workflows
- **Streamlined README**: Focused quick-start guide directing to comprehensive documentation

### 🛡️ Security & Reliability
- **Advanced Push Protection**: Intelligent handling of upstream repositories with secrets
- **Branch Protection Integration**: Automated branch protection rule management
- **Security Pattern Recognition**: Enhanced security scanning and pattern detection
- **MCP Configuration**: Secure Model Context Protocol integration for AI development

### 🔧 Workflow Enhancements
- **Cascade Monitoring**: Advanced cascade workflow monitoring and SLA management
- **Dependabot Integration**: Enhanced dependabot validation and automation
- **Template Synchronization**: Sophisticated template update propagation system
- **Issue State Tracking**: Advanced issue lifecycle management and tracking
- **GITHUB_TOKEN Standardization**: Improved token handling across all workflows

### ♻️ Code Refactoring
- **Removed AI_EVOLUTION.md**: Migrated to structured ADR approach for better maintainability
- **Simplified README Structure**: Eliminated redundancy between README and documentation site
- **Enhanced Initialization Cleanup**: Improved fork repository cleanup and setup process
- **Standardized Error Handling**: Consistent error handling patterns across all workflows

### 🐛 Bug Fixes
- **YAML Syntax Issues**: Resolved multiline string handling in workflow configurations
- **Release Workflow Compatibility**: Updated to googleapis/release-please-action@v4
- **MCP Server Configuration**: Fixed Maven MCP server connection and configuration issues
- **Cascade Trigger Reliability**: Implemented pull_request_target pattern for better triggering
- **Git Diff Syntax**: Corrected git command syntax in sync-template workflow
- **Label Management**: Standardized label usage across all workflows and templates

## [1.0.0] - Initial Release

### ✨ Features
- Initial release of OSDU Fork Management Template
- Automated fork initialization workflow
- Daily upstream synchronization with AI-enhanced PR descriptions
- Three-branch management strategy (main, fork_upstream, fork_integration)
- Automated conflict detection and resolution guidance
- Semantic versioning and release management
- Template development workflows separation

### 📚 Documentation
- Complete architectural decision records (ADRs)
- Product requirements documentation
- Development and usage guides
- GitHub Actions workflow documentation
