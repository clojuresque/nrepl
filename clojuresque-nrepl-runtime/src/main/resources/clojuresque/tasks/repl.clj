(ns clojuresque.tasks.repl
  (:require
    [clojuresque.util :as util]
    [clojure.tools.nrepl.server :as repl]))

(def barrier (promise))
(def server nil)

(util/deftask start-repl
  [{:keys [port handler middleware injections]}]
  (when-let [injections (seq injections)]
    (apply require (map symbol injections)))
  (let [p   (if (string? port)
              (Long/parseLong port)
              port)
        mw  (map util/resolve-required middleware)
        h   (if handler
              (let [custom-handler (util/resolve-required handler)]
                (when-not custom-handler
                  (throw (Exception. (str "Unknown handler: " handler))))
                @custom-handler)
              (apply repl/default-handler mw))
        s   (repl/start-server :port p :handler h)]
    (println
      (format "nREPL server started on port %1$d on host 127.0.0.1 - nrepl://127.0.0.1:%1$d" port))
    (alter-var-root #'server (constantly s)))
  @barrier)

(defn stop-repl
  []
  (when server
    (repl/stop-server server)
    (deliver barrier true)))
