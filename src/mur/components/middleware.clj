(ns mur.components.middleware
  (:require
   [com.stuartsierra.component :as c]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol IMiddleware
  (wrapper [this]))

;; =================================================================
;; middleware
;; =================================================================

(defn- substitute
  [component entry]
  (if (vector? entry)
    (replace {:component component} entry)
    entry))

(defn- as-middleware
  [entry]
  (if (vector? entry)
    #(apply (first entry) % (rest entry))
    entry))

(defn- compose
  [component entries]
  (apply comp (into []
                    (comp
                     (map #(substitute component %))
                     (map as-middleware))
                    entries)))

(defrecord Middleware [entries generated-wrapper middleware]
  c/Lifecycle
  (start [this]
    (if (some? generated-wrapper)
      this
      (assoc this :generated-wrapper (compose this entries))))
  (stop [this]
    (if (nil? generated-wrapper)
      this
      (assoc this :generated-wrapper nil)))

  IMiddleware
  (wrapper [this]
    (comp generated-wrapper (if (some? middleware)
                           (wrapper middleware)
                           identity))))

(defn make-middleware
  [entries]
  (map->Middleware {:entries entries}))
