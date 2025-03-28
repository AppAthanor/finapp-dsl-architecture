.PHONY: docs setup-docs clean-docs serve-docs check-docs

# Generate documentation
docs:
	@echo "Generating documentation..."
	@./doc/generate_docs.sh

# Set up documentation automation
setup-docs:
	@echo "Setting up documentation automation..."
	@./setup_doc_automation.sh

# Clean generated documentation
clean-docs:
	@echo "Cleaning documentation..."
	@rm -rf doc/api
	@rm -rf target/doc

# Serve documentation locally using a simple HTTP server
serve-docs:
	@echo "Serving documentation at http://localhost:8000/api/"
	@cd doc && python3 -m http.server 8000

# Check for missing documentation
check-docs:
	@echo "Checking for missing documentation..."
	@lein with-profile dev run -m finapp-dsl.doc-checker

# Watch for changes and regenerate docs
watch-docs:
	@echo "Watching for changes and regenerating docs..."
	@lein cljdoc

# Install all documentation dependencies
install-doc-deps:
	@echo "Installing documentation dependencies..."
	@lein deps 