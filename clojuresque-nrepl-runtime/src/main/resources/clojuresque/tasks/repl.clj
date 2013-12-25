(ns clojuresque.tasks.repl
  (:require
    [clojuresque.util :as util]
    [clojure.tools.nrepl.server :as repl]))

(def barrier (promise))
(def server nil)

(util/deftask start-repl
  [{:keys [port handler]}]
  (let [p (Long/parseLong port)
        h (if handler
            (let [custom-handler (util/resolve-required handler)]
              (when-not custom-handler
                (throw (Exception. (str "Unknown handler: " handler))))
              (custom-handler))
            (repl/default-handler))
        s (repl/start-server :port p :handler h)]
    (println "Repl started on port" port)
    (alter-var-root #'server (constantly s)))
  @barrier)

(defn stop-repl
  []
  (when server
    (repl/stop-server server)
    (deliver barrier true)))
