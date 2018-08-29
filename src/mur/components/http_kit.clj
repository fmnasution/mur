(ns mur.components.http-kit
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [org.httpkit.server :refer [run-server]]
   [mur.components.ring :as cptrng]))

;; =================================================================
;; web server spec
;; =================================================================

(s/def ::port
  pos-int?)

(s/def ::config
  (s/keys :req-un [::port]))

;; =================================================================
;; web server
;; =================================================================

(defrecord WebServer [port ring-handler server]
  c/Lifecycle
  (start [this]
    (if (some? server)
      this
      (assoc this :server (run-server (cptrng/request-handler ring-handler)
                                      {:port port}))))
  (stop [this]
    (if (nil? server)
      this
      (do (server :timeout 100)
          (assoc this :server nil)))))

(defn make-web-server
  [option]
  (-> (s/assert ::config option)
      (select-keys [:port])
      (map->WebServer)
      (c/using [:ring-handler])))
