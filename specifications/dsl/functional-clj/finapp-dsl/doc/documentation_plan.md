# FinApp DSL Documentation Plan

## Documentation Goals

1. Provide comprehensive reference documentation for the DSL core components
2. Document domain-specific concepts and their implementation
3. Create tutorials for common use cases and journeys
4. Establish a style guide for DSL usage
5. Demonstrate business rule implementations across regions

## Documentation Components

### 1. API Reference Documentation

We'll use [Codox](https://github.com/weavejester/codox) to generate API documentation from docstrings:

- Core language constructs
- Environment and evaluation components
- Domain utilities and helpers
- Business rule implementations

### 2. Domain Concept Documentation

For each domain concept, we'll create detailed markdown files explaining:

- Business purpose
- Implementation details
- Validation rules
- Regional variations
- Usage examples

### 3. User Guides and Tutorials

Step-by-step guides for implementing common financial journeys:

- Loan Top-Up journey
- Eligibility assessment
- Customizing for new regions
- Adding new customer segments
- Implementing complex business rules

### 4. DSL Usage Patterns

Document recommended patterns for:

- Building expressions
- Extending the environment
- Creating business rules
- Testing DSL components
- Debugging

### 5. Interactive Documentation

- REPL-based examples
- Interactive tutorials using Clerk notebooks
- Visual representations of DSL evaluation

## Documentation Structure

```
/doc
├── api/                  # Generated API documentation
├── concepts/             # Domain concept documentation
│   ├── regions.md
│   ├── segments.md
│   ├── business_rules.md
│   └── journeys.md
├── guides/               # User guides and tutorials
│   ├── getting_started.md
│   ├── loan_topup.md
│   ├── eligibility.md
│   └── extending.md
├── examples/             # Example implementations
│   ├── uk_region.md
│   ├── wealth_segment.md
│   └── amount_rules.md
└── notebooks/            # Interactive documentation
    ├── core_concepts.clj
    ├── loan_journey.clj
    └── visualizations.clj
```

## Implementation Strategy

1. **Short-term (1-2 weeks)**:
   - Add comprehensive docstrings to all functions
   - Generate basic API documentation
   - Create initial domain concept documentation

2. **Medium-term (2-4 weeks)**:
   - Develop user guides and tutorials
   - Add diagrams and visualizations
   - Create interactive examples

3. **Long-term (ongoing)**:
   - Maintain documentation with code changes
   - Add domain-specific examples
   - Create video tutorials

## Documentation Tooling

1. **Code Documentation**:
   - [Codox](https://github.com/weavejester/codox) for API docs
   - [Marginalia](https://github.com/gdeer81/marginalia) for literate programming style

2. **Interactive Documentation**:
   - [Clerk](https://github.com/nextjournal/clerk) for interactive notebooks
   - [Oz](https://github.com/metasoarous/oz) for data visualizations

3. **Diagram Generation**:
   - [Mermaid](https://mermaid-js.github.io/) for sequence and flow diagrams
   - [PlantUML](https://plantuml.com/) for UML diagrams

4. **Documentation Site**:
   - [cljdoc](https://cljdoc.org/) for hosting library documentation
   - GitHub Pages for additional interactive documentation

## Next Steps

1. Add comprehensive docstrings to core.clj and loan_topup.clj
2. Set up Codox for the project
3. Create initial documentation for regions and customer segments
4. Develop a "Getting Started" guide 