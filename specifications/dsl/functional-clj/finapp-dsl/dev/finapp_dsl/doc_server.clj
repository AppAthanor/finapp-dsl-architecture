(ns finapp-dsl.doc-server
  "Simple HTTP server for serving documentation locally.
   
   This namespace provides a simple HTTP server for serving
   the generated documentation locally."
  (:require [clojure.java.io :as io]
            [clojure.java.browse :as browse])
  (:import [com.sun.net.httpserver HttpServer HttpHandler HttpExchange]
           [java.net InetSocketAddress URI]
           [java.io File]
           [java.util.concurrent Executors]))

(def ^:private default-port 8888)
(def ^:private default-doc-root "doc")

(defn- mime-type [^String path]
  (cond
    (.endsWith path ".html") "text/html"
    (.endsWith path ".css") "text/css"
    (.endsWith path ".js") "application/javascript"
    (.endsWith path ".json") "application/json"
    (.endsWith path ".png") "image/png"
    (.endsWith path ".jpg") "image/jpeg"
    (.endsWith path ".jpeg") "image/jpeg"
    (.endsWith path ".gif") "image/gif"
    (.endsWith path ".svg") "image/svg+xml"
    :else "text/plain"))

(defn- send-response [^HttpExchange exchange status ^String content-type ^bytes body]
  (let [headers (.getResponseHeaders exchange)]
    (.add headers "Content-Type" content-type)
    (.sendResponseHeaders exchange status (if body (alength body) 0))
    (when body
      (with-open [os (.getResponseBody exchange)]
        (.write os body)
        (.flush os)))))

(defn- file-handler [^String doc-root]
  (reify HttpHandler
    (handle [_ exchange]
      (try
        (let [uri (-> (.getRequestURI exchange) .getPath)
              path (if (or (= uri "/") (= uri ""))
                     "/api/index.html"
                     uri)
              file (io/file (str doc-root path))]
          (if (and (.exists file) (.isFile file))
            (send-response exchange 200 
                           (mime-type path) 
                           (Files/readAllBytes (.toPath file)))
            (send-response exchange 404 "text/html" 
                           (.getBytes (str "<h1>404 Not Found</h1><p>The file " path " was not found.</p>")))))
        (catch Exception e
          (println "Error serving" (.getRequestURI exchange) ":" (.getMessage e))
          (send-response exchange 500 "text/html" 
                         (.getBytes (str "<h1>500 Internal Server Error</h1><p>" (.getMessage e) "</p>"))))))))

(defn start-server 
  "Start a local HTTP server to serve documentation.
   
   Parameters:
   * port - The port to listen on (default 8888)
   * doc-root - The root directory containing documentation (default \"doc\")
   
   Returns the server instance."
  ([] (start-server default-port default-doc-root))
  ([port] (start-server port default-doc-root))
  ([port doc-root]
   (let [server (HttpServer/create (InetSocketAddress. port) 0)]
     (.createContext server "/" (file-handler doc-root))
     (.setExecutor server (Executors/newFixedThreadPool 4))
     (.start server)
     (println "Documentation server started at http://localhost:" port "/")
     (println "API documentation available at http://localhost:" port "/api/")
     server)))

(defn stop-server 
  "Stop a documentation server.
   
   Parameters:
   * server - The server instance to stop"
  [^HttpServer server]
  (.stop server 0)
  (println "Documentation server stopped."))

(defn -main
  "Start a documentation server and open a browser.
   
   Usage: lein run -m finapp-dsl.doc-server [port]"
  [& args]
  (let [port (if (seq args)
               (Integer/parseInt (first args))
               default-port)
        server (start-server port)]
    (browse/browse-url (str "http://localhost:" port "/api/"))
    (println "Press Ctrl+C to stop the server.")
    (.. Thread currentThread join))) ; Keep running until interrupted

(comment
  ;; For REPL usage
  (def server (start-server))
  (stop-server server)
) 