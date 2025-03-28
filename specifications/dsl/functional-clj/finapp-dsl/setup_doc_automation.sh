#!/bin/bash

# Script to set up automated documentation generation for FinApp DSL

echo "Setting up automated documentation generation..."

# Make scripts executable
chmod +x doc/generate_docs.sh
chmod +x .git/hooks/pre-commit

# Create symbolic link to make the pre-commit hook active
REPO_ROOT=$(git rev-parse --show-toplevel)
HOOK_TARGET="$REPO_ROOT/.git/hooks/pre-commit"
HOOK_LINK="$REPO_ROOT/specifications/dsl/functional-clj/finapp-dsl/.git/hooks/pre-commit"

# This is a bit tricky because the hook needs to be in the repository root's .git/hooks directory
# But our custom hook is in the project subdirectory
ln -sf "$HOOK_LINK" "$HOOK_TARGET"

# Create a doc-generator namespace to enable the lein task
mkdir -p src/finapp_dsl

# Add the documentation generator namespace
if [ ! -f src/finapp_dsl/doc_generator.clj ]; then
  cat > src/finapp_dsl/doc_generator.clj << 'EOF'
(ns finapp-dsl.doc-generator
  "Documentation generator for FinApp DSL.
   
   This namespace provides functionality to programmatically generate
   documentation from within the Clojure application."
  (:require [codox.main :as codox]))

(defn generate-api-docs
  "Generate API documentation using Codox"
  []
  (codox/generate-docs
    {:language :clojure
     :source-paths ["src"]
     :output-path "doc/api"
     :doc-files ["doc/guides" "doc/concepts"]
     :metadata {:doc/format :markdown}
     :namespaces ['finapp-dsl.core 'finapp-dsl.loan-topup]}))

(defn -main
  "Main entry point for documentation generation via Leiningen.
   
   Usage: lein run -m finapp-dsl.doc-generator"
  [& args]
  (println "Generating FinApp DSL documentation...")
  (generate-api-docs)
  (println "Documentation generation complete!")
  (println "API documentation available at: doc/api/index.html"))
EOF
fi

echo "Done! You can now generate documentation with:"
echo "  ./doc/generate_docs.sh"
echo ""
echo "Documentation will automatically update when you commit changes to source files."
echo "You can also manually trigger documentation generation with:"
echo "  lein generate-docs" 