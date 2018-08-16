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

(defrecord WebRequestHandlerHead [handler middleware]
  IWebRequestHandler
  (request-handler [this]
    (let [wrapper (if (some? middleware)
                    (cptmdw/wrapper middleware)
                    identity)]
      (wrapper (request-handler handler)))))

(defn make-web-request-handler-head
  []
  (c/using
   (map->WebRequestHandlerHead {})
   [:handler]))
