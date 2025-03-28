#!/bin/bash

# Script to generate comprehensive documentation for FinApp DSL

echo "Generating FinApp DSL documentation..."

# Navigate to the project root
cd "$(dirname "$0")/.."

# Create required directories
mkdir -p doc/api
mkdir -p doc/concepts
mkdir -p doc/guides
mkdir -p doc/examples
mkdir -p doc/notebooks

# Generate API documentation with Codox
echo "Generating API documentation with Codox..."
lein codox

# Generate literate programming documentation with Marginalia
echo "Generating literate programming documentation with Marginalia..."
lein marg

echo "Documentation generation complete!"
echo "API documentation available at: doc/api/index.html" 