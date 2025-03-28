(ns finapp-dsl.doc-publisher
  "Utility for publishing documentation to external sites.
   
   This namespace provides tools for publishing documentation
   to external sites like GitHub Pages and cljdoc."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(defn ensure-dir!
  "Ensure a directory exists, creating it if necessary.
   
   Parameters:
   * dir - The directory path to ensure exists"
  [dir]
  (let [file (io/file dir)]
    (when-not (.exists file)
      (.mkdirs file))))

(defn copy-dir!
  "Copy a directory and its contents to another location.
   
   Parameters:
   * src - The source directory
   * dest - The destination directory"
  [src dest]
  (ensure-dir! dest)
  (doseq [file (file-seq (io/file src))
          :when (.isFile file)]
    (let [rel-path (str/replace-first (.getPath file) (str src "/") "")
          dest-file (io/file (str dest "/" rel-path))]
      (ensure-dir! (.getParent dest-file))
      (io/copy file dest-file))))

(defn publish-to-github-pages
  "Publish documentation to GitHub Pages.
   
   This function:
   1. Generates documentation using Codox
   2. Creates a temporary gh-pages branch
   3. Copies documentation to the root
   4. Commits and pushes to GitHub
   
   Parameters:
   * doc-dir - The directory containing generated documentation (default \"doc/api\")
   
   Returns nil"
  ([] (publish-to-github-pages "doc/api"))
  ([doc-dir]
   (println "Publishing documentation to GitHub Pages...")
   (let [temp-dir (str (System/getProperty "java.io.tmpdir") "/finapp-dsl-gh-pages")
         current-branch (str/trim (:out (sh "git" "rev-parse" "--abbrev-ref" "HEAD")))]
     
     ;; Clean up any previous temp directory
     (when (.exists (io/file temp-dir))
       (sh "rm" "-rf" temp-dir))
     
     ;; Copy documentation to temp directory
     (copy-dir! doc-dir temp-dir)
     
     ;; Create and switch to gh-pages branch
     (sh "git" "checkout" "--orphan" "gh-pages-temp")
     (sh "git" "rm" "-rf" ".")
     
     ;; Copy docs from temp directory
     (copy-dir! temp-dir ".")
     
     ;; Add, commit, and push
     (sh "git" "add" ".")
     (sh "git" "commit" "-m" "Update documentation")
     (sh "git" "push" "origin" "gh-pages-temp:gh-pages" "--force")
     
     ;; Return to original branch
     (sh "git" "checkout" current-branch)
     
     ;; Clean up
     (sh "git" "branch" "-D" "gh-pages-temp")
     (sh "rm" "-rf" temp-dir)
     
     (println "Documentation published to GitHub Pages successfully!"))))

(defn publish-to-cljdoc
  "Publish documentation to cljdoc.org.
   
   This function:
   1. Builds the project with lein
   2. Deploys to Clojars
   3. Triggers a cljdoc build
   
   Parameters:
   * version - The version to publish (default is from project.clj)
   
   Returns nil"
  ([] (publish-to-cljdoc nil))
  ([version]
   (println "Publishing documentation to cljdoc.org...")
   
   ;; Get the current version if not specified
   (let [version (or version
                     (let [project-clj (slurp "project.clj")
                           version-pattern #"\(defproject\s+[\w\-\.]+\s+\"([^\"]+)\""
                           matcher (re-find version-pattern project-clj)]
                       (second matcher)))]
     
     ;; Clean and build the project
     (sh "lein" "clean")
     (sh "lein" "jar")
     
     ;; Deploy to Clojars
     (sh "lein" "deploy" "clojars")
     
     ;; Trigger cljdoc build
     (sh "curl" "-X" "POST" 
         (str "https://cljdoc.org/api/request-build/finapp-dsl/finapp-dsl/" version))
     
     (println "Documentation published to cljdoc.org successfully!")
     (println "View it at: https://cljdoc.org/d/finapp-dsl/finapp-dsl/" version))))

(defn -main
  "Publish documentation to all configured platforms.
   
   Usage: lein run -m finapp-dsl.doc-publisher [target]
   Where target is 'github', 'cljdoc', or 'all' (default)"
  [& args]
  (let [target (if (seq args) (first args) "all")]
    (case target
      "github" (publish-to-github-pages)
      "cljdoc" (publish-to-cljdoc)
      "all" (do (publish-to-github-pages)
                (publish-to-cljdoc))
      (println "Unknown target:" target))))

(comment
  ;; For REPL usage
  (publish-to-github-pages)
  (publish-to-cljdoc)
) 