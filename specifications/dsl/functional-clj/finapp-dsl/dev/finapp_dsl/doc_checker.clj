(ns finapp-dsl.doc-checker
  "Utility to check for missing documentation in the codebase.
   
   This namespace provides tools to scan the codebase and identify
   functions that lack proper documentation."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.namespace.find :as ns-find]))

(defn has-docstring? 
  "Check if a var has a docstring.
   
   Parameters:
   * var - The var to check
   
   Returns true if the var has a docstring, false otherwise."
  [var]
  (boolean (:doc (meta var))))

(defn public-var? 
  "Check if a var is public (not private).
   
   Parameters:
   * var - The var to check
   
   Returns true if the var is public, false otherwise."
  [var]
  (not (:private (meta var))))

(defn fn-var? 
  "Check if a var holds a function.
   
   Parameters:
   * var - The var to check
   
   Returns true if the var holds a function, false otherwise."
  [var]
  (let [val (var-get var)]
    (and val (or (fn? val) (instance? clojure.lang.MultiFn val)))))

(defn check-namespace
  "Check a namespace for functions missing documentation.
   
   Parameters:
   * ns-sym - The symbol naming the namespace to check
   
   Returns a sequence of symbols naming functions without docstrings."
  [ns-sym]
  (try
    (require ns-sym)
    (let [ns-obj (find-ns ns-sym)]
      (->> (ns-publics ns-obj)
           (filter (fn [[_ var]] (and (fn-var? var) (public-var? var) (not (has-docstring? var)))))
           (map first)
           (map #(symbol (name ns-sym) (name %)))))
    (catch Exception e
      (println "Error loading namespace" ns-sym ":" (.getMessage e))
      [])))

(defn find-clojure-files
  "Find all Clojure source files in a directory.
   
   Parameters:
   * dir - The directory to search
   
   Returns a sequence of File objects for Clojure source files."
  [dir]
  (ns-find/find-sources-in-dir (io/file dir)))

(defn find-namespaces
  "Find all namespaces in a set of directories.
   
   Parameters:
   * dirs - A sequence of directory paths to search
   
   Returns a sequence of namespace symbols."
  [dirs]
  (mapcat #(ns-find/find-namespaces-in-dir (io/file %)) dirs))

(defn check-all-namespaces
  "Check all namespaces in the project for functions missing documentation.
   
   Returns a map from namespace symbols to sequences of function symbols."
  []
  (let [source-paths ["src"]
        namespaces (find-namespaces source-paths)]
    (->> namespaces
         (map (fn [ns]
                [ns (check-namespace ns)]))
         (filter (fn [[_ missing]] (seq missing)))
         (into {}))))

(defn -main
  "Check the project for missing documentation and print a report.
   
   Usage: lein run -m finapp-dsl.doc-checker"
  [& args]
  (println "Checking for missing documentation...")
  (let [missing-docs (check-all-namespaces)]
    (if (seq missing-docs)
      (do
        (println "\nFunctions missing documentation:")
        (doseq [[ns fns] missing-docs]
          (println "\nNamespace:" ns)
          (doseq [fn fns]
            (println "  -" (name fn))))
        (println "\nTotal functions missing documentation:" 
                 (reduce + (map count (vals missing-docs))))
        (System/exit 1))
      (do
        (println "All functions are documented!")
        (System/exit 0)))))

(comment
  ;; For REPL usage
  (check-all-namespaces)
  (check-namespace 'finapp-dsl.core)
  (-main)
) 