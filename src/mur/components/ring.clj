(ns mur.components.ring
  (:require
   [com.stuartsierra.component :as c]
   [mur.components.middleware :as cptmdw]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol IWebRequestHandler
  (request-handler [this]))

;; =================================================================
;; web request handler head
;; =================================================================

(defrecord RingHead [ring-handler ring-middleware]
  IWebRequestHandler
  (request-handler [this]
    (let [wrapper (if (some? ring-middleware)
                    (cptmdw/wrapper ring-middleware)
                    identity)]
      (wrapper (request-handler ring-handler)))))

(defn make-ring-head
  []
  (c/using
   (map->RingHead {})
   [:ring-handler]))
