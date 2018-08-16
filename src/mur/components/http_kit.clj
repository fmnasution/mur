(ns mur.components.http-kit
  (:require
   [com.stuartsierra.component :as c]
   [org.httpkit.server :refer [run-server]]
   [mur.components.ring :as cptrng]))

;; =================================================================
;; web server
;; =================================================================

(defrecord WebServer [port handler server]
  c/Lifecycle
  (start [this]
    (if (some? server)
      this
      (assoc this :server (run-server (cptrng/request-handler handler)
                                      {:port port}))))
  (stop [this]
    (if (nil? server)
      this
      (do (server :timeout 100)
          (assoc this :server nil)))))

(defn make-web-server
  [option]
  (-> option
      (select-keys [:port])
      (map->WebServer)
      (c/using [:handler])))
