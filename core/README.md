# Core Framework

This directory contains the core framework components that support the entire FinApp Multi-DSL Architecture.

## Overview

The Core Framework provides shared utilities, configuration, integration interfaces, and documentation that are used by all other components in the monorepo. It serves as the foundation upon which the specification, code generation, and output management components are built.

## Directory Structure

```
core/
├── lib/                 # Shared libraries and utilities
│   ├── common/          # Common utilities (logging, error handling, etc.)
│   ├── dsl/             # Base DSL components and utilities
│   └── testing/         # Common testing utilities
│
├── config/              # Common configuration and standards
│   ├── eslint/          # ESLint configuration
│   ├── prettier/        # Prettier configuration
│   └── tsconfig/        # TypeScript configuration
│
├── integration/         # Integration interfaces
│   ├── api/             # API interfaces
│   ├── events/          # Event system
│   └── plugins/         # Plugin system
│
└── docs/                # Documentation
    ├── architecture/    # Architecture documentation
    ├── getting-started/ # Getting started guides
    └── api/             # API documentation
```

## Key Components

### Shared Libraries

The `lib` directory contains shared utilities used throughout the monorepo:

- **Common Utilities**: Logging, error handling, and other common utilities
- **DSL Utilities**: Base components for DSL implementation
- **Testing Utilities**: Common testing infrastructure

### Configuration

The `config` directory contains shared configuration files:

- **ESLint**: Code quality and style enforcement
- **Prettier**: Code formatting
- **TypeScript**: Type definitions and configuration

### Integration Interfaces

The `integration` directory contains interfaces for integrating the different components:

- **API Interfaces**: Definitions for APIs between components
- **Event System**: Communication between components
- **Plugin System**: Extension mechanisms

### Documentation

The `docs` directory contains comprehensive documentation:

- **Architecture**: Overall system architecture
- **Getting Started**: Guides for new developers
- **API Documentation**: Interface documentation

## Usage

To use the Core Framework in other components:

```javascript
// Import from the core library
const { Logger } = require('@finapp/core/lib/common/logger');

// Create a component-specific logger
const logger = new Logger('code-generation');

// Use the logger
logger.info('Starting code generation...');
```

## Development

When developing the Core Framework, remember that changes here affect all other components. Follow these guidelines:

1. **Backward Compatibility**: Maintain backward compatibility whenever possible
2. **Versioning**: Use semantic versioning for all interfaces
3. **Documentation**: Document all public APIs and interfaces
4. **Testing**: Write thorough tests for all functionality
5. **Performance**: Consider the performance impact of all changes 