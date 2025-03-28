# Documentation Automation

This document explains how the automated documentation system for FinApp DSL works.

## Overview

The documentation system automatically generates and publishes documentation for the FinApp DSL, ensuring that:

1. Documentation is always up-to-date with the code
2. All public functions are documented
3. Documentation is easily accessible both locally and online

## Components

### 1. Code Documentation

- **Docstrings**: All public functions should have comprehensive docstrings with:
  - Function purpose
  - Parameter descriptions
  - Return value descriptions
  - Example usage

- **Namespace Documentation**: Each namespace should have a docstring explaining its purpose

### 2. Concept Documentation

- Markdown files in `doc/concepts/` explaining domain concepts:
  - Regions
  - Customer segments
  - Business rules
  - Financial journeys

### 3. User Guides

- Markdown files in `doc/guides/` providing step-by-step tutorials

### 4. Automated Tools

- **Pre-commit Hook**: Automatically generates documentation before committing
- **GitHub Actions**: Updates and publishes documentation on code changes
- **Documentation Checker**: Identifies missing documentation
- **Documentation Server**: Serves documentation locally for development
- **Documentation Publisher**: Publishes documentation to GitHub Pages and cljdoc

## Setting Up Documentation Automation

Run the setup script to set up documentation automation:

```bash
./setup_doc_automation.sh
```

This will:
1. Make documentation scripts executable
2. Set up the pre-commit hook
3. Create necessary directories and files

## Available Commands

Use these commands to work with the documentation system:

| Command | Description |
|---------|-------------|
| `make docs` | Generate documentation |
| `make setup-docs` | Set up documentation automation |
| `make clean-docs` | Remove generated documentation |
| `make serve-docs` | Start a local documentation server |
| `make check-docs` | Check for missing documentation |
| `make watch-docs` | Watch for changes and regenerate docs |

Alternatively, use Leiningen tasks:

| Task | Description |
|------|-------------|
| `lein docs` | Generate documentation |
| `lein watch-docs` | Watch for changes and regenerate docs |
| `lein check-docs` | Check for missing documentation |
| `lein publish-docs` | Publish documentation to GitHub Pages and cljdoc |
| `lein dev-docs` | Generate docs and start a local server |
| `lein verify` | Run tests and check documentation |

## Continuous Integration

The GitHub Actions workflow `.github/workflows/documentation.yml` will:

1. Generate documentation on each push to main
2. Publish documentation to GitHub Pages
3. Keep documentation up-to-date with code changes

## Best Practices

1. **Write Docstrings First**: Use docstring-driven development
2. **Update Concept Docs**: When changing domain concepts, update concept documentation
3. **Run `make check-docs`**: Before committing, check for missing documentation
4. **Use Examples**: Include examples in your docstrings
5. **Update Guides**: Keep user guides in sync with implementation changes

## Troubleshooting

- **Missing Documentation**: If `check-docs` reports missing documentation, add docstrings to the identified functions
- **Failed Builds**: Check GitHub Actions logs for errors in documentation generation
- **Local Development**: Use `make serve-docs` to preview documentation locally

## Additional Resources

- [Codox Documentation](https://github.com/weavejester/codox)
- [Marginalia Documentation](https://github.com/gdeer81/marginalia)
- [cljdoc.org](https://cljdoc.org/) 