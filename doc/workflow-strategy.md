# Workflow Distribution Strategy

This document outlines which workflows are included in forked repositories vs. which remain template-only.

## Essential Workflows (Copied to Forks)

These workflows are copied during initialization and can be updated via template-sync:

### Core Functionality
- **`sync.yml`** - Upstream repository synchronization
- **`validate.yml`** - PR validation and commit message checks  
- **`build.yml`** - Project build and test automation
- **`release.yml`** - Automated semantic versioning and releases

### Infrastructure Management
- **`template-sync.yml`** - Template repository updates (this workflow!)
- **`dependabot-validation.yml`** - Dependabot PR automation

### Support Files
- **`.github/actions/`** - Custom GitHub Actions
- **`.github/labels.json`** - Repository label configuration
- **`.github/dependabot.yml`** - Dependency update configuration
- **`.github/.template-sync-commit`** - Template sync tracking file

## Template-Only Workflows (Not Copied)

These workflows remain in the template repository and are not copied to forks:

### Repository Management  
- **`init.yml`** - Repository initialization trigger
- **`init-complete.yml`** - Repository setup and configuration
- **`cascade.yml`** - Multi-repository cascade operations
- **`cascade-monitor.yml`** - Cascade monitoring and SLA management

## How Template Sync Works

1. **During Init**: Only essential files are copied according to `.github/sync-config.json`
2. **Template Updates**: `template-sync.yml` pulls updates for configured sync paths only
3. **Configuration-Driven**: Sync behavior is defined in `.github/sync-config.json`
4. **Version Tracking**: `.template-sync-commit` tracks the last synced template version

For detailed information about the sync configuration, see [Sync Configuration Documentation](sync-configuration.md).

## Benefits

- **Clean Separation**: Template management stays in template, project workflows go to forks
- **Automatic Updates**: Forks get workflow improvements without manual intervention
- **No Pollution**: Template-specific workflows don't clutter fork repositories
- **Self-Updating**: Template-sync workflow can update itself

## Example Repository Structure

### Template Repository
```
.github/workflows/
├── init.yml              # Template-only
├── init-complete.yml     # Template-only  
├── cascade.yml           # Template-only
├── cascade-monitor.yml   # Template-only
├── sync.yml              # → Copied to forks
├── validate.yml          # → Copied to forks  
├── build.yml             # → Copied to forks
├── release.yml           # → Copied to forks
├── template-sync.yml     # → Copied to forks
└── dependabot-validation.yml # → Copied to forks
```

### Fork Repository (After Init)
```
.github/workflows/
├── sync.yml              # Synced from template
├── validate.yml          # Synced from template
├── build.yml             # Synced from template  
├── release.yml           # Synced from template
├── template-sync.yml     # Synced from template
└── dependabot-validation.yml # Synced from template
```

This approach ensures forks get all the essential automation while keeping template management workflows separate.